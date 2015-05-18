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

import org.htmlparser.Tag;
import org.htmlparser.lexer.Page;

/**
 * Evaluated information of a HTML tag.
 */
public class TagInfo {

  public final char[] chars;

  public final int startPosition;

  /**
   * Constructor.
   *
   * @param tag
   *          The tag that we want to store information about.
   */
  public TagInfo(final Tag tag) {
    Page page = tag.getPage();
    startPosition = tag.getStartPosition();
    int endPosition = tag.getEndPosition();
    chars = new char[endPosition - startPosition];
    page.getText(chars, 0, startPosition, endPosition);
  }
}
