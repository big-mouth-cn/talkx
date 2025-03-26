package org.bigmouth.gpt.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bigmouth.gpt.entity.User;
import org.bigmouth.gpt.exceptions.ForbiddenException;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;

/**
 * @author allen
 * @date 2023/6/13
 * @since 1.0.0
 */
@Configuration
public class NeedTokenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        User user = ContextFactory.getLoginUser();

        if (Objects.isNull(user)) {
            throw new ForbiddenException("抱歉，需要登录才能使用这个功能。");
        }
        return true;
    }
}
