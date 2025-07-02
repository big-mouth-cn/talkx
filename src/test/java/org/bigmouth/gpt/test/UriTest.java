package org.bigmouth.gpt.test;

import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Allen Hu
 * @date 2025/4/21
 */
public class UriTest {

    public static void main(String[] args) {
        System.out.println(UriComponentsBuilder.fromUriString("https://api.aigateway.work").build().getPath());
        System.out.println(UriComponentsBuilder.fromUriString("https://api.aigateway.work/").build().getPath());
        System.out.println(UriComponentsBuilder.fromUriString("https://api.aigateway.work/v1").build().getPath());
        System.out.println(UriComponentsBuilder.fromUriString("https://api.aigateway.work/v1/chat/completions").build().getPath());
    }
}
