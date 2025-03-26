package org.bigmouth.gpt.controller;

import com.bxm.warcar.cache.Counter;
import com.bxm.warcar.cache.KeyGenerator;
import com.bxm.warcar.cache.impls.redis.JedisCounter;
import com.bxm.warcar.utils.IpHelper;
import com.bxm.warcar.utils.UUIDHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bigmouth.gpt.ApplicationConfig;
import org.bigmouth.gpt.ai.ChatServiceFactory;
import org.bigmouth.gpt.ai.entity.ChatServiceArgument;
import org.bigmouth.gpt.entity.ChatRequest;
import org.bigmouth.gpt.entity.User;
import org.bigmouth.gpt.exceptions.ForbiddenException;
import org.bigmouth.gpt.exceptions.TalkxException;
import org.bigmouth.gpt.interceptor.ContextFactory;
import org.bigmouth.gpt.utils.Constants;
import org.bigmouth.gpt.utils.RedisKeys;
import org.bigmouth.gpt.utils.WebUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.Duration;
import java.util.Objects;

/**
 * 聊天接口
 *
 * @author allen
 * @date 2023-04-20
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/gpt")
public class GptController {

    private final ChatServiceFactory chatServiceFactory;
    private final Counter counter;
    private final ApplicationConfig config;

    public static final int GUEST_CHAT_COUNT_EXPIRE_TIME = Math.toIntExact(Duration.ofDays(1).getSeconds());

    public GptController(ChatServiceFactory chatServiceFactory, Counter counter, ApplicationConfig config) {
        this.chatServiceFactory = chatServiceFactory;
        this.counter = counter;
        this.config = config;
    }

    @PostMapping("/chat")
    public ResponseEntity<StreamingResponseBody> chat(@RequestBody ChatRequest chatRequest,
                                                      HttpServletRequest httpServletRequest,
                                                      HttpServletResponse httpServletResponse) {
        checkLimitForIp(httpServletRequest);

        String reqId = UUIDHelper.generate();

        // 获取用户信息
        User user = ContextFactory.getLoginUser();

        // 限制游客请求次数
        boolean isGuest = Objects.isNull(user);
        if (isGuest && doInterceptor(httpServletRequest)) {
            throw new ForbiddenException(String.format("游客身份每天限制 %d 次对话，请登录后继续免费使用。", config.getMaximumChatByDaily()));
        }

        // 初始化 sessionId
        String sessionId = chatRequest.getSessionId();
        if (StringUtils.isBlank(sessionId)) {
            sessionId = UUIDHelper.generate();
            chatRequest.setSessionId(sessionId);
        }

        StreamingResponseBody body = outputStream -> {
            ChatServiceArgument argument = ChatServiceArgument.builder()
                    .id(reqId)
                    .user(user)
                    .chatRequest(chatRequest)
                    .httpServletResponse(httpServletResponse)
                    .writeConsumer(outputStream::write)
                    .flushRunnable(outputStream::flush)
                    .clientAbortExceptionStringBiConsumer((e, s) -> {
                        IOUtils.closeQuietly(outputStream);
                    })
                    .build();
            chatServiceFactory.chat(argument);
        };
        HttpHeaders headers = new HttpHeaders();
        headers.add(Constants.HEADER_SESSIONID, chatRequest.getSessionId());
        headers.add(Constants.HEADER_MSGID, reqId);
        return ResponseEntity.ok().headers(headers).contentType(MediaType.TEXT_PLAIN).body(body);
    }

    private boolean doInterceptor(HttpServletRequest httpServletRequest) {
        String ip = WebUtils.getIpAddr(httpServletRequest);
        String ua = WebUtils.getUserAgent(httpServletRequest);
        String md5IpUa = DigestUtils.md5Hex(ip + ua);
        Long afterIncr = counter.incrementAndGet(RedisKeys.stringGuestChatCount(md5IpUa), GUEST_CHAT_COUNT_EXPIRE_TIME);
        return afterIncr > config.getMaximumChatByDaily();
    }

    public void checkLimitForIp(HttpServletRequest request) throws IllegalStateException {
        String ip = IpHelper.getIpFromHeader(request);
        KeyGenerator keyGenerator = RedisKeys.stringIpLimitPerMinute(ip);
        int expireTime = (int) Duration.ofMinutes(1).getSeconds();
        Long value = counter.incrementAndGet(keyGenerator, expireTime);
        if (log.isDebugEnabled()) {
            log.debug("{} [{}] requestUri={} | - {} = {}", ip,
                    request.getMethod(),
                    request.getRequestURI(), keyGenerator.generateKey(), value);
        }
        if (value > 5) {
            throw new TalkxException("您的操作太快了，请休息一下吧！");
        }
    }
}
