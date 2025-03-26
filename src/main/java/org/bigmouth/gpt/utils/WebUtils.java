package org.bigmouth.gpt.utils;


import org.apache.commons.lang3.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;

/**
 * @ClassName WebUtil.java
 * @Description 功能描述： 浏览器相关方法
 */
public class WebUtils {

    // 编码
    private static final String ECODING = "UTF-8";


    /**
     * 说明方法描述：获取User-Agent信息
     *
     * @param request
     * @return
     * @throws Exception
     * @author leon
     */
    public static String getUserAgent(HttpServletRequest request) {
        try {
            return request.getHeader("User-Agent");
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown";
        }
    }


    /**
     * 说明方法描述：获取用户访问的ip
     *
     * @param request
     * @return
     */
    public static String getIpAddr(HttpServletRequest request) {
        if (null == request) {
            return null;
        } else {
            String ip = request.getHeader("X-Forwarded-For");
            if (StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
                return ip.indexOf(44) == -1 ? ip : ip.substring(0, ip.indexOf(44));
            } else {
                if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("X-Real-IP");
                }

                if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("Proxy-Client-IP");
                }

                if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("WL-Proxy-Client-IP");
                }

                if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }

                return ip;
            }
        }
    }


    public static String getHost(HttpServletRequest request){
        String url = request.getHeader("origin");
        URI uri;
        try {
            uri = URI.create(url);
        } catch (Exception e) {
            return url;
        }
        return uri.getHost();
    }

}
