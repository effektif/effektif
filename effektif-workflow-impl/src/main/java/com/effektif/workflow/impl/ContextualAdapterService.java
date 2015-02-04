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
package com.effektif.workflow.impl;

import java.util.List;

import com.effektif.workflow.api.command.RequestContext;
import com.effektif.workflow.api.datasource.ItemReference;
import com.effektif.workflow.impl.adapter.Adapter;
import com.effektif.workflow.impl.adapter.AdapterQuery;
import com.effektif.workflow.impl.adapter.AdapterService;
import com.effektif.workflow.impl.adapter.ExecuteRequest;
import com.effektif.workflow.impl.adapter.ExecuteResponse;
import com.effektif.workflow.impl.adapter.FindItemsRequest;


public class ContextualAdapterService implements AdapterService {
  
  AdapterService adapterService;
  RequestContext requestContext;
  
  public ContextualAdapterService(AdapterService adapterService, RequestContext requestContext) {
    this.adapterService = adapterService;
    this.requestContext = requestContext;
  }

  @Override
  public Adapter saveAdapter(Adapter adapter) {
    try {
      RequestContext.set(requestContext);
      return adapterService.saveAdapter(adapter);
    } finally {
      RequestContext.unset();
    }
  }

  @Override
  public ExecuteResponse executeAdapterActivity(String adapterId, ExecuteRequest executeRequest) {
    try {
      RequestContext.set(requestContext);
      return adapterService.executeAdapterActivity(adapterId, executeRequest);
    } finally {
      RequestContext.unset();
    }
  }

  @Override
  public Adapter refreshAdapter(String adapterId) {
    try {
      RequestContext.set(requestContext);
      return adapterService.refreshAdapter(adapterId);
    } finally {
      RequestContext.unset();
    }
  }

  @Override
  public List<Adapter> findAdapters(AdapterQuery adapterQuery) {
    try {
      RequestContext.set(requestContext);
      return adapterService.findAdapters(adapterQuery);
    } finally {
      RequestContext.unset();
    }
  }
  

  @Override
  public Adapter findAdapterById(String adapterId) {
    try {
      RequestContext.set(requestContext);
      return adapterService.findAdapterById(adapterId);
    } finally {
      RequestContext.unset();
    }
  }


  @Override
  public void deleteAdapters(AdapterQuery adapterQuery) {
    try {
      RequestContext.set(requestContext);
      adapterService.deleteAdapters(adapterQuery);
    } finally {
      RequestContext.unset();
    }
  }


  @Override
  public AdapterService createAdapterService(RequestContext requestContext) {
    return new ContextualAdapterService(adapterService, requestContext);
  }

  @Override
  public List<ItemReference> findItems(String adapterId, FindItemsRequest findItemsRequest) {
    try {
      RequestContext.set(requestContext);
      return adapterService.findItems(adapterId, findItemsRequest);
    } finally {
      RequestContext.unset();
    }
  }
}
