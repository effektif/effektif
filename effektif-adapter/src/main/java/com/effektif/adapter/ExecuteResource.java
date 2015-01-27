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
package com.effektif.adapter;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.impl.adapter.ExecuteRequest;
import com.effektif.workflow.impl.adapter.ExecuteResponse;
import com.effektif.workflow.impl.util.BadRequestException;


@Path("/execute")
public class ExecuteResource {
  
  private static final Logger log = LoggerFactory.getLogger(ExecuteResource.class);

  protected Configuration configuration;
  /** maps activity keys to activity adapters */
  protected Map<String, ActivityAdapter> activityAdapters = new HashMap<>();
  
  public ExecuteResource(Configuration configuration) {
    this.configuration = configuration;
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  public ExecuteResponse execute(ExecuteRequest executeRequest) {
    String activityKey = executeRequest.getActivityKey();
    ActivityAdapter activityAdapter = activityAdapters.get(activityKey);
    if (activityAdapter==null) {
      throw new BadRequestException("No activity found for key "+activityKey);
    }
    ActivityContext activityContext = new ActivityContext(configuration, executeRequest);
    activityAdapter.execute(activityContext);
    return activityContext.getExecuteResponse();
  }

  public void addActivityAdapter(ActivityAdapter activityAdapter) {
    String activityKey = activityAdapter.getDescriptor().getActivityKey();
    log.debug("Adding activity '"+activityKey+"' --> "+activityAdapter);
    activityAdapters.put(activityKey, activityAdapter);
  }

}
