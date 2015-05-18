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
package org.everit.templating.html.internal;

import org.everit.templating.html.internal.util.Coordinate;
import org.everit.templating.html.internal.util.HTMLTemplatingUtil;
import org.htmlparser.lexer.Page;
import org.htmlparser.lexer.PageAttribute;

/**
 * Additional information about a pageAttribute.
 */
public class AttributeInfo {

  public final TagInfo tagInfo;

  /**
   * Starting cursor position of the value of the attribute within the template.
   */
  public final int valueCursorInTag;

  public final Coordinate valueStartCoordinate;

  /**
   * Constructor.
   */
  public AttributeInfo(final PageAttribute pageAttribute, final TagInfo tagInfo,
      final Coordinate templateStartCoordinate) {
    this.tagInfo = tagInfo;

    int valueStartPosition = pageAttribute.getValueStartPosition();
    valueCursorInTag = valueStartPosition - tagInfo.startPosition;

    Page page = pageAttribute.getPage();
    valueStartCoordinate = HTMLTemplatingUtil
        .calculateCoordinate(page, valueStartPosition, templateStartCoordinate);

  }
}
