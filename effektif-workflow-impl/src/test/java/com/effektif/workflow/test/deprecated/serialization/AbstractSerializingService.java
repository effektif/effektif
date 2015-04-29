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
package com.effektif.workflow.test.deprecated.serialization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.impl.deprecated.json.JsonMapper;
import com.effektif.workflow.test.serialization2.SerializationTest;


/**
 * @author Tom Baeyens
 */
public class AbstractSerializingService {
  
  protected static final Logger log = LoggerFactory.getLogger(SerializationTest.class+".JSON");

  protected JsonMapper jsonMapper;
  
  public AbstractSerializingService(JsonMapper jsonMapper) {
    this.jsonMapper = jsonMapper;
  }

  protected <T> T wireize(String name, Object o, Class<T> type) {
    if (o==null) return null;
    String jsonString = jsonMapper.writeToString(o);
    log.debug(name+jsonString);
    return jsonMapper.readFromString(jsonString, type);
  }
}
