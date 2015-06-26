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
package com.effektif.workflow.impl.json.types;

import java.lang.reflect.Type;

import com.effektif.workflow.api.model.ValueConverter;
import com.effektif.workflow.api.model.ValueConverter.Converter;
import com.effektif.workflow.impl.data.InvalidValueException;
import com.effektif.workflow.impl.json.JsonReader;
import com.effektif.workflow.impl.json.JsonWriter;


/**
 * Maps a {@link Number} to a JSON number value for serialisation and deserialisation.
 *
 * @author Tom Baeyens
 */
public class NumberMapper extends AbstractTypeMapper<Number> {

  Class<? extends Number> numberClass;
  Converter<Number, ? extends Number> converter;
  
  public NumberMapper(Type type) {
    numberClass = (Class< ? extends Number>) type;
    converter = ValueConverter.findConverter(Number.class, numberClass);
  }

  @Override
  public void write(Number objectValue, JsonWriter jsonWriter) {
    jsonWriter.writeNumber(objectValue);
  }

  @Override
  public Number read(Object jsonValue, JsonReader jsonReader) {
    if (jsonValue==null) {
      return null;
    }
    if (!Number.class.isAssignableFrom(jsonValue.getClass())) {
      throw new InvalidValueException(String.format("Invalid numeric value ‘%s’ (%s)", jsonValue, jsonValue.getClass().getName()));
    }
    Number number = (Number) jsonValue;
    if (number.getClass()!=numberClass
        && converter!=null) {
      return converter.convert(number);
    }
    return number;
  }
}
