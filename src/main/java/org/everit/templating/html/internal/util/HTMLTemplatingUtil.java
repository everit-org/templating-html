/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.biz)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.templating.html.internal.util;

import org.everit.templating.util.CompileException;
import org.htmlparser.Tag;
import org.htmlparser.lexer.Page;
import org.htmlparser.lexer.PageAttribute;

/**
 * Util functions for HTML templating.
 */
public final class HTMLTemplatingUtil {

  private static final String ESCAPED_GT = "&gt;";

  private static final String ESCAPED_LT = "&lt;";

  private static final String ESCAPED_QUOT = "&quot;";

  private static final String ESCAPED_APOS = "&apos;";

  private static final String ESCAPED_AMP = "&amp;";

  /**
   * Checks whether the value of an attribute is equal to the expected in the way that the value of
   * the attribute might be quoted with quotes or apostrophes.
   *
   * @param expectedValue
   *          The expected value.
   * @param attributeValue
   *          The value of the attribute that might be quoted.
   * @return true if the attribute value is equals with expected value.
   */
  public static boolean attributeConstantEquals(final String expectedValue,
      final String attributeValue) {
    String normalizedAttributeValue = attributeValue;
    if (normalizedAttributeValue == null) {
      return false;
    }
    normalizedAttributeValue = normalizedAttributeValue.trim();

    if ((normalizedAttributeValue.startsWith("\"") && normalizedAttributeValue.endsWith("\""))
        || (normalizedAttributeValue.startsWith("'") && normalizedAttributeValue.endsWith("'"))) {
      normalizedAttributeValue = normalizedAttributeValue
          .substring(1, normalizedAttributeValue.length() - 1);
    } else {
      return false;
    }

    if (normalizedAttributeValue.equalsIgnoreCase(expectedValue)) {
      return true;
    }
    return false;
  }

  /**
   * Calculates the coordinate within a page based on an initial offset.
   * 
   * @param page
   *          The HTML/XML page.
   * @param cursor
   *          The cursor on the page.
   * @param coordinateOffset
   *          The offset of the cursor.
   * @return The calculated coordinate.
   */
  public static Coordinate calculateCoordinate(final Page page, final int cursor,
      final Coordinate coordinateOffset) {
    int row = page.row(cursor);
    int column = page.column(cursor) + (row == 0 ? coordinateOffset.column : 1);
    row = row + coordinateOffset.row;
    return new Coordinate(row, column);

  }

  /**
   * Escapes special characters (and, lt, gt, quot, apos) within the text.
   * 
   * @param textString
   *          The text that should be escaped.
   * @return The escaped text.
   */
  public static String escape(final String textString) {
    StringBuilder sb = new StringBuilder(textString.length());
    for (int i = 0, n = textString.length(); i < n; i++) {
      char charAt = textString.charAt(i);
      switch (charAt) {
        case '&':
          sb.append(ESCAPED_AMP);
          break;
        case '\'':
          sb.append(ESCAPED_APOS);
          break;
        case '"':
          sb.append(ESCAPED_QUOT);
          break;
        case '<':
          sb.append(ESCAPED_LT);
          break;
        case '>':
          sb.append(ESCAPED_GT);
          break;
        default:
          sb.append(charAt);
          break;
      }
    }
    return sb.toString();
  }

  /**
   * Generates a String that contains the specified character N times.
   * 
   * @param c
   *          The character that will be repeated.
   * @param n
   *          The length of the generated String.
   * @return The String that contains c character n times.
   */
  public static String repeatChar(final char c, final int n) {
    char[] chars = new char[n];
    for (int i = 0; i < n; i++) {
      chars[i] = c;
    }
    return new String(chars);
  }

  /**
   * Throws an exception for an eht attribute with all information that can be useful for the
   * programmer.
   * 
   * @param message
   *          The message that should be part of the exception.
   * @param tag
   *          The tag that the attribute belongs to.
   * @param attribute
   *          The attribute that indicated the error.
   * @param positionOfAttributeValue
   *          The position of the attribute.
   * @param startPosition
   *          The starting position of the template.
   */
  public static void throwCompileExceptionForAttribute(final String message, final Tag tag,
      final PageAttribute attribute,
      final boolean positionOfAttributeValue, final Coordinate startPosition) {
    Page page = tag.getPage();
    int tagStartPosition = tag.getStartPosition();
    int tagEndPosition = tag.getEndPosition();

    char[] expr = new char[tagEndPosition - tagStartPosition];
    page.getText(expr, 0, tagStartPosition, tagEndPosition);

    int positionInPage = tagStartPosition + 1;
    int cursor = 1;
    if (attribute != null) {
      if (positionOfAttributeValue) {
        positionInPage = attribute.getValueStartPosition();
      } else {
        positionInPage = attribute.getNameStartPosition();
      }
      cursor = positionInPage - tagStartPosition;

    }

    CompileException e = new CompileException(message, expr, cursor);

    Coordinate position = HTMLTemplatingUtil.calculateCoordinate(page, positionInPage,
        startPosition);
    e.setColumn(position.column);
    e.setLineNumber(position.row);
    throw e;

  }

  /**
   * Unescapes special expressions (amp, quot, apostrophe, gt, lt) of a text.
   * 
   * @param text
   *          The text that will be unescaped.
   * @return The unescaped text.
   */
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
        if (text.startsWith(ESCAPED_AMP, i)) {
          result.append('&');
          i += ESCAPED_AMP.length();
        } else if (text.startsWith(ESCAPED_APOS, i)) {
          result.append('\'');
          i += ESCAPED_APOS.length();
        } else if (text.startsWith(ESCAPED_QUOT, i)) {
          result.append('"');
          i += ESCAPED_QUOT.length();
        } else if (text.startsWith(ESCAPED_LT, i)) {
          result.append('<');
          i += ESCAPED_LT.length();
        } else if (text.startsWith(ESCAPED_GT, i)) {
          result.append('>');
          i += ESCAPED_GT.length();
        } else {
          result.append(charAt);
          i++;
        }
      }
    }
    return result.toString();
  }

  private HTMLTemplatingUtil() {
  }
}
