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
import com.effektif.workflow.test.TestSuiteHelper;
import com.effektif.workflow.test.WorkflowTest;
import com.mongodb.DB;


public class MongoApiTest {

  public static void initializeIds() {
    WorkflowTest.JOHN_ID = "5530a4aec2e610258e946fe0";
    WorkflowTest.MARY_ID = "5530a4aec2e610258e946fe1";
    WorkflowTest.JACK_ID = "5530a4aec2e610258e946fe2";
    WorkflowTest.DEV_ID = "5530a4aec2e610258e946fe3";
    WorkflowTest.OPS_ID = "5530a4aec2e610258e946fe4";
  }
  
  @Test
  public void testApiWithMongoConfiguration() {
    Configuration configuration = createMongoTestConfiguration();
    
    DB db = configuration.get(DB.class);
    db.dropDatabase();

    initializeIds();
    // this test runs the full API test suite with a mongo test configuration.
    
    TestSuiteHelper.run(configuration
      // use the next line if you only want to run 1 test
      // , SequentialExecutionTest.class, "testSequentialExecution"
      );
  }

  public static Configuration createMongoTestConfiguration() {
    MongoConfiguration configuration = new MongoConfiguration()
      .databaseName("effektif-test")
      .prettyPrint()
      .synchronous();
    configuration.start();
    return configuration;
  }

}
