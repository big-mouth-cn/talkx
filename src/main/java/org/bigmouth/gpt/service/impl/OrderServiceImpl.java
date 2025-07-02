package org.bigmouth.gpt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bxm.warcar.id.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.bigmouth.gpt.ApplicationConfig;
import org.bigmouth.gpt.entity.CoinCatalog;
import org.bigmouth.gpt.entity.Order;
import org.bigmouth.gpt.entity.PrepayDto;
import org.bigmouth.gpt.facade.PayOrder;
import org.bigmouth.gpt.integration.DistributedLock;
import org.bigmouth.gpt.integration.EmptyDistributedLock;
import org.bigmouth.gpt.integration.PayIntegration;
import org.bigmouth.gpt.mapper.talkx.OrderMapper;
import org.bigmouth.gpt.service.CoinService;
import org.bigmouth.gpt.service.ICoinCatalogService;
import org.bigmouth.gpt.service.IOrderService;
import org.bigmouth.gpt.utils.Constants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author allen
 * @since 2023-11-03
 */
@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    private final ApplicationConfig applicationConfig;
    private final ICoinCatalogService coinCatalogService;
    private final IdGenerator idGenerator;
    private final PayIntegration payIntegration;
    private final CoinService coinService;
    private final DistributedLock distributedLock;

    public OrderServiceImpl(ApplicationConfig applicationConfig, ICoinCatalogService coinCatalogService,
                            @Qualifier("orderIdGenerator") IdGenerator idGenerator, PayIntegration payIntegration, CoinService coinService) {
        this.applicationConfig = applicationConfig;
        this.coinCatalogService = coinCatalogService;
        this.idGenerator = idGenerator;
        this.payIntegration = payIntegration;
        this.coinService = coinService;
        this.distributedLock = new EmptyDistributedLock();
    }

    @Override
    public Order query(String orderId) {
        return getOne(Wrappers.query(new Order().setOrderId(orderId)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public String createOrder(Long userId, Long catalogId) {
        // 先判断是否已经存在未支付的订单
//        int waitForPayCount = count(Wrappers.query(new Order().setUserId(userId).setStatus(Constants.Order.WAIT_PAY)));
//        if (waitForPayCount > 0) {
//            throw new IllegalStateException("存在待支付订单，请先取消或支付。");
//        }
        // 检查提交的商品目录是否可用
        CoinCatalog coinCatalog = coinCatalogService.getById(catalogId);
        if (Objects.isNull(coinCatalog) || coinCatalog.getStocks() <= 0) {
            throw new IllegalStateException("商品已下架或库存不足");
        }
        // 创建订单
        String orderId = idGenerator.next();
        Order order = new Order()
                .setUserId(userId)
                .setOrderId(orderId)
                .setStatus(Constants.Order.WAIT_PAY)
                .setProductType(Constants.Order.PRODUCT_TYPE_COIN)
                .setProductAmount(coinCatalog.getCoins())
                .setTotalPrice(coinCatalog.getPrice());

        if (!save(order)) {
            throw new IllegalStateException("下单失败，原因不明");
        }
        return orderId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public Object prepay(PrepayDto prepayDto) {
        String orderId = prepayDto.getOrderId();
        boolean acquired = canAcquireLockOrElseThrows(orderId);
        try {
            Order order = query(orderId);
            if (Objects.isNull(order)) {
                throw new IllegalArgumentException("支付的订单不存在");
            }
            Integer status = order.getStatus();

            boolean isNotAvailablePrepayStatus = Objects.equals(status, Constants.Order.WAIT_PAY) || Objects.equals(status, Constants.Order.PAYING);
            if (!isNotAvailablePrepayStatus) {
                throw new IllegalStateException("订单状态不允许支付");
            }

            PayOrder payOrder = new PayOrder()
                    .setOrderNum(orderId)
                    .setAmount(order.getTotalPrice())
                    .setPayType(prepayDto.getPayType())
                    .setSubject("TalkX Coin Recharge")
                    .setBody(orderId)
                    .setClientType(prepayDto.getClientType())
                    .setOpenId(prepayDto.getOpenId())
                    .setClientIp(prepayDto.getClientIp())
                    .setNotifyUrl(applicationConfig.getPayNotifyUrl());

            order.setStatus(Constants.Order.PAYING);
            order.setModifyTime(LocalDateTime.now());
            if (!updateById(order)) {
                throw new IllegalStateException(orderId + " 订单更新失败了");
            }

            return payIntegration.pay(payOrder);
        } finally {
            if (acquired) {
                distributedLock.release(orderId);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 10)
    public void delivery(String orderId, String paymentId) {
        boolean acquired = canAcquireLockOrElseThrows(orderId);
        try {
            Order order = query(orderId);
            if (Objects.isNull(order)) {
                log.error("订单支付成功，交付阶段订单不存在：{}", orderId);
                return;
            }
            Integer status = order.getStatus();
            if (!Objects.equals(Constants.Order.PAYING, status)) {
                // 只允许支付中变为支付成功
                log.warn("订单 {} 支付成功回调，但是非支付中状态：{}。", orderId, status);
                return;
            }

            order.setPaymentId(paymentId);
            order.setStatus(Constants.Order.SUCCESS);
            order.setModifyTime(LocalDateTime.now());

            if (!updateById(order)) {
                throw new IllegalStateException(orderId + " 订单更新失败了");
            }

            BigDecimal amount = order.getProductAmount();
            if (!coinService.plus(order.getUserId(), Constants.Coin.BILL_TYPE_RECHARGE, amount, orderId)) {
                throw new IllegalStateException(orderId + " 订单充值蒜粒失败了");
            }
        } finally {
            if (acquired) {
                distributedLock.release(orderId);
            }
        }
    }

    @Override
    public BigDecimal sum(QueryWrapper<Order> queryWrapper) {
        return getBaseMapper().sum(queryWrapper);
    }

    private boolean canAcquireLockOrElseThrows(String orderId) {
        boolean acquired = distributedLock.acquire(orderId, 5, TimeUnit.SECONDS);
        if (!acquired) {
            throw new IllegalStateException("order has been locked!");
        }
        return true;
    }
}
