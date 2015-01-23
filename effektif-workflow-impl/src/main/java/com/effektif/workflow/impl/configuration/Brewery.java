/* Copyright 2014 Effektif GmbH.
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
package com.effektif.workflow.impl.configuration;

import java.util.HashMap;
import java.util.Map;

import com.effektif.workflow.impl.util.Exceptions;


/** brews service objects used by the implementation from raw configuration materials.
 * (minimalistic ioc container) */ 
public class Brewery {
  
  // private static final Logger log = LoggerFactory.getLogger(Brewery.class);

  Map<String,String> aliases = new HashMap<>();
  Map<String,Factory> factories = new HashMap<>();
  Map<String,Object> created = new HashMap<>();
  Map<String,Object> initialized = new HashMap<>();

  @SuppressWarnings("unchecked")
  public <T> T get(Class<T> type) {
    Exceptions.checkNotNullParameter(type, "type");
    return (T) get(type.getName());
  }

  public synchronized Object get(String name) {
    if (aliases.containsKey(name)) {
      name = aliases.get(name);
    }
    Object o = initialized.get(name);
    if (o!=null) {
      return o;
    }
    o = created.remove(name);
    if (o!=null) {
      initialize(o);
      return o;
    } 
    Factory factory = factories.get(name);
    if (factory!=null) {
      return factory.create(name, this);
    }
    throw new RuntimeException(name+" is not in registry");
  }

  public void initialize(Object o) {
    String name = o.getClass().getName();
    registerAlias(name, o.getClass());
    initialize(o, name);
  }

  public void initialize(Object o, String name) {
    if (o instanceof Initializable) {
      // log.debug("initializing("+name+")");
      ((Initializable)o).initialize(this);
    }
    initialized.put(name, o);
  }

  public void register(Object o) {
    String name = o.getClass().getName();
    registerAlias(name, o.getClass());
    register(o, name);
  }

  public void register(Object o, String name) {
    // log.debug("created("+o+")-->"+name);
    created.put(name, o);
    if (o instanceof Registerable) {
      ((Registerable)o).register(this);
    }
  }

  public void registerFactory(Factory f, Class<?> type) {
    String name = type.getClass().getName();
    registerFactory(f, name);
    registerAlias(name, type);
  }

  public void registerFactory(Factory f, String name) {
    factories.put(name, f);
  }

  public void registerAlias(String alias, String name) {
    // log.debug("alias("+alias+")-->"+name);
    aliases.put(alias, name);
  }

  protected void registerAlias(String name, Class<?>... types) {
    if (types!=null) {
      for (Class<?> serviceType: types) {
        registerAlias(serviceType.getName(), name);
        Class< ? > superclass = serviceType.getSuperclass();
        if (superclass!=null && superclass!=Object.class) {
          registerAlias(name, superclass);
        }
        registerAlias(name, serviceType.getInterfaces());
      }
    }
  }
}
