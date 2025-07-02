package org.bigmouth.gpt.test;

import org.bigmouth.gpt.Application;
import org.bigmouth.gpt.entity.AiModel;

public class ApplicationTest {
    public static void main(String[] args) {
        // Test case 1: No proxy, with model URL
        AiModel model1 = new AiModel();
        model1.setRequestUrl("https://api.model1.com/v1");
        String defaultUrl1 = "https://api.default.com/v1";
        String result1 = Application.getRequestUri(model1, defaultUrl1);
        System.out.println("Test 1 - Model URL should be used:");
        System.out.println("Expected: https://api.model1.com/v1");
        System.out.println("Actual: " + result1);
        System.out.println();

        // Test case 2: No proxy, no model URL
        AiModel model2 = new AiModel();
        String defaultUrl2 = "https://api.default.com/v1";
        String result2 = Application.getRequestUri(model2, defaultUrl2);
        System.out.println("Test 2 - Default URL should be used:");
        System.out.println("Expected: https://api.default.com/v1");
        System.out.println("Actual: " + result2);
        System.out.println();

        // Test case 3: With proxy
        AiModel model3 = new AiModel();
        model3.setRequestUrl("https://api.model3.com/v1/chat");
        String defaultUrl3 = "https://api.default.com/v1";
        String proxyUrl = "https://proxy.example.com:8080";
        String result3 = Application.getRequestUri(model3, defaultUrl3, proxyUrl);
        System.out.println("Test 3 - Proxy URL should be used with original path:");
        System.out.println("Expected: https://proxy.example.com:8080/v1/chat");
        System.out.println("Actual: " + result3);
        System.out.println();

        // Test case 4: With proxy having custom path
        AiModel model4 = new AiModel();
        model4.setRequestUrl("https://api.model4.com/v1/chat");
        String defaultUrl4 = "https://api.default.com/v1";
        String proxyUrlWithPath = "https://proxy.example.com:8080/custom/path";
        String result4 = Application.getRequestUri(model4, defaultUrl4, proxyUrlWithPath);
        System.out.println("Test 4 - Proxy URL should be used with custom path:");
        System.out.println("Expected: https://proxy.example.com:8080/custom/path");
        System.out.println("Actual: " + result4);
    }
} 