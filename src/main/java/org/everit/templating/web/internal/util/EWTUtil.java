/**
 * This file is part of Everit - Web Templating.
 *
 * Everit - Web Templating is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Web Templating is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Web Templating.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.templating.web.internal.util;

import org.everit.expression.AbstractExpressionException;

public class EWTUtil {

    public static boolean attributeConstantEquals(final String expectedAttributeValue, String currentAttributeValue) {
        if (currentAttributeValue == null) {
            return false;
        }
        currentAttributeValue = currentAttributeValue.trim();

        if ((currentAttributeValue.startsWith("\"") && currentAttributeValue.endsWith("\""))
                || (currentAttributeValue.startsWith("'") && currentAttributeValue.endsWith("'"))) {
            currentAttributeValue = currentAttributeValue.substring(1, currentAttributeValue.length() - 1);
        }

        if (currentAttributeValue.equalsIgnoreCase(expectedAttributeValue)) {
            return true;
        }
        return false;
    }

    /**
     * This is an important aspect of the core parser tools. This method is used throughout the core parser and
     * sub-lexical parsers to capture a balanced capture between opening and terminating tokens such as:
     * <em>( [ { ' " </em> <br>
     * <br>
     * For example: ((foo + bar + (bar - foo)) * 20;<br>
     * <br>
     * <p/>
     * If a balanced capture is performed from position 2, we get "(foo + bar + (bar - foo))" back.<br>
     * If a balanced capture is performed from position 15, we get "(bar - foo)" back.<br>
     * Etc.
     *
     * @param chars
     *            -
     * @param start
     *            -
     * @param type
     *            -
     * @return -
     */
    public static int balancedCapture(final char[] chars, final int start, final char type) {
        return balancedCapture(chars, start, chars.length, type);
    }

    public static int balancedCapture(final char[] chars, int start, final int end, final char type) {
        int depth = 1;
        char term = type;
        switch (type) {
        case '[':
            term = ']';
            break;
        case '{':
            term = '}';
            break;
        case '(':
            term = ')';
            break;
        }

        if (type == term) {
            for (start++; start < end; start++) {
                if (chars[start] == type) {
                    return start;
                }
            }
        }
        else {
            for (start++; start < end; start++) {
                if (start < end && chars[start] == '/') {
                    if (start + 1 == end) {
                        return start;
                    }
                    if (chars[start + 1] == '/') {
                        start++;
                        while (start < end && chars[start] != '\n') {
                            start++;
                        }
                    }
                    else if (chars[start + 1] == '*') {
                        start += 2;
                        SkipComment: while (start < end) {
                            switch (chars[start]) {
                            case '*':
                                if (start + 1 < end && chars[start + 1] == '/') {
                                    break SkipComment;
                                }
                            case '\r':
                            case '\n':

                                break;
                            }
                            start++;
                        }
                    }
                }
                if (start == end) {
                    return start;
                }
                if (chars[start] == '\'' || chars[start] == '"') {
                    start = captureStringLiteral(chars[start], chars, start, end);
                }
                else if (chars[start] == type) {
                    depth++;
                }
                else if (chars[start] == term && --depth == 0) {
                    return start;
                }
            }
        }

        switch (type) {
        case '[':
            throw new AbstractExpressionException("unbalanced braces [ ... ]", chars, start);
        case '{':
            throw new AbstractExpressionException("unbalanced braces { ... }", chars, start);
        case '(':
            throw new AbstractExpressionException("unbalanced braces ( ... )", chars, start);
        default:
            throw new AbstractExpressionException("unterminated string literal", chars, start);
        }
    }

    public static int captureStringLiteral(final char type, final char[] expr, int cursor, final int end) {
        while (++cursor < end && expr[cursor] != type) {
            if (expr[cursor] == '\\') {
                cursor++;
            }
        }

        if (cursor >= end || expr[cursor] != type) {
            throw new AbstractExpressionException("unterminated string literal", expr, cursor);
        }

        return cursor;
    }

    public static String createStringTrimmed(final char[] s, int start, int length) {
        if ((length = start + length) > s.length) {
            return new String(s);
        }
        while (start != length && s[start] < '\u0020' + 1) {
            start++;
        }
        while (length != start && s[length - 1] < '\u0020' + 1) {
            length--;
        }
        return new String(s, start, length - start);
    }

    public static String escape(final String textString) {
        StringBuilder sb = new StringBuilder(textString.length());
        for (int i = 0, n = textString.length(); i < n; i++) {
            char charAt = textString.charAt(i);
            switch (charAt) {
            case '&':
                sb.append("&amp;");
                break;
            case '\'':
                sb.append("&apos;");
                break;
            case '"':
                sb.append("&quot;");
                break;
            case '<':
                sb.append("&lt;");
                break;
            case '>':
                sb.append("&gt;");
                break;
            default:
                sb.append(charAt);
                break;
            }
        }
        return sb.toString();
    }

    public static boolean isWhitespace(final char c) {
        return c < '\u0020' + 1;
    }

    public static String repeatChar(final char c, final int times) {
        char[] n = new char[times];
        for (int i = 0; i < times; i++) {
            n[i] = c;
        }
        return new String(n);
    }

    public static char[] subset(final char[] array, final int start, final int length) {

        char[] newArray = new char[length];

        for (int i = 0; i < newArray.length; i++) {
            newArray[i] = array[i + start];
        }

        return newArray;
    }

    public static String unescape(final String text) {
        StringBuilder result = new StringBuilder(text.length());
        int i = 0;
        int n = text.length();
        while (i < n) {
            char charAt = text.charAt(i);
            if (charAt != '&') {
                result.append(charAt);
                i++;
            } else {
                if (text.startsWith("&amp;", i)) {
                    result.append('&');
                    i += 5;
                } else if (text.startsWith("&apos;", i)) {
                    result.append('\'');
                    i += 6;
                } else if (text.startsWith("&quot;", i)) {
                    result.append('"');
                    i += 6;
                } else if (text.startsWith("&lt;", i)) {
                    result.append('<');
                    i += 4;
                } else if (text.startsWith("&gt;", i)) {
                    result.append('>');
                    i += 4;
                }
            }
        }
        return result.toString();
    }
}
