/**
 * This file is part of Everit - HTML Templating.
 *
 * Everit - HTML Templating is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - HTML Templating is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - HTML Templating.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.templating.html.internal.util;

public class HTMLTemplatingUtil {

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

    public static String repeatChar(final char c, final int times) {
        char[] n = new char[times];
        for (int i = 0; i < times; i++) {
            n[i] = c;
        }
        return new String(n);
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
