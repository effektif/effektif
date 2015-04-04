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
package com.effektif.mongo.test;

import org.junit.BeforeClass;

import com.effektif.mongo.MongoJsonMapper;
import com.effektif.workflow.api.mapper.Readable;
import com.effektif.workflow.test.serialization.AbstractMapperTest;
import com.mongodb.BasicDBObject;


/**
 * @author Tom Baeyens
 */
public class MongoJsonTest extends AbstractMapperTest {

  static MongoJsonMapper mongoJsonMapper = new MongoJsonMapper();

  @BeforeClass
  public static void initialize() {
    initializeMappings();
    mongoJsonMapper = new MongoJsonMapper();
    mongoJsonMapper.setJsonMappings(mappings);
  }

  @Override
  protected <T extends Readable> T serialize(T o) {
    BasicDBObject dbWorkflow = (BasicDBObject) mongoJsonMapper
      .createWriter()
      .toDbObject(o);
    
    System.out.println(dbWorkflow);
    
    return (T) mongoJsonMapper
      .createReader()
      .toObject(dbWorkflow, (Class<T>)o.getClass());
  }

}
