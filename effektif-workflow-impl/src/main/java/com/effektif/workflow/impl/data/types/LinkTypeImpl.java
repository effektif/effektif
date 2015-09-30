/*
 * Copyright 2014 Effektif GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.effektif.workflow.impl.data.types;

import java.util.regex.Pattern;

import com.effektif.workflow.api.types.LinkType;

/**
 * @author Peter Hilton
 */
public class LinkTypeImpl extends TextTypeImpl {

  static Pattern validCharacters = Pattern.compile("[-A-Z0-9+&@#/%?=~_|!:,.;]*", Pattern.CASE_INSENSITIVE);

  public LinkTypeImpl() {
    super(LinkType.INSTANCE);
  }

  public LinkTypeImpl(LinkType type) {
    super(type);
  }

  @Override
  public String validateInternalValue(Object internalValue) {
    if (! (internalValue instanceof String)) {
      return "Links must be of type "+String.class.getName();
    }
    return null;
  }
}
