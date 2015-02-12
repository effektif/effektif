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
package com.effektif.workflow.impl.activity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.impl.json.JsonService;
import com.effektif.workflow.impl.util.Exceptions;


@Deprecated
public class DescriptorSender {
  
  public static final Logger log = LoggerFactory.getLogger(DescriptorSender.class);

  protected ActivityTypeService activityTypeService;
  protected JsonService jsonService;
  protected String authenticationToken;
  protected String authenticationUsername;
  protected String authenticationPassword;
  protected String scheme = "https";
  protected String server = "api.effektif.com";
  protected Integer port;
  protected String organizationKey;

  public DescriptorSender() {
  }

  public DescriptorSender(ActivityTypeService activityTypeService, JsonService jsonService) {
    this.activityTypeService = activityTypeService;
    this.jsonService = jsonService;
  }

  public String send() {
    // TODO add HTTP
    if (log.isDebugEnabled())
      log.debug("Sending process profile over HTTP to the process builder:");
    if (log.isDebugEnabled())
      log.debug(">>> PUT "+getUrl());
    // TODO send over HTTP to the server
    if (log.isDebugEnabled())
      log.debug(">>> "+jsonService.objectToJsonStringPretty(activityTypeService));
    return "the http response, which is hopefully 200 OK";
  }
  
  public DescriptorSender authenticationToken(String authenticationToken) {
    this.authenticationToken = authenticationToken;
    return this;
  }
  
  public DescriptorSender authentication(String username, String password) {
    this.authenticationUsername = username;
    this.authenticationPassword = password;
    return this;
  }

  public DescriptorSender scheme(String scheme) {
    this.scheme = scheme;
    return this;
  }
  
  public DescriptorSender server(String server) {
    this.server = server;
    return this;
  }
  
  public DescriptorSender port(Integer port) {
    this.port = port;
    return this;
  }
  
  public DescriptorSender organizationKey(String organizationKey) {
    this.organizationKey = organizationKey;
    return this;
  }
  
  public String getAuthenticationToken() {
    return authenticationToken;
  }
  
  public void setAuthenticationToken(String authenticationToken) {
    this.authenticationToken = authenticationToken;
  }
  
  public String getAuthenticationUsername() {
    return authenticationUsername;
  }
  
  public void setAuthenticationUsername(String authenticationUsername) {
    this.authenticationUsername = authenticationUsername;
  }
  
  public String getAuthenticationPassword() {
    return authenticationPassword;
  }
  
  public void setAuthenticationPassword(String authenticationPassword) {
    this.authenticationPassword = authenticationPassword;
  }

  
  public String getScheme() {
    return scheme;
  }

  
  public void setScheme(String scheme) {
    this.scheme = scheme;
  }

  
  public String getServer() {
    return server;
  }

  
  public void setServer(String server) {
    this.server = server;
  }

  
  public Integer getPort() {
    return port;
  }

  
  public void setPort(Integer port) {
    this.port = port;
  }

  public String getUrl() {
    Exceptions.checkNotNull(scheme, "profileName is null");
    Exceptions.checkNotNull(server, "server is null");
    Exceptions.checkNotNull(organizationKey, "organizationKey is null");
    return scheme+"://"+server+(port!=null ? ":"+port : "")+"/api/v1/"+organizationKey+"/profile";
  }
}
