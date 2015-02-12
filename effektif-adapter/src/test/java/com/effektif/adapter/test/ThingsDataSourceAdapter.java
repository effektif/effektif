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
package com.effektif.adapter.test;

import java.util.List;

import com.effektif.adapter.DataSourceAdapter;
import com.effektif.workflow.api.datasource.ItemQuery;
import com.effektif.workflow.api.datasource.ItemReference;
import com.effektif.workflow.impl.data.source.DataSourceDescriptor;
import com.effektif.workflow.impl.util.Lists;


public class ThingsDataSourceAdapter implements DataSourceAdapter {
  
  @Override
  public List<ItemReference> findItems(ItemQuery query) {
    return Lists.of(
      new ItemReference()
            .id("1")
            .label("Chair"),
      new ItemReference()
            .id("2")
            .label("Umbrella"),
      new ItemReference()
            .id("3")
            .label("Shoe"));
  }

  @Override
  public DataSourceDescriptor getDescriptor() {
    return new DataSourceDescriptor()
      .dataSourceKey("things")
      .label("Thing");
  }
}
