package org.bigmouth.gpt.interceptor;

import com.bxm.warcar.utils.IpHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bigmouth.gpt.entity.User;
import org.bigmouth.gpt.service.IUserService;
import org.bigmouth.gpt.utils.UserTokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;

/**
 * @author huxiao
 * @date 2023/9/21
 * @since 1.0.0
 */
@Configuration
public class ContextInitInterceptor implements HandlerInterceptor, Ordered {

    private IUserService userService;

    @Autowired
    public void setUserService(IUserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Context context = ContextFactory.get();

        // 设置sessionId
        String sessionId = request.getSession().getId();
        context.setSessionId(sessionId);

        // 设置IP
        String ip = IpHelper.getIpFromHeader(request);
        context.setIp(ip);

        // 总是查询用户最新的信息
        User user = userService.getUserFromToken(UserTokenUtils.getToken());
        if (Objects.nonNull(user)) {
            User u = userService.getById(user.getId());
            context.setUser(u);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        ContextFactory.remove();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
