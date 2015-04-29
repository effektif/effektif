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
package com.effektif.workflow.api.json;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * Specifies the JSON field name in case it must be different from the Java model class field name;
 * 
 * @author Tom Baeyens
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonFieldName {

  String value();
}
