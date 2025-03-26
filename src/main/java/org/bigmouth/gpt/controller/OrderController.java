package org.bigmouth.gpt.controller;


import com.bxm.warcar.utils.IpHelper;
import com.bxm.warcar.utils.UrlHelper;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.bigmouth.gpt.ApplicationConfig;
import org.bigmouth.gpt.entity.Order;
import org.bigmouth.gpt.entity.PrepayDto;
import org.bigmouth.gpt.entity.User;
import org.bigmouth.gpt.entity.response.OrderVo;
import org.bigmouth.gpt.integration.wechat.WeChatException;
import org.bigmouth.gpt.integration.wechat.WechatAuthIntegration;
import org.bigmouth.gpt.interceptor.ContextFactory;
import org.bigmouth.gpt.service.IOrderService;
import org.bigmouth.gpt.utils.Constants;
import org.bigmouth.gpt.utils.QRCodeGenerator;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.imageio.ImageIO;
import jakarta.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author allen
 * @since 2023-11-03
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    private final ApplicationConfig applicationConfig;
    private final IOrderService orderService;
    private final WechatAuthIntegration wechatAuthIntegration;

    public OrderController(ApplicationConfig applicationConfig, IOrderService orderService, WechatAuthIntegration wechatAuthIntegration) {
        this.applicationConfig = applicationConfig;
        this.orderService = orderService;
        this.wechatAuthIntegration = wechatAuthIntegration;
    }

    @GetMapping(value = "/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qrcode(String orderId) throws IOException {

        String orderUrl = UriComponentsBuilder.fromUriString(applicationConfig.getTalkxHost())
                .fragment("/pay?order=" + orderId)
                .build()
                .toString();
        String orderUrlEn = UrlHelper.urlEncode(orderUrl);

        String redirectUrl = UriComponentsBuilder.fromUriString("https://wechat.bianxianmao.com/basic/authorize_redirect")
                .queryParam("next", orderUrlEn)
                .build()
                .toString();

        String redirectUrlEn = UrlHelper.urlEncode(redirectUrl);

        String wx = "https://open.weixin.qq.com/connect/oauth2/authorize" +
                "?appid=" + wechatAuthIntegration.getWechatConfig().getAppId() +
                "&redirect_uri=" + redirectUrlEn +
                "&response_type=code" +
                "&scope=snsapi_base" +
                "#wechat_redirect";
        log.info("wx-authorize-url: {}", wx);
        BufferedImage image = QRCodeGenerator.generate(wx);
        if (Objects.isNull(image)) {
            return ResponseEntity.ok().build();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] byteArray = baos.toByteArray();
        return ResponseEntity.ok(byteArray);
    }

    @PostMapping("/create")
    public ResponseEntity<CreateOrderResponse> create(@RequestBody @Validated CreateOrder request) {
        User user = ContextFactory.getLoginUser();
        String orderId = orderService.createOrder(user.getId(), request.getCatalogId());
        return ResponseEntity.ok(new CreateOrderResponse().setOrderId(orderId));
    }

    @GetMapping("/query")
    public ResponseEntity<OrderVo> query(@RequestParam String orderId) {
        Order order = orderService.query(orderId);
        if (Objects.isNull(order)) {
            return ResponseEntity.notFound().build();
        }
        OrderVo orderVo = new OrderVo(order);
        String url = UriComponentsBuilder.fromUriString(applicationConfig.getTalkxApiHost())
                .path("/order/qrcode")
                .queryParam("orderId", orderId)
                .build()
                .toString();
        orderVo.setOrderPageQrcode(url);
        return ResponseEntity.ok(orderVo);
    }

    @PostMapping("/prepay")
    public ResponseEntity<Object> prepay(@RequestBody @Validated PrepayRequest prepayRequest,
                                         HttpServletRequest request) {
        PrepayDto prepayDto = new PrepayDto()
                .setOrderId(prepayRequest.getOrderId())
                .setPayType(prepayRequest.getPayType())
                .setClientType(prepayRequest.getClientType())
                .setClientIp(IpHelper.getIpFromHeader(request));

        if (Objects.equals(prepayRequest.getPayType(), Constants.Order.PAY_TYPE_WECHAT)) {
            try {
                String wxCode = prepayRequest.getWxCode();
                String openId = wechatAuthIntegration.getOpenId(wxCode);
                prepayDto.setOpenId(openId);
            } catch (WeChatException e) {
                log.error("getOpenId: ", e);
                throw new IllegalStateException("微信授权失败");
            }
        }

        Object res = orderService.prepay(prepayDto);
        return ResponseEntity.ok(res);
    }

    @Data
    public static class CreateOrder {
        @NotNull(message = "商品不能为空")
        private Long catalogId;
    }

    @Data
    @Accessors(chain = true)
    public static class CreateOrderResponse {
        private String orderId;
    }

    @Data
    public static class PrepayRequest {
        @NotBlank(message = "订单号不能为空") private String orderId;
        @NotNull private Integer payType;
        @NotNull private Integer clientType;
        private String wxCode;
    }
}
