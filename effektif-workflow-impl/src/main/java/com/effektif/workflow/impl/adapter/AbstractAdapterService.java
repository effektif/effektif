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
package com.effektif.workflow.impl.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.command.TypedValue;
import com.effektif.workflow.api.types.Type;
import com.effektif.workflow.impl.activity.types.AdapterActivityImpl;
import com.effektif.workflow.impl.activity.types.MappingImpl;
import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.data.DataTypeService;
import com.effektif.workflow.impl.data.TypedValueImpl;
import com.effektif.workflow.impl.util.BadRequestException;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.TypeFactory;


public abstract class AbstractAdapterService implements AdapterService, Brewable {
  
  private static final Logger log = LoggerFactory.getLogger(AbstractAdapterService.class);
  
  protected DataTypeService dataTypeService;
  protected ObjectMapper objectMapper;
  
  @Override
  public void brew(Brewery brewery) {
    dataTypeService = brewery.get(DataTypeService.class);
    objectMapper = brewery.get(ObjectMapper.class);
  }

  public Adapter refreshAdapter(String adapterId) {
    Adapter adapter = getAdapter(adapterId);
    if (adapter.url!=null) {
      try {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        HttpGet request = new HttpGet(adapter.url+"/activities");
        CloseableHttpResponse response = httpClient.execute(request);
        int status = response.getStatusLine().getStatusCode();
        if (200!=status) {
          throw new BadRequestException("Adapter didn't get it and answered "+status);
        }

        HttpEntity httpEntity = response.getEntity();
        if (httpEntity != null) {
          InputStream inputStream = httpEntity.getContent();
          CollectionLikeType listOfDescriptors = TypeFactory.defaultInstance().constructCollectionType(List.class, ActivityDescriptor.class);
          List<ActivityDescriptor> adapterDescriptors = objectMapper.readValue(inputStream, listOfDescriptors);
          for (ActivityDescriptor descriptor: adapterDescriptors) {
            log.debug("Adding descriptor: "+descriptor.getActivityKey());
            adapter.addDescriptor(descriptor);
          }
          
          saveAdapter(adapter);
        }
        
      } catch (IOException e) {
        log.error("Problem while connecting to adapter: "+e.getMessage(), e);
      }
    }
    return adapter;
  }
  
  public Adapter executeAdapterActivity(AdapterActivityImpl adapterActivity, ActivityInstanceImpl activityInstance) {
    Adapter adapter = getAdapter(adapterActivity.getAdapterId());
    if (adapter.url!=null) {
      try {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        ExecuteRequest executeRequest = new ExecuteRequest()
          .activityInstanceId(activityInstance.id)
          .workflowInstanceId(activityInstance.workflowInstance.id)
          .activityKey(adapterActivity.getActivityKey());

        // TODO move the mapping into the activity and only keep the http interactions here...
        
        List<MappingImpl> inputMappings = adapterActivity.getInputMappings();
        if (inputMappings!=null) {
          for (MappingImpl mapping: inputMappings) {
            Type type = null;
            Object value = null;
            TypedValueImpl typedValueImpl = activityInstance.getTypedValue(mapping.sourceBinding);
            if (typedValueImpl!=null) {
              value = typedValueImpl.value;
              if (typedValueImpl.type!=null) {
                type = typedValueImpl.type.serialize();
              } else if (value!=null) {
                type = dataTypeService.getTypeByValue(value);
              }
            }
            if (value!=null) {
              executeRequest.inputParameter(mapping.destinationKey, new TypedValue()
                .type(type)
                .value(value));
            }
          }
        }
        
        HttpPost request = new HttpPost(adapter.url+"/execute");
        String requestEntityJsonString = objectMapper.writeValueAsString(executeRequest);
        request.setEntity(new StringEntity(requestEntityJsonString, ContentType.APPLICATION_JSON));
        CloseableHttpResponse response = httpClient.execute(request);

        AdapterStatus adapterStatus = null;
        int status = response.getStatusLine().getStatusCode();
        if (200!=status) {
          log.error("Execution of adapter activity failed with http response code "+response.getStatusLine().toString());
          adapterStatus = AdapterStatus.ERROR;
        }
        
        ExecuteResponse executeResponse = null;
        HttpEntity httpEntity = response.getEntity();
        if (httpEntity != null) {
          try {
            InputStream inputStream = httpEntity.getContent();
            executeResponse = objectMapper.readValue(inputStream, ExecuteResponse.class);
            log.debug("Parsed adapter activity execute response");
          } catch (Exception e) {
            log.error("Problem while parsing the adapter activity execute response: "+e.getMessage(), e);
          }
        }
        
        if (executeResponse!=null) {
          // TODO parse the response values
        }

        AdapterLog adapterLog = new AdapterLog(executeRequest, executeResponse);
        updateAdapterExecution(adapterStatus, adapterLog);

      } catch (IOException e) {
        log.error("Problem while connecting to adapter: "+e.getMessage(), e);
      }
    }
    return adapter;
  }

  public void updateAdapterExecution(AdapterStatus adapterStatus, AdapterLog adapterLog) {
    // TODO
  }

  public Adapter getAdapter(String adapterId) {
    if (adapterId==null || "".equals(adapterId)) {
      throw new BadRequestException("Adapter may not be null or an empty string");
    }
    List<Adapter> adapters = findAdapters(new AdapterQuery().adapterId(adapterId));
    if (adapters.isEmpty()) {
      throw new BadRequestException("Adapter '");
    }
    Adapter adapter = adapters.get(0);
    return adapter;
  }

}
