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


/**
 * Indicates that a model class can be serialised to JSON, using one of the {@link JsonWriter} implementations
 * (for the Jackson or MongoDB JSON model). Using this abstract API makes this model serialisation independent of the
 * concrete {@link JsonWriter} serialisation.
 *
 * @author Tom Baeyens
 */
public interface JsonWritable {

  void writeJson(JsonWriter w);

}
