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
package com.effektif.server;

import java.net.URI;

import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.mongo.MongoConfiguration;
import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.WorkflowEngine;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * @author Tom Baeyens
 */
public class WorkflowServer {

  // Note Jetty HTTP container does not support deployment on context paths 
  // other than root path ("/"). Non-root context path is ignored during deployment.
  
  public static final Logger log = LoggerFactory.getLogger(WorkflowServer.class+".HTTP");
  
  String baseUrl = "http://localhost:9999/";
  Configuration configuration;
  WorkflowEngine workflowEngine;
  
  public WorkflowServer(Configuration configuration) {
    this.configuration = configuration;
    this.workflowEngine = configuration.getWorkflowEngine();
  }

  public WorkflowServer baseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
    return this;
  }

  public static void main(String[] args) {
    MongoConfiguration configuration = new MongoConfiguration()
      .server("localhost", 27017);
    WorkflowServer workflowServer = new WorkflowServer(configuration);
    workflowServer.start();
  }

  public void start() {
    try {
      URI baseUri = new URI(baseUrl);
      ResourceConfig config = buildRestApplication(configuration);
      Server server = JettyHttpContainerFactory.createServer(baseUri, config);
      server.start();
      log.info("Workflow server started on "+baseUrl);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public static ResourceConfig buildRestApplication(Configuration configuration) {
    ResourceConfig config = new ResourceConfig();

    WorkflowEngine workflowEngine = configuration.getWorkflowEngine();
    
    config.registerInstances(
            new DeployResource(workflowEngine),
            new StartResource(workflowEngine),
            new MessageResource(workflowEngine),
            new PingResource() );

    ObjectMapper objectMapper = configuration.get(ObjectMapper.class);

    config.registerInstances(
            new JacksonFeature(),
            new ObjectMapperResolver(objectMapper),
            new RequestLogger(),
            new DefaultExceptionMapper() );
    
    return config;
  }
}
