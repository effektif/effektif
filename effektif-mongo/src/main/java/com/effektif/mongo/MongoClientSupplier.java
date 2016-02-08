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
package com.effektif.mongo;

import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.configuration.Supplier;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import java.util.List;

public class MongoClientSupplier implements Supplier {

  @Override
  public Object supply(Brewery brewery) {
    MongoConfiguration mongoConfiguration = brewery.get(MongoConfiguration.class);
    List<ServerAddress> serverAddresses = mongoConfiguration.getServerAddresses();
    List<MongoCredential> credentials = mongoConfiguration.getCredentials();
    MongoClientOptions options = mongoConfiguration.getOptionBuilder().build();
    if (credentials != null) {
      return new MongoClient(serverAddresses, credentials, options);
    }
    return new MongoClient(serverAddresses, options);
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
}
