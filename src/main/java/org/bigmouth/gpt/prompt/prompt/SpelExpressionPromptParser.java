package org.bigmouth.gpt.prompt.prompt;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Map;

/**
 * @author Allen Hu
 * @date 2025/3/12
 */
public class SpelExpressionPromptParser implements PromptParser {

    public static final String EXPRESSION_PREFIX = "{{";
    public static final String EXPRESSION_SUFFIX = "}}";

    private final ExpressionParser parser = new SpelExpressionParser();

    private final StandardEvaluationContext context = new StandardEvaluationContext();

    public SpelExpressionPromptParser(Map<String, Object> variables) {
        context.setVariables(variables);
    }

    @Override
    public String process(String prompt) {
        return processText0(prompt);
    }

    private String processText0(String inputText) {
        StringBuilder result = new StringBuilder();
        int startIdx = 0;

        while (startIdx < inputText.length()) {
            int exprStart = inputText.indexOf(EXPRESSION_PREFIX, startIdx);
            if (exprStart == -1) {
                result.append(inputText.substring(startIdx));
                break;
            }

            result.append(inputText.substring(startIdx, exprStart));
            int exprEnd = inputText.indexOf(EXPRESSION_SUFFIX, exprStart);
            if (exprEnd == -1) {
                throw new IllegalArgumentException("Unmatched braces in the expression.");
            }

            String expr = inputText.substring(exprStart + 2, exprEnd);
            Expression expression = parser.parseExpression(expr);
            Object value = expression.getValue(context);
            result.append(value);

            startIdx = exprEnd + 2;
        }

        return result.toString();
    }
}
