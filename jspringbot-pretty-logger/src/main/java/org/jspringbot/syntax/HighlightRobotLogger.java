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

package org.jspringbot.syntax;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.WordUtils;
import org.jspringbot.JSpringBotLogger;

/**
 * Add highlight support.
 */
public class HighlightRobotLogger extends JSpringBotLogger {

    public static final int WORD_WRAP_LENGTH = 120;


    public static HighlightRobotLogger getLogger(Class clazz) {
        return new HighlightRobotLogger(clazz);
    }

    protected HighlightRobotLogger(Class logger) {
        super(logger);
    }

    public HtmlAppender createAppender() {
        return new HtmlAppender(this);
    }

    public KeywordAppender keywordAppender() {
        if(HighlightKeywordLogger.appender() == null) {
            return new KeywordAppender(null);
        }

        return new KeywordAppender(HighlightKeywordLogger.appender());
    }

    public static class HtmlAppender {
        private StringBuilder buf = new StringBuilder();

        private StringBuilder code = new StringBuilder();

        private StringBuilder properties = new StringBuilder();

        private StringBuilder xml = new StringBuilder();

        private StringBuilder sql = new StringBuilder();

        private StringBuilder text = new StringBuilder();

        private StringBuilder json = new StringBuilder();

        private StringBuilder javascript = new StringBuilder();

        private StringBuilder css = new StringBuilder();

        private JSpringBotLogger logger;

        private boolean silent = false;

        HtmlAppender(JSpringBotLogger logger) {
            this.logger = logger;
        }

        public boolean isSilent() {
            return silent;
        }

        public void setSilent(boolean silent) {
            this.silent = silent;
        }

        public HtmlAppender append(String msg, Object... args) {
            if(isSilent()) {
                return this;
            }

            if(args != null && args.length > 0) {
                buf.append(String.format(msg, args));
            } else {
                buf.append(msg);
            }

            return this;
        }

        public HtmlAppender appendBold(String msg, Object... args) {
            if(isSilent()) {
                return this;
            }

            buf.append("<b>");
            append(msg, args);
            buf.append("</b>");

            return this;
        }

        public HtmlAppender appendPropertyStringArray(String name, String[] values) {
            if(isSilent()) {
                return this;
            }

            for(int i = 0; i < values.length; i++) {
                appendProperty(name + "[" + i + "]", values[i]);
            }

            return this;
        }

        private String hardWordWrap(String str) {
            return WordUtils.wrap(str, WORD_WRAP_LENGTH, "\n    ", true);
        }

        public HtmlAppender appendArguments(Object[] params) {
            if(isSilent()) {
                return this;
            }

            if(params == null) {
                appendProperty("Keyword Arguments", null);

                return this;
            } else if(params.length == 0) {
                append("Keyword Arguments", "Array length is 0");

                return this;
            }

            for(int i = 0; i < params.length; i++) {
                if(params[i] != null) {
                    appendProperty("Keyword Argument Class [" + i + "]", params[i].getClass().getName());
                }

                appendProperty("Keyword Argument Value [" + i + "]", params[i]);
            }

            return this;
        }

        public HtmlAppender appendProperty(String property, Object value) {
            if(isSilent()) {
                return this;
            }

            if(properties.length() > 0) {
                properties.append("\n");
            }

            if(Number.class.isInstance(value)) {
                if(Long.class.isInstance(value) || Integer.class.isInstance(value) || Byte.class.isInstance(value) || Short.class.isInstance(value)) {
                    properties.append(hardWordWrap(String.format("%s = %d", property, ((Number) value).longValue())));
                } else {
                    properties.append(hardWordWrap(String.format("%s = %f", property, ((Number) value).doubleValue())));
                }
            } else if(Boolean.class.isInstance(value)) {
                properties.append(hardWordWrap(String.format("%s = %s", property, String.valueOf(value))));
            } else if(Character.class.isInstance(value)) {
                properties.append(hardWordWrap(String.format("%s = '%s'", property, String.valueOf(value))));
            } else {
                properties.append(hardWordWrap(String.format("%s = \"%s\"", property, StringEscapeUtils.escapeJava(String.valueOf(value)))));
            }

            return this;
        }

        public HtmlAppender appendText(String msg, Object... args) {
            if(isSilent()) {
                return this;
            }

            if(args != null && args.length > 0) {
                text.append(String.format(msg, args));
            } else {
                text.append(msg);
            }

            return this;
        }

        public HtmlAppender appendCode(String msg, Object... args) {
            if(isSilent()) {
                return this;
            }

            if(args != null && args.length > 0) {
                code.append(String.format(msg, args));
            } else {
                code.append(msg);
            }

            return this;
        }

        public HtmlAppender appendXML(String msg, Object... args) {
            if(isSilent()) {
                return this;
            }

            if(args != null && args.length > 0) {
                xml.append(String.format(msg, args));
            } else {
                xml.append(msg);
            }

            return this;
        }

        public HtmlAppender appendSQL(String msg, Object... args) {
            if(isSilent()) {
                return this;
            }

            if(args != null && args.length > 0) {
                sql.append(String.format(msg, args));
            } else {
                sql.append(msg);
            }

            return this;
        }

        public HtmlAppender appendJavascript(String msg, Object... args) {
            if(isSilent()) {
                return this;
            }

            if(args != null && args.length > 0) {
                javascript.append(String.format(msg, args));
            } else {
                javascript.append(msg);
            }

            return this;
        }

        public HtmlAppender appendCss(String msg, Object... args) {
            if(isSilent()) {
                return this;
            }

            if(args != null && args.length > 0) {
                css.append(String.format(msg, args));
            } else {
                css.append(msg);
            }

            return this;
        }

        public HtmlAppender appendJSON(String msg, Object... args) {
            if(isSilent()) {
                return this;
            }

            if(args != null && args.length > 0) {
                json.append(String.format(msg, args));
            } else {
                json.append(msg);
            }

            return this;
        }

        public void log() {
            if(isSilent()) {
                return;
            }

            if(code.length() > 0) {
                buf.append(HighlighterUtils.INSTANCE.highlightNormal(code.toString()));
            }

            if(properties.length() > 0) {
                buf.append(HighlighterUtils.INSTANCE.highlightText(properties.toString()));
            }

            if(text.length() > 0) {
                buf.append(HighlighterUtils.INSTANCE.highlightText(text.toString()));
            }

            if(xml.length() > 0) {
                buf.append(HighlighterUtils.INSTANCE.highlightXML(xml.toString()));
            }

            if(sql.length() > 0) {
                buf.append(HighlighterUtils.INSTANCE.highlightSQL(sql.toString()));
            }

            if(javascript.length() > 0) {
                buf.append(HighlighterUtils.INSTANCE.highlightJavascript(javascript.toString()));
            }

            if(css.length() > 0) {
                buf.append(HighlighterUtils.INSTANCE.highlightCss(css.toString()));
            }

            if(json.length() > 0) {
                buf.append(HighlighterUtils.INSTANCE.highlightJSON(json.toString()));
            }

            logger.pureHtml(buf.toString());
        }
    }
}
