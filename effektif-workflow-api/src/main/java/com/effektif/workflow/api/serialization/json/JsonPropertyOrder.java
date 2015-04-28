/* Copyright (c) 2014, Effektif GmbH.
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
package com.effektif.workflow.api.serialization.json;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * Specifies the ordering for model object properties to use for JSON serialisation, i.e. the order of fields in the
 * generated JSON.
 *
 * @author Tom Baeyens
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonPropertyOrder {

  String[] value();
}
