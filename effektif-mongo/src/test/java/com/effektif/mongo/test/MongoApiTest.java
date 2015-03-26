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
package com.effektif.mongo.test;

import org.junit.Test;

import com.effektif.mongo.MongoConfiguration;
import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.impl.email.TestOutgoingEmailService;
import com.effektif.workflow.test.TestSuiteHelper;
import com.mongodb.DB;


public class MongoApiTest {
  
  @Test
  public void testApiWithMongoConfiguration() {
    Configuration configuration = createMongoTestConfiguration();
    
    DB db = configuration.get(DB.class);
    db.dropDatabase();

    // this test runs the full API test suite with a mongo test configuration.
    
    TestSuiteHelper.run(configuration
      // use the next line if you only want to run 1 test
      // , UserTaskTest.class, "testTaskRole"
      );
  }
  
  public static Configuration createMongoTestConfiguration() {
    return new MongoConfiguration()
      .databaseName("effektif-test")
      .ingredient(new TestOutgoingEmailService())
      .prettyPrint()
      .synchronous();
  }

  @Test
  public void testMongoConfigurationApi() {
    // this is used to copy and paste into the wiki docs
    Configuration configuration = new MongoConfiguration()
      .server("localhost") // localhost is the default
      .databaseName("databasename")
      .authentication("username", "password", "database");
  }
}
