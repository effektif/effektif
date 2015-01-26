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
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.types.ObjectType;
import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.util.BadRequestException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.TypeFactory;


public abstract class AbstractAdapterService implements AdapterService, Brewable {
  
  private static final Logger log = LoggerFactory.getLogger(AbstractAdapterService.class);
  
  protected ObjectMapper objectMapper;
  
  @Override
  public void brew(Brewery brewery) {
    objectMapper = brewery.get(ObjectMapper.class);
  }

  public Adapter refreshAdapter(String adapterId) {
    if (adapterId==null || "".equals(adapterId)) {
      throw new BadRequestException("Adapter may not be null or an empty string");
    }
    List<Adapter> adapters = findAdapters(new AdapterQuery().adapterId(adapterId));
    if (adapters.isEmpty()) {
      throw new BadRequestException("Adapter '");
    }
    Adapter adapter = adapters.get(0);
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
          CollectionLikeType listOfDescriptors = TypeFactory.defaultInstance().constructCollectionType(List.class, ObjectType.class);
          List<ObjectType> adapterDescriptors = objectMapper.readValue(inputStream, listOfDescriptors);
          for (ObjectType descriptor: adapterDescriptors) {
            log.debug("Adapter said: "+descriptor.getDescription());
          }
        }
        
      } catch (IOException e) {
        log.error("Problem while connecting to adapter: "+e.getMessage(), e);
      }
    }
    return adapter;
  }
}
