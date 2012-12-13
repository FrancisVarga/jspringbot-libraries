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

    public class HtmlAppender {
        private StringBuilder buf = new StringBuilder();

        private StringBuilder code = new StringBuilder();

        private StringBuilder properties = new StringBuilder();

        private StringBuilder xml = new StringBuilder();

        private StringBuilder sql = new StringBuilder();

        private StringBuilder text = new StringBuilder();

        private StringBuilder json = new StringBuilder();


        private JSpringBotLogger logger;

        private HtmlAppender(JSpringBotLogger logger) {
            this.logger = logger;
        }


        public HtmlAppender append(String msg, Object... args) {
            if(args != null && args.length > 0) {
                buf.append(String.format(msg, args));
            } else {
                buf.append(msg);
            }

            return this;
        }

        public HtmlAppender appendBold(String msg, Object... args) {
            buf.append("<b>");
            append(msg, args);
            buf.append("</b>");

            return this;
        }

        public HtmlAppender appendPropertyStringArray(String name, String[] values) {
            for(int i = 0; i < values.length; i++) {
                appendProperty(name + "[" + i + "]", values[i]);
            }

            return this;
        }

        private String hardWordWrap(String str) {
            return WordUtils.wrap(str, WORD_WRAP_LENGTH, "\n    ", true);
        }

        public HtmlAppender appendProperty(String property, Object value) {
            if(properties.length() > 0) {
                properties.append("\n");
            }
            properties.append(hardWordWrap(String.format("%s = \"%s\"", property, StringEscapeUtils.escapeJava(String.valueOf(value)))));

            return this;
        }

        public HtmlAppender appendText(String msg, Object... args) {
            if(args != null && args.length > 0) {
                text.append(String.format(msg, args));
            } else {
                text.append(msg);
            }

            return this;
        }

        public HtmlAppender appendCode(String msg, Object... args) {
            if(args != null && args.length > 0) {
                code.append(String.format(msg, args));
            } else {
                code.append(msg);
            }

            return this;
        }

        public HtmlAppender appendXML(String msg, Object... args) {
            if(args != null && args.length > 0) {
                xml.append(String.format(msg, args));
            } else {
                xml.append(msg);
            }

            return this;
        }

        public HtmlAppender appendSQL(String msg, Object... args) {
            if(args != null && args.length > 0) {
                sql.append(String.format(msg, args));
            } else {
                sql.append(msg);
            }

            return this;
        }

        public HtmlAppender appendJSON(String msg, Object... args) {
            if(args != null && args.length > 0) {
                json.append(String.format(msg, args));
            } else {
                json.append(msg);
            }

            return this;
        }

        public void log() {
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

            if(json.length() > 0) {
                buf.append(HighlighterUtils.INSTANCE.highlightJSON(json.toString()));
            }

            logger.pureHtml(buf.toString());
        }
    }
}
