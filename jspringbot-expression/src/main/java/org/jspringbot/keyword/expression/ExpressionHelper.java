/*
 * Copyright (c) 2012. JSpringBot. All Rights Reserved.
 *
 * See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The JSpringBot licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jspringbot.keyword.expression;

import de.odysseus.el.TreeValueExpression;
import org.jspringbot.keyword.expression.engine.DefaultELContext;
import org.jspringbot.keyword.expression.engine.function.SupportedFunctionsManager;
import org.jspringbot.keyword.expression.plugin.ExpressionHandler;
import org.jspringbot.keyword.expression.plugin.ExpressionHandlerManager;
import org.jspringbot.keyword.expression.plugin.VariableProviderManager;
import org.jspringbot.syntax.HighlightRobotLogger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.el.ExpressionFactory;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionHelper implements ApplicationContextAware {

    public static final HighlightRobotLogger LOG = HighlightRobotLogger.getLogger(ExpressionHelper.class);

    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("\\#\\{(.*)\\}", Pattern.CASE_INSENSITIVE);

    private static final Pattern PREFIX_EXPRESSION_PATTERN = Pattern.compile("([a-z]+)\\:(.*)", Pattern.CASE_INSENSITIVE);

    private ExpressionFactory factory;

    private SupportedFunctionsManager functionManager;

    private ExpressionHandlerManager expressionManager;

    private VariableProviderManager variableManager;

    public ExpressionHelper(ExpressionFactory factory) {
        this.factory = factory;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        functionManager = new SupportedFunctionsManager(context);
        expressionManager = new ExpressionHandlerManager(context, new ELExpressionHandler());
        variableManager = new VariableProviderManager(context);
    }

    public void evaluationShouldBe(String expression, Object expected) throws Exception {
        LOG.keywordAppender()
                .appendProperty("Base Expression", expression)
                .appendProperty("Expected Result", expected);

        Object value = evaluate(expression);

        if(!expected.equals(value)) {
            throw new IllegalArgumentException("Evaluation was not expected.");
        }
    }

    public void evaluationShouldBeNull(String expression) throws Exception {
        LOG.keywordAppender()
                .appendProperty("Base Expression", expression);

        Object value = evaluate(expression);

        if(value != null) {
            throw new IllegalArgumentException("Evaluation is not null.");
        }
    }

    public void evaluationShouldNotBeNull(String expression) throws Exception {
        LOG.keywordAppender()
                .appendProperty("Base Expression", expression);

        Object value = evaluate(expression);

        if(value == null) {
            throw new IllegalArgumentException("Evaluation is null.");
        }
    }

    public void evaluationShouldBeTrue(String expression) throws Exception {
        LOG.keywordAppender().appendProperty("Base Expression", expression);

        Object value = evaluate(expression);

        if(value == null) {
            throw new IllegalArgumentException("Evaluation was not true.");
        }

        if(!Boolean.class.isInstance(value)) {
            throw new IllegalArgumentException("Evaluation was not true.");
        }

        if(!Boolean.TRUE.equals(value)) {
            throw new IllegalArgumentException("Evaluation was not true.");
        }
    }

    public void evaluationShouldBeFalse(String expression) throws Exception {
        LOG.keywordAppender().appendProperty("Base Expression", expression);

        Object value = evaluate(expression);

        if(value == null) {
            throw new IllegalArgumentException("Evaluation was not false.");
        }

        if(!Boolean.class.isInstance(value)) {
            throw new IllegalArgumentException("Evaluation was not false.");
        }

        if(!Boolean.FALSE.equals(value)) {
            throw new IllegalArgumentException("Evaluation was not false.");
        }
    }

    public Object evaluate(String expression) throws Exception {
        LOG.keywordAppender().appendProperty("Base Expression", expression);

        Matcher matcher = EXPRESSION_PATTERN.matcher(expression);

        if(!matcher.find()) {
            throw new IllegalArgumentException("Invalid expression format.");
        }

        String content = matcher.group(1);

        Matcher prefixMatcher = PREFIX_EXPRESSION_PATTERN.matcher(content);
        if(prefixMatcher.matches()) {
            String prefix = prefixMatcher.group(1);
            String prefixContent = prefixMatcher.group(2);

            return expressionManager.evaluation(prefix, prefixContent);
        } else {
            return expressionManager.defaultEvaluation(content);
        }
    }

    public boolean isSupported(String expression) {
        Matcher matcher = EXPRESSION_PATTERN.matcher(expression);

        return matcher.matches();
    }

    private Map<String, Object> getVariables() {
        return variableManager.getVariables();
    }

    public class ELExpressionHandler implements ExpressionHandler {

        @Override
        public String getPrefix() {
            throw new UnsupportedOperationException("This is the default handler. This method should not be called.");
        }

        private String dump(TreeValueExpression expression) throws IOException {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            expression.dump(pw);
            pw.close();
            sw.close();

            return sw.toString();
        }

        @Override
        public Object evaluate(String expression) throws Exception {
            DefaultELContext context = new DefaultELContext(functionManager, getVariables());
            TreeValueExpression expr = (TreeValueExpression) factory.createValueExpression(context, String.format("${%s}", expression), Object.class);

            Object result = expr.getValue(context);

            LOG.keywordAppender().appendProperty("EL Expression", expression);
            LOG.keywordAppender().appendProperty("EL Evaluation Result", result);

            return result;
        }
    }
}
