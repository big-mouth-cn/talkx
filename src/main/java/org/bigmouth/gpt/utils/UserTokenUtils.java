package org.bigmouth.gpt.utils;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * @author allen
 * @date 2023/6/13
 * @since 1.0.0
 */
public class UserTokenUtils {

    public static final String HEADER_TALKX_TOKEN = "talkx-token";

    public static String getToken() {
        HttpServletRequest request = getHttpServletRequest();
        return getToken(request);
    }

    public static String getToken(HttpServletRequest request) {
        String talkxToken = request.getHeader(HEADER_TALKX_TOKEN);
        if (talkxToken != null && !talkxToken.isEmpty()) {
            // 对于获取到的 talkx-token 进行安全性检查和编码规范处理
            // 比如可以使用正则表达式检查是否符合特定的格式要求，或者进行 URL 解码等处理
            // 这里简单地返回获取到的 talkx-token
            return talkxToken;
        } else {
            return null;
        }
    }

    private static HttpServletRequest getHttpServletRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return Objects.requireNonNull(requestAttributes).getRequest();
    }

    private static HttpServletResponse getHttpServletResponse() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return Objects.requireNonNull(requestAttributes).getResponse();
    }
}
