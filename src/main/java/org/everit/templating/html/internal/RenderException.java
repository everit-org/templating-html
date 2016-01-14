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

import org.everit.templating.util.CompileException;

/**
 * Exception during rendering the template.
 */
public class RenderException extends CompileException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructor.
   *
   * @param message
   *          The message of the expression.
   * @param attributeInfo
   *          The attribute that had an expression that initiated this exception.
   */
  public RenderException(final String templateFileName, final String message,
      final AttributeInfo attributeInfo) {
    super("[Name: " + templateFileName + "] " + message,
        attributeInfo.tagInfo.chars, attributeInfo.valueCursorInTag);
    setColumn(attributeInfo.valueStartCoordinate.column);
    setLineNumber(attributeInfo.valueStartCoordinate.row);
  }
}
