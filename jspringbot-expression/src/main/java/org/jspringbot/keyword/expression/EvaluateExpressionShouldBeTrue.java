package org.jspringbot.keyword.expression;

import org.jspringbot.KeywordInfo;
import org.springframework.stereotype.Component;

@Component
@KeywordInfo(
        name = "Evaluate Expression Should Be False",
        parameters = {"expression"},
        description = "classpath:desc/EvaluateExpression.txt"
)
public class EvaluateExpressionShouldBeTrue extends AbstractExpressionKeyword {
    @Override
    protected Object executeInternal(Object[] params) throws Exception {
        helper.evaluationShouldBeTrue(String.valueOf(params[0]));

        return null;
    }
}
