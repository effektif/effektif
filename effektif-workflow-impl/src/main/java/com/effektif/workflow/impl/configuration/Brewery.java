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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.effektif.workflow.impl.util.Exceptions;


/** brews service objects used by the implementation from raw configuration ingredients.
 * (minimalistic ioc container) */ 
public class Brewery {
  
  // private static final Logger log = LoggerFactory.getLogger(Brewery.class);

  /** maps aliases to object names. 
   * aliases are typically interface or superclass names. 
   * object names are typically the most specific classname of the object. */
  Map<String,String> aliases = new HashMap<>();
  
  /** maps object names to a supplier, which can create the object on demand. */  
  Map<String,Supplier> suppliers = new HashMap<>();
  
  /** maps names to a ingredients are objects that potentially need to be brewed before they become a brew */
  Map<String,Object> ingredients = new HashMap<>();
  
  /** the names of objects that are being brewed, to prevent that they are brewed twice */
  Set<String> brewing = new HashSet<>();
  
  /** maps object names to brews, which are the final cached objects that are delivered */
  Map<String,Object> brews = new HashMap<>();

  @SuppressWarnings("unchecked")
  public <T> T get(Class<T> type) {
    Exceptions.checkNotNullParameter(type, "type");
    return (T) get(type.getName());
  }

  public synchronized Object get(String name) {
    if (aliases.containsKey(name)) {
      name = aliases.get(name);
    }
    Object o = brews.get(name);
    if (o!=null) {
      return o;
    }
    o = ingredients.get(name);
    if (o!=null) {
      brew(o);
      return o;
    } 
    Supplier supplier = suppliers.get(name);
    if (supplier!=null) {
      // log.debug("supplying("+name+")");
      return supplier.supply(this, name);
    }
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
    // log.debug("\n\n"+contents);
    throw new RuntimeException(name+" is not in registry: \n"+contents);
  }

  public void brew(Object o) {
    String name = o.getClass().getName();
    alias(name, o.getClass());
    brew(o, name);
  }

  public void brew(Object o, String name) {
    if ( (o instanceof Brewable) 
         &&!brewing.contains(name) ) {
      // log.debug("brewing("+name+")");
      brewing.add(name);
      ((Brewable)o).brew(this);
      brewing.remove(name);
    }
    brews.put(name, o);
  }

  public void ingredient(Object ingredient) {
    String name = ingredient.getClass().getName();
    alias(name, ingredient.getClass());
    ingredient(ingredient, name);
  }

  public void ingredient(Object ingredient, String name) {
    // log.debug("ingredient("+ingredient+")-->"+name);
    ingredients.put(name, ingredient);
  }

  public void supplier(Supplier supplier, Class<?> type) {
    String name = type.getName();
    supplier(supplier, name);
    alias(name, type);
  }

  public void supplier(Supplier supplier, String name) {
    suppliers.put(name, supplier);
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
