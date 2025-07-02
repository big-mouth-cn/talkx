package org.bigmouth.gpt.interceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author tangxiao
 * @date 2023/7/25
 * @since 1.0
 */
@Configuration
public class InterceptorConfigurer implements WebMvcConfigurer {

    private final ContextInitInterceptor contextInitInterceptor;
    private final NeedTokenInterceptor needTokenInterceptor;

    public InterceptorConfigurer(ContextInitInterceptor contextInitInterceptor, NeedTokenInterceptor needTokenInterceptor) {
        this.contextInitInterceptor = contextInitInterceptor;
        this.needTokenInterceptor = needTokenInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(contextInitInterceptor)
                .addPathPatterns("/**");

        registry.addInterceptor(needTokenInterceptor)
                .addPathPatterns("/user/info/**")
                .addPathPatterns("/user/voice/**")
                .addPathPatterns("/coin_bill/**")
                .addPathPatterns("/feedback/**")
                .addPathPatterns("/order/create")
                .addPathPatterns("/friend/**")
                .addPathPatterns("/favorites/**")
                .addPathPatterns("/table_schema/**")
                .addPathPatterns("/session/list")
                .addPathPatterns("/device/**")
                .excludePathPatterns("/friend/list")
                .excludePathPatterns("/friend/plaza")
                .excludePathPatterns("/user/info/list_model")
                ;
    }
}
