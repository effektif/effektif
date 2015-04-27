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
package com.effektif.adapter;

import java.net.URI;

import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.adapter.helpers.DefaultExceptionMapper;
import com.effektif.adapter.helpers.RequestLogger;
import com.effektif.server.EffektifJsonProvider;
import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.impl.mapper.JsonMapper;


public class AdapterServer {

  public static final Logger log = LoggerFactory.getLogger(AdapterServer.class);
  
  protected Integer port;
  protected ResourceConfig config;
  protected Server server;
  protected JsonMapper jsonMapper;
  protected DescriptorsResource descriptorsResource;
  protected ExecuteResource executeResource;
  protected FindItemsResource findItemsResource;
  
  public AdapterServer() {
    Configuration configuration = new DefaultAdapterConfiguration();
    jsonMapper = configuration.get(JsonMapper.class);
    
    descriptorsResource = new DescriptorsResource();
    executeResource = new ExecuteResource(configuration);
    findItemsResource = new FindItemsResource();
    
    config = new ResourceConfig();
    config.registerInstances(
            new EffektifJsonProvider(jsonMapper),
            new RequestLogger(),
            new DefaultExceptionMapper(),
            descriptorsResource,
            executeResource,
            findItemsResource);
  }
  
  public AdapterServer port(Integer port) {
    this.port = port;
    return this;
  }

  public AdapterServer registerDataSourceAdapter(DataSourceAdapter dataSourceAdapter) {
    descriptorsResource.addDataSourceDescriptor(dataSourceAdapter.getDescriptor());
    findItemsResource.addDataSourceAdapter(dataSourceAdapter);
    return this;
  }

  public AdapterServer registerActivityAdapter(ActivityAdapter activityAdapter) {
    descriptorsResource.addActivityDescriptor(activityAdapter.getDescriptor());
    executeResource.addActivityAdapter(activityAdapter);
    return this;
  }

  public void startup() {
    try {
      URI baseUri = new URI("http://localhost"+(port!=null ? ":"+port : "")+"/");
      server = createServer(baseUri);
      server.start();
      log.info("Workflow adapter started.");
      
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  protected Server createServer(URI baseUri) {
    return JettyHttpContainerFactory.createServer(baseUri, config);
  }
  
  public void shutdown() {
    try {
      server.stop();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
  
  public ResourceConfig getResourceConfig() {
    return config;
  }
}
