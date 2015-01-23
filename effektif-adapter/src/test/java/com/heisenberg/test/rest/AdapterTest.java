/* Copyright 2014 Effektif GmbH.
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
package com.heisenberg.test.rest;

import org.junit.Test;

import com.effektif.adapter.Adapter;
import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.test.TestConfiguration;


public class AdapterTest {

//  static {
//    try {
//      final InputStream inputStream = RestTest.class.getResourceAsStream("/logging.properties");
//      LogManager.getLogManager().readConfiguration(inputStream);
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//  }

  @Test
  public void testAdapter() {
    int port = 11111;
    Adapter workflowAdapter = new Adapter()
      .port(11111)
      .registerActivityAdapter(new HelloWorldAdapter());
    workflowAdapter.startup();
    
    WorkflowEngine workflowEngine = new TestConfiguration()
      .registerAdapter("http://localhost:"+port+"/")
      .initialize()
      .getWorkflowEngine();
    
    
    
    workflowAdapter.shutdown();
  }
}
