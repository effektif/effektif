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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.adapter.helpers.BadRequestException;
import com.effektif.workflow.api.datasource.ItemReference;
import com.effektif.workflow.impl.adapter.FindItemsRequest;


@Path("/items")
public class FindItemsResource {
  
  private static final Logger log = LoggerFactory.getLogger(FindItemsResource.class);

//  protected Configuration configuration;
  /** maps data source keys to data source adapters */
  protected Map<String, DataSourceAdapter> dataSourceAdapters = new HashMap<>();
  
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  public List<ItemReference> findItems(FindItemsRequest request) {
    String dataSourceKey = request.getDataSourceKey();
    DataSourceAdapter dataSourceAdapter = dataSourceAdapters.get(dataSourceKey);
    if (dataSourceAdapter==null) {
      throw new BadRequestException("No data source found for key "+dataSourceKey);
    }
    return dataSourceAdapter.findItems(request.getItemQuery());
  }

  public void addDataSourceAdapter(DataSourceAdapter dataSourceAdapter) {
    String dataSourceKey = dataSourceAdapter.getDescriptor().getDataSourceKey();
    log.debug("Adding data source '"+dataSourceKey+"' --> "+dataSourceAdapter);
    dataSourceAdapters.put(dataSourceKey, dataSourceAdapter);
  }
}
