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
package com.effektif.adapter.test;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.effektif.adapter.AdapterServer;
import com.effektif.adapter.activity.AdapterActivity;
import com.effektif.adapter.service.Adapter;
import com.effektif.adapter.service.AdapterService;
import com.effektif.adapter.service.FindItemsRequest;
import com.effektif.adapter.service.MemoryAdapterService;
import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.datasource.ItemReference;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.memory.TestConfiguration;


public class AdapterTest {

  @Test
  public void testAdapter() {
    // NOTE from Rotterdam meeting:
    // Users will be able to create an API key in the Effektif Organization Settings UI
    // This API key will have to be passed into the pushDescriptorsToEffektif method
    
    // A developer or sysamind boots his own adapter
    int port = 11111;
    AdapterServer adapterServer = new AdapterServer()
      // .id("coca-cola") (*)
      .port(port)
      .registerActivityAdapter(new HelloWorldActivityAdapter())
      .registerDataSourceAdapter(new ThingsDataSourceAdapter());
    adapterServer.startup();

    // (*) TODO the adapter server should define its id.
    //     the pushDescriptorsToEffektif should also push the adapterServerId to the central workflow engine
    
    // The user opens the settings in the Effektif product and 
    // adds the adapter by configuring the URL
    TestConfiguration configuration = new TestConfiguration()
      .registerIngredient(new MemoryAdapterService());
    configuration.start();

    AdapterService adapterService = configuration.get(AdapterService.class);
    Adapter adapter = adapterService.saveAdapter(new Adapter().url("http://localhost:"+port+"/"));
    adapterService.refreshAdapter(adapter.getId()); // TODO (*) replace this with the line below
    // adapterServer.pushDescriptorsToEffektif("apikey");

    WorkflowEngine workflowEngine = configuration.getWorkflowEngine();

    
    // Next, the user is able to start building and executing workflows 
    // with the new activity 
    Workflow workflow = new Workflow()
      .activity("hello", new AdapterActivity()
        .adapterId(adapter.getId()) // TODO (*) replace with the line below
        // .adapterServerId("coca-cola")
        .activityKey("hello")
        .inputValue(HelloWorldActivityAdapter.NAME, "Walter")
      );
    
    workflowEngine
      .deployWorkflow(workflow)
      .checkNoErrors();
    workflowEngine.start(new TriggerInstance().workflowId(workflow.getId()));
    
    List<ItemReference> items = adapterService.findItems(adapter.getId(),
            new FindItemsRequest()
              .dataSourceKey("things"));
    assertEquals(3, items.size());
      
    adapterServer.shutdown();
  }
}
