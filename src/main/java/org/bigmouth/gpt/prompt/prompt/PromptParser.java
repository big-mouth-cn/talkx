package org.bigmouth.gpt.prompt.prompt;

public interface PromptParser {

    /**
     * 将输入的prompt解析为实际需要发送的prompt
     * @param prompt 用户输入的prompt
     * @return 解析后的prompt
     * @throws IllegalArgumentException 输入的prompt 不合法时抛出异常
     */
    String process(String prompt) throws IllegalArgumentException;
}
