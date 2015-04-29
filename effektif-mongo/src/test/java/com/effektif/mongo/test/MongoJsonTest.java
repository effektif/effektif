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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.mongo.PrettyPrinter;
import com.effektif.mongo.deprecated.MongoJsonMapper;
import com.effektif.workflow.test.deprecated.serialization.AbstractMapperTest;
import com.mongodb.BasicDBObject;

/**
 * @author Tom Baeyens
 */
public class MongoJsonTest extends AbstractMapperTest {

  protected static final Logger log = LoggerFactory.getLogger(MongoJsonTest.class);
  static MongoJsonMapper mongoJsonMapper = new MongoJsonMapper();

  protected String fileId() {
    return "552ce4fdc2e610a6a3dedb10";
  }

  protected String groupId(int index) {
    String[] ids = { "552ce4fdc2e610a6a3dedb20", "552ce4fdc2e610a6a3dedb21", "552ce4fdc2e610a6a3dedb22" };
    return ids[index];
  }

  protected String userId(int index) {
    String[] ids = { "552ce4fdc2e610a6a3dedb30", "552ce4fdc2e610a6a3dedb31", "552ce4fdc2e610a6a3dedb32" };
    return ids[index];
  }

  protected String workflowId() {
    return "552ce4fdc2e610a6a3dedb40";
  }

  @BeforeClass
  public static void initialize() {
    initializeMappings();
    mongoJsonMapper = new MongoJsonMapper();
    mongoJsonMapper.setMappings(mappings);
  }
  
//  @Test
//  public void testPrintObjectIds() {
//    for (int i=0; i<20; i++) {
//      System.out.println(ObjectId.get().toString());
//    }
//  }

  @Override
  protected <T> T serialize(T o) {
    BasicDBObject dbWorkflow = (BasicDBObject) mongoJsonMapper
      .writeToDbObject(o);
    
    String json = null; 
    if (mappings.isPretty()) {
      json = PrettyPrinter.toJsonPrettyPrint(dbWorkflow);
    } else {
      json = dbWorkflow.toString();
    }
    log.info("\n" + json + "\n");
    
    return (T) mongoJsonMapper
      .readFromDbObject(dbWorkflow, (Class<T>) o.getClass());
  }

}
