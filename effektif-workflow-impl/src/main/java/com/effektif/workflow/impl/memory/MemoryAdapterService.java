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
package com.effektif.workflow.impl.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.effektif.workflow.api.model.RequestContext;
import com.effektif.workflow.impl.ContextualAdapterService;
import com.effektif.workflow.impl.adapter.AbstractAdapterService;
import com.effektif.workflow.impl.adapter.Adapter;
import com.effektif.workflow.impl.adapter.AdapterQuery;
import com.effektif.workflow.impl.adapter.AdapterService;


public class MemoryAdapterService extends AbstractAdapterService {

  int nextId = 1;
  Map<String,Adapter> adapters = new ConcurrentHashMap<>();

  @Override
  public Adapter saveAdapter(Adapter adapter) {
    if (adapter.getId()==null) {
      String adapterId = Integer.toString(nextId++);
      adapter.setId(adapterId);
    }
    adapters.put(adapter.getId(), adapter);
    return adapter;
  }
  
  @Override
  public List<Adapter> findAdapters(AdapterQuery adapterQuery) {
    return new ArrayList<>(adapters.values());
  }

  @Override
  public void deleteAdapters(AdapterQuery adapterQuery) {
    adapters.clear();
  }

  @Override
  public AdapterService createAdapterService(RequestContext requestContext) {
    return new ContextualAdapterService(this, requestContext);
  }

  @Override
  public Adapter findAdapterById(String adapterId) {
    return adapters.get(adapterId);
  }
}
