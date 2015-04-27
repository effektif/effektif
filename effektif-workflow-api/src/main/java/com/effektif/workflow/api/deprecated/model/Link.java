package com.effektif.workflow.api.deprecated.model;/* Copyright (c) 2015, Effektif GmbH.
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
 * limitations under the License. */

/**
 * A named hyperlink.
 *
 * @author Peter Hilton
 */
public class Link {

  protected String name;

  public String getName() {
    return this.name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public Link name(String name) {
    this.name = name;
    return this;
  }

  protected String url;

  public String getUrl() {
    return this.url;
  }
  public void setUrl(String url) {
    this.url = url;
  }
  public Link url(String url) {
    this.url = url;
    return this;
  }
}
