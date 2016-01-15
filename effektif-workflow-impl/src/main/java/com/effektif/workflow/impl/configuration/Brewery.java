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
package com.effektif.workflow.impl.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.effektif.workflow.impl.util.Exceptions;


/** minimalistic ioc container that brews objects used by the 
 * implementation from raw configuration ingredients.
 */ 
public class Brewery {
  
   // private static final Logger log = LoggerFactory.getLogger(Brewery.class);
  
  List<Stoppable> stoppables = new ArrayList<>();
  boolean isStarted = false;

  /** maps aliases to object names. 
   * aliases are typically interface or superclass names. 
   * object names are typically the most specific classname of the object. */
  Map<String,String> aliases = new HashMap<>();
  
  /** maps object names to a supplier, which can create the object on demand. */  
  Map<String,Supplier> suppliers = new LinkedHashMap<>();
  
  /** maps names to a ingredients are objects that potentially need to be brewed before they become a brew */
  Map<String,Object> ingredients = new LinkedHashMap<>();
  
  /** maps object names to brews, which are the final cached objects that are delivered */
  Map<String,Object> brews = new LinkedHashMap<>();
  
  /** creates a deep copy of all the collection fields, convenient 
   * for testing. */
  public Brewery createCopy() {
    Brewery copy = new Brewery();
    copy.stoppables = new ArrayList<>(stoppables);
    copy.aliases = new HashMap<>(aliases);
    copy.suppliers = new LinkedHashMap<>(suppliers);
    copy.ingredients = new LinkedHashMap<>(ingredients);
    copy.brews = new LinkedHashMap<>(brews);
    return copy;
  }
  
  @SuppressWarnings("unchecked")
  public <T> T get(Class<T> type) {
    Exceptions.checkNotNullParameter(type, "type");
    return (T) get(type.getName());
  }

  /** get and do not throw an exception of the object is not found */
  public <T> T getOpt(Class<T> type) {
    Exceptions.checkNotNullParameter(type, "type");
    return (T) getOpt(type.getName());
  }

  /**
   * Returns a named dependency, or throws an exception if not found.
   */
  public synchronized Object get(String name) {
    Object o = getOpt(name);
    if (o!=null) {
      return o;
    }
    // log.debug("\n\n"+contents);
    String contents = "brews\n";
    for (String n: brews.keySet()) {
      contents += "  "+n+"\n";
    }
    contents += "ingredients\n";
    for (String n: ingredients.keySet()) {
      contents += "  "+n+"\n";
    }
    contents += "suppliers\n";
    for (String n: suppliers.keySet()) {
      contents += "  "+n+"\n";
    }
    contents += "aliasses\n";
    for (String n: aliases.keySet()) {
      contents += "  "+n+"-->"+aliases.get(n)+"\n";
    }
    throw new RuntimeException(name+" is not in registry: \n"+contents);
  }

  protected synchronized void ensureStarted() {
    if (!isStarted) {
      start();
    }
  }

  public void start() {
    if (!isStarted) {
      isStarted = true;
      brewStartableIngredients(ingredients.values());
      start(suppliers.values());
      start(ingredients.values());
      start(brews.values());
    }
  }

  private void brewStartableIngredients(Collection<Object> ingredients) {
    for (Object ingredient: ingredients) {
      if (ingredient instanceof Startable) {
        Startable startable = (Startable) ingredient;
        if (!brews.containsValue(startable)) {
          brew(startable);
        }
      }
    }
  }

  protected void start(Collection<?> objects) {
    for (Object o: objects) {
      if (o instanceof Startable) {
        Startable startable = (Startable) o;
        startable.start(this);
      }
    }
  }

  public void stop() {
    if (isStarted) {
      isStarted = false;
      Collections.reverse(stoppables);
      for (Stoppable stoppable: stoppables) {
        stoppable.stop(this);
      }
      stoppables = new ArrayList<>();
    }
  }

  public synchronized Object getOpt(String name) {
    ensureStarted();
    // log.debug("getting("+name+")");
    if (aliases.containsKey(name)) {
      name = aliases.get(name);
    }
    Object o = brews.get(name);
    if (o!=null) {
      // log.debug("returning cached brew("+name+") "+System.identityHashCode(o));
      return o;
    }
    o = ingredients.get(name);
    if (o!=null) {
      brew(o);
      // log.debug("returning brewed("+name+") "+System.identityHashCode(o));
      return o;
    } 
    Supplier supplier = suppliers.get(name);
    if (supplier!=null) {
      // log.debug("supplying("+name+")");
      o = supplier.supply(this);
      if (supplier.isSingleton()) {
        brew(o);
      }
      // log.debug("returning supplied("+name+") "+System.identityHashCode(o));
      return o;
    }
    return null;
  }

  public void brew(Object o) {
    String name = o.getClass().getName();
    alias(name, o.getClass());
    brew(o, name);
  }

  public void brew(Object o, String name) {
    brews.put(name, o);
    if (o instanceof Brewable) {
      // log.debug("brewing("+name+")");
      ((Brewable)o).brew(this);
    }
    if (o instanceof Stoppable) {
      stoppables.add((Stoppable)o);
    }
  }

  public void ingredient(Object ingredient) {
    String name = ingredient.getClass().getName();
    alias(name, ingredient.getClass());
    ingredient(ingredient, name);
    if (ingredient instanceof Stoppable) {
      stoppables.add((Stoppable)ingredient);
    }
  }

  public void ingredient(Object ingredient, String name) {
    // log.debug("ingredient("+ingredient+")-->"+name);
    ingredients.put(name, ingredient);
  }

  public void supplier(Supplier supplier, Class<?> type) {
    String name = type.getName();
    alias(name, type);
    supplier(supplier, name);
  }

  public void supplier(Supplier supplier, String name) {
    suppliers.put(name, supplier);
    if (supplier instanceof Stoppable) {
      stoppables.add((Stoppable)supplier);
    }
  }

  public void alias(String alias, String name) {
    // log.debug("alias("+alias+")-->"+name);
    aliases.put(alias, name);
  }

  protected void alias(String name, Class<?>... types) {
    if (types!=null) {
      for (Class<?> serviceType: types) {
        alias(serviceType.getName(), name);
        Class< ? > superclass = serviceType.getSuperclass();
        if (superclass!=null && superclass!=Object.class) {
          alias(name, superclass);
        }
        alias(name, serviceType.getInterfaces());
      }
    }
  }
}
