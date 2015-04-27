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
package com.effektif.adapter.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

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

import com.effektif.workflow.api.datasource.ItemReference;
import com.effektif.workflow.api.serialization.json.GenericType;
import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.data.DataTypeService;
import com.effektif.workflow.impl.mapper.JsonMapper;


public abstract class AbstractAdapterService implements AdapterService, Brewable {
  
  private static final Logger log = LoggerFactory.getLogger(AbstractAdapterService.class);
  
  protected DataTypeService dataTypeService;
  protected JsonMapper jsonMapper;
  
  @Override
  public void brew(Brewery brewery) {
    dataTypeService = brewery.get(DataTypeService.class);
    jsonMapper = brewery.get(JsonMapper.class);
  }

  public Adapter refreshAdapter(String adapterId) {
    Adapter adapter = getAdapter(adapterId);
    if (adapter.url!=null) {
      try {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        HttpGet request = new HttpGet(adapter.url+"/descriptors");
        CloseableHttpResponse response = httpClient.execute(request);
        int status = response.getStatusLine().getStatusCode();
        if (200!=status) {
          throw new RuntimeException("Adapter didn't get it and answered "+status);
        }

        HttpEntity httpEntity = response.getEntity();
        if (httpEntity != null) {
          InputStream inputStream = httpEntity.getContent();
          InputStreamReader reader = new InputStreamReader(inputStream);
          AdapterDescriptors adapterDescriptors = jsonMapper.readFromReader(reader, AdapterDescriptors.class);
          adapter.setActivityDescriptors(adapterDescriptors);
          saveAdapter(adapter);
        }
        
      } catch (IOException e) {
        log.error("Problem while connecting to adapter: "+e.getMessage(), e);
      }
    }
    return adapter;
  }
  
  public ExecuteResponse executeAdapterActivity(String adapterId, ExecuteRequest executeRequest) {
    ExecuteResponse executeResponse = null;
    Adapter adapter = getAdapter(adapterId);
    if (adapter!=null) {
      try {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        HttpPost request = new HttpPost(adapter.url+"/execute");
        String requestEntityJsonString = jsonMapper.writeToStringPretty(executeRequest);
        request.setEntity(new StringEntity(requestEntityJsonString, ContentType.APPLICATION_JSON));
        CloseableHttpResponse response = httpClient.execute(request);

        AdapterStatus adapterStatus = null;
        int status = response.getStatusLine().getStatusCode();
        if (200!=status) {
          log.error("Execution of adapter activity failed with http response code "+response.getStatusLine().toString());
          adapterStatus = AdapterStatus.ERROR;
        }
        
        HttpEntity httpEntity = response.getEntity();
        if (httpEntity != null) {
          try {
            InputStream inputStream = httpEntity.getContent();
            InputStreamReader reader = new InputStreamReader(inputStream);
            executeResponse = jsonMapper.readFromReader(reader, ExecuteResponse.class);
            log.debug("Parsed adapter activity execute response");
          } catch (Exception e) {
            log.error("Problem while parsing the adapter activity execute response: "+e.getMessage(), e);
          }
        }
        
        AdapterLog adapterLog = new AdapterLog(executeRequest, executeResponse);
        updateAdapterExecution(adapterStatus, adapterLog);

      } catch (IOException e) {
        log.error("Problem while connecting to adapter: "+e.getMessage(), e);
      }
    } 
    return executeResponse;
  }
  
  @Override
  public List<ItemReference> findItems(String adapterId, FindItemsRequest findItemsRequest) {
    List<ItemReference> items = null;
    Adapter adapter = getAdapter(adapterId);
    if (adapter!=null) {
      try {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        HttpPost request = new HttpPost(adapter.url+"/items");
        String requestEntityJsonString = jsonMapper.writeToStringPretty(findItemsRequest);
        request.setEntity(new StringEntity(requestEntityJsonString, ContentType.APPLICATION_JSON));
        CloseableHttpResponse response = httpClient.execute(request);

        AdapterStatus adapterStatus = null;
        int status = response.getStatusLine().getStatusCode();
        if (200!=status) {
          log.error("findItems of adapter "+adapterId+" failed with http response code "+response.getStatusLine().toString());
          adapterStatus = AdapterStatus.ERROR;
        }
        
        HttpEntity httpEntity = response.getEntity();
        if (httpEntity != null) {
          try {
            InputStream inputStream = httpEntity.getContent();
            InputStreamReader reader = new InputStreamReader(inputStream);
            items = jsonMapper.readFromReader(reader, new GenericType(List.class, ItemReference.class));
            log.debug("Parsed adapter data source find items");
          } catch (Exception e) {
            log.error("Problem while parsing the adapter activity execute response: "+e.getMessage(), e);
          }
        }
        
//        AdapterLog adapterLog = new AdapterLog(executeRequest, executeResponse);
//        updateAdapterExecution(adapterStatus, adapterLog);

      } catch (IOException e) {
        log.error("Problem while connecting to adapter: "+e.getMessage(), e);
      }
    } 
    return items;
  }


  public void updateAdapterExecution(AdapterStatus adapterStatus, AdapterLog adapterLog) {
    // TODO
  }

  public Adapter getAdapter(String adapterId) {
    if (adapterId==null || "".equals(adapterId)) {
      throw new RuntimeException("Adapter id may not be null or an empty string");
    }
    List<Adapter> adapters = findAdapters(new AdapterQuery().adapterId(adapterId));
    if (adapters.isEmpty()) {
      throw new RuntimeException("Adapter '"+adapterId+"' doesn't exist");
    }
    return adapters.get(0);
  }

}
