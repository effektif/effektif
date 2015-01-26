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
package com.effektif.adapter.test;

import org.junit.Test;

import com.effektif.adapter.AdapterServer;
import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.activities.AdapterActivity;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.adapter.Adapter;
import com.effektif.workflow.impl.adapter.AdapterService;
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
    AdapterServer adapterServer = new AdapterServer()
      .port(port)
      .registerActivityAdapter(new HelloWorldAdapter());
    adapterServer.startup();
    
    TestConfiguration configuration = new TestConfiguration();
    WorkflowEngine workflowEngine = configuration.getWorkflowEngine();

    AdapterService adapterService = configuration.get(AdapterService.class);
    Adapter adapter = adapterService.saveAdapter(new Adapter().url("http://localhost:"+port+"/"));
    adapterService.refreshAdapter(adapter.getId());

    Workflow workflow = new Workflow()
      .activity("hello", new AdapterActivity());
    
    workflow = workflowEngine.deployWorkflow(workflow);
    
    workflowEngine.startWorkflowInstance(workflow);
    
    adapterServer.shutdown();
  }
}
