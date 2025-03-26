package org.bigmouth.gpt.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bigmouth.gpt.entity.Favorites;
import org.bigmouth.gpt.entity.PageRequest;
import org.bigmouth.gpt.entity.Session;
import org.bigmouth.gpt.entity.SessionMessage;
import org.bigmouth.gpt.interceptor.ContextFactory;
import org.bigmouth.gpt.service.IFavoritesService;
import org.bigmouth.gpt.service.ISessionMessageService;
import org.bigmouth.gpt.service.ISessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * @author huxiao
 * @date 2024/4/15
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/favorites")
public class FavoritesController {

    private final ISessionService sessionService;
    private final ISessionMessageService sessionMessageService;
    private final IFavoritesService favoritesService;

    public FavoritesController(ISessionService sessionService, ISessionMessageService sessionMessageService, IFavoritesService favoritesService) {
        this.sessionService = sessionService;
        this.sessionMessageService = sessionMessageService;
        this.favoritesService = favoritesService;
    }

    @PostMapping("/save_msg")
    public ResponseEntity<Object> saveMsg(@RequestBody @Validated SaveMsgReq req) {
        Long messageId = req.getMessageId();
        SessionMessage message = sessionMessageService.getById(messageId);
        if (Objects.isNull(message)) {
            return ResponseEntity.ok().build();
        }
        String sessionId = message.getSessionId();
        Session session = sessionService.getById(sessionId);
        if (Objects.isNull(session)) {
            return ResponseEntity.ok().build();
        }
        Long userId = ContextFactory.getLoginUser().getId();
        if (!Objects.equals(session.getUserId(), userId)) {
            return ResponseEntity.ok().build();
        }
        long count = favoritesService.count(Wrappers.query(new Favorites().setUserId(userId).setSessionMessageId(messageId)));
        if (count > 0) {
            return ResponseEntity.ok().build();
        }
        Favorites favorites = new Favorites()
                .setUserId(userId)
                .setSessionId(sessionId)
                .setSessionMessageId(messageId)
                .setRole(message.getRole())
                .setContent(message.getContent())
                .setStatus(message.getStatus())
                .setIsIncludeAttachs(message.getIsIncludeAttachs())
                .setCreateTime(message.getCreateTime());
        if (!favoritesService.save(favorites)) {
            throw new IllegalStateException("收藏失败了！");
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/delete_msg")
    public ResponseEntity<Object> deleteMsg(@RequestBody @Validated DeleteMsgReq req) {
        Long userId = ContextFactory.getLoginUser().getId();
        Favorites favorites = favoritesService.getOne(Wrappers.query(new Favorites().setUserId(userId).setId(req.getId())));
        if (Objects.isNull(favorites)) {
            return ResponseEntity.notFound().build();
        }
        if (!favoritesService.removeById(req.getId())) {
            throw new IllegalStateException("删除收藏失败了！");
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public ResponseEntity<IPage<Favorites>> list(@Validated ListReq req) {
        Long userId = ContextFactory.getLoginUser().getId();
        IPage<Favorites> page = new Page<>(req.getCurrent(), req.getSize());
        QueryWrapper<Favorites> query = Wrappers.query(new Favorites().setUserId(userId));
        query.orderByDesc(Favorites.ID);
        IPage<Favorites> favoritesPage = favoritesService.page(page, query);
        return ResponseEntity.ok(favoritesPage);
    }

    @Data
    public static class SaveMsgReq {
        @NotNull(message = "消息ID不能为空")
        private Long messageId;
    }

    @Data
    public static class DeleteMsgReq {
        @NotNull(message = "ID不能为空")
        private Long id;
    }

    public static class ListReq extends PageRequest {
    }
}
