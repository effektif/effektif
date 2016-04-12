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

import com.effektif.workflow.impl.util.Exceptions;

import java.util.*;


/** A container for initializing components and
 * resolving dependencies between components.
 *
 * <p>Uninitialized components are put in the brewery.
 * It's the brewery's responsibility to initialize
 * the dependencies of a component when it is requested
 * from the brewery.  Initialized components are cached
 * by the brewery.</p>
 *
 * <p>A brewery refers to components by name.  There are convenience
 * methods to look up a component by class name.  This class
 * name resolving is done with polymorhic classes in mind, so that
 * you can request an implementation object by its interface class.</p>
 *
 * <ul>
 * <li>An <b>ingredient</b> is a component that is instantiated and
 * which is not yet initialized.</li>
 *
 * <li>A <b>beer</b> is an initialized component.</li>
 *
 * <li>A <b>supplier</b> is a factory for instantiating a component.</li>
 * </ul>
 *
 * <p>Typically components have a default constructor and
 * member fields to store their dependencies.</p>
 *
 * <p>As input, the brewery receives instantiated components.
 * Meaning, these are components that are typically instantiated
 * with the default constructor, but for which the dependency member
 * fields are not yet initialized.</p>
 *
 * <p>When a component is fetched from the brewery, the brewery ensures
 * it is initialized by calling {@link Brewable#brew(Brewery)}.
 * In that method, the component can obtain it's dependencies
 * from the brewery.  So recursively all components that are
 * initialized that are required to return the originally requested
 * component.</p>
 *
 * <p>To resolve a dependency, components can choose between
 * {@link #get(String)} for required dependencies or {@link #getOpt(String)}
 * for optional dependencies.</p>
 *
 * <p>The brewery can be started and stopped.  Components can be notified
 * when the brewery is started ({@link Startable}) or stopped
 * ({@link Stoppable}).  The brewery will automatically scan for
 * these interfaces when they are added and invoke the appropriate
 * notifications.</p>
 */
public class Brewery {
  
  /** Components that have been started and that need to be
   * stopped when the brewery is stopped. */
  protected List<Stoppable> stoppables = new ArrayList<>();
  protected boolean isStarted = false;

  /** Maps aliases to component names.
   * Aliases are typically interface or superclass names.
   * Component names are typically the most specific classname of the component. */
  protected Map<String,String> aliases = new HashMap<>();
  
  /** Maps component names to a supplier, which can create the component on demand. */
  protected Map<String,Supplier> suppliers = new LinkedHashMap<>();
  
  /** Maps component names to a ingredients (= uninitialized components) that still need to be
   * brewed before they become a beer */
  protected Map<String,Object> ingredients = new LinkedHashMap<>();
  
  /** Maps component names to beers, which are the initialized, cached components
   * that are delivered to the application or to other components. */
  protected Map<String,Object> beers = new LinkedHashMap<>();
  
  /** Retrieves an initialized component based on the given class and
   * ensures the returned component is initialized.
   * Lookup by type should only be done for classes and interfaces
   * of which you know there will only be one in the whole brewery.
   * Use lookup by name otherwise.
   * @return the initialized component, never returns null.
   * @param type is the class or interface of the requested component. Subclasses
   *             of the given class or implementations of the given class will
   *             be found as well. A RuntimeException is thrown if type is null.
   * @throws RuntimeException if the component can not be found in the brewery
   * @throws RuntimeException if the type parameter is null.
   */
  @SuppressWarnings("unchecked")
  public <T> T get(Class<T> type) {
    Exceptions.checkNotNullParameter(type, "type");
    return (T) get(type.getName());
  }

  /** Retrieves an optional component based on the given class and if it's found,
   * the brewery ensures the returned component is initialized.
   * If the component is not found, null is returned.
   * Lookup by type should only be done for classes and interfaces
   * of which you know there will only be one in the whole brewery.
   * Use lookup by name otherwise.
   * @return the initialized component or null if it is not found.
   * @param type is the class or interface of the requested component. Subclasses
   *             of the given class or implementations of the given class will
   *             be found as well.
   * @throws RuntimeException if the type parameter is null.
   */
  @SuppressWarnings("unchecked")
  public <T> T getOpt(Class<T> type) {
    Exceptions.checkNotNullParameter(type, "type");
    return (T) getOpt(type.getName());
  }

  /** Retrieves a component by name and ensures the returned component is
   * initialized.
   * @return the initialized component, never returns null.
   * @param name is the name of the component.
   * @throws RuntimeException if the type parameter is null.
   * @throws RuntimeException if the component can not be found in the brewery */
  public synchronized Object get(String name) {
    Object component = getOpt(name);
    if (component!=null) {
      return component;
    }
    throw new RuntimeException("Unknown component name: '"+name+"' \n"+toString());
  }

  /** Retrieves an initialized component by name and if it's found,
   * the brewery ensures the returned component is initialized.
   * If the component is not found, null is returned.
   * @return the initialized component, never returns null.
   * @param name is the name of the component.
   * @throws RuntimeException if the type parameter is null.
   * @throws RuntimeException if the component can not be found in the brewery */
  public synchronized Object getOpt(String name) {
    Exceptions.checkNotNullParameter(name, "name");
    ensureStarted();
    if (aliases.containsKey(name)) {
      name = aliases.get(name);
    }
    Object component = beers.get(name);
    if (component!=null) {
      return component;
    }
    component = ingredients.get(name);
    if (component!=null) {
      beer(component);
      return component;
    }
    Supplier supplier = suppliers.get(name);
    if (supplier!=null) {
      component = supplier.supply(this);
      if (supplier.isSingleton()) {
        beer(component);
      }
      return component;
    }
    return null;
  }

  /** Current state of the brewery, listing
   * first the name of each component and its status,
   * and second the list of all the aliases */
  @Override
  public String toString() {
    StringBuilder internalState = new StringBuilder();
    StringBuilder aliasState = new StringBuilder();
    HashSet<String> names = new HashSet<>();
    names.addAll(aliases.keySet());
    names.addAll(suppliers.keySet());
    names.addAll(ingredients.keySet());
    names.addAll(beers.keySet());
    for (String name: new TreeSet<>(names)) {
      if (aliases.containsKey(name)) {
        aliasState.append(name);
        aliasState.append(" -> ");
        aliasState.append(aliases.get(name));
        aliasState.append("\n");
      } else {
        internalState.append(name);
        if (beers.containsKey(name)) {
          internalState.append(" (beer)");
        } else if (ingredients.containsKey(name)) {
          internalState.append(" (ingredient)");
        } else if (suppliers.containsKey(name)) {
          internalState.append(" (supplier)");
        }
        internalState.append("\n");
      }
    }
    return internalState.toString()+aliasState.toString();
  }

  /** Notifies all {@link Startable}s that have
   * been added to the brewery.
   * Starting and stopping a brewery is optional.
   * Starting a started brewery is ignored. */
  public void start() {
    if (!isStarted) {
      isStarted = true;

      List<String> namesToStart = new ArrayList<>();
      for (String name: ingredients.keySet()) {
        Object component = ingredients.get(name);
        addNameIfStartable(namesToStart, name, component.getClass());
      }
      for (String name: beers.keySet()) {
        Object component = beers.get(name);
        addNameIfStartable(namesToStart, name, component.getClass());
      }
// TODO add Supplier.getType and add this check for startables
//      for (String name: suppliers.keySet()) {
//        Supplier supplier = suppliers.get(name);
//        Class<?> type = supplier.getType();
//        addNameIfStartable(namesToStart, name, type);
//      }

      // first perform initialization on all startables
      List<Startable> startables = new ArrayList<>();
      for (String name: namesToStart) {
        startables.add((Startable) get(name));
      }

      // then invoke the start after all startables are initialized
      for (Startable startable: startables) {
        startable.start(this);
      }
    }
  }

  private void addNameIfStartable(List<String> namesToStart, String name, Class<?> type) {
    if (Startable.class.isAssignableFrom(type)
        && !namesToStart.contains(name)) {
      namesToStart.add(name);
    }
  }

  /** Notifies all {@link Stoppable}s that have
   * been started in this brewery.
   * Starting and stopping a brewery is optional.
   * Stopping a stopped brewery is ignored. */
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

  /** Puts an initialized component into the brewery.
   * It will be possible to lookup this component by all it's
   * superclasses and interfaces it implements.
   * @throws RuntimeException if component is null */
  public void beer(Object component) {
    Exceptions.checkNotNullParameter(component, "component");
    String name = component.getClass().getName();
    alias(name, component.getClass());
    beer(component, name);
  }

  /** Adds an initialized component to the brewery
   * under the given name.
   * @throws RuntimeException if component is null
   * @throws RuntimeException if name is null */
  public void beer(Object component, String name) {
    Exceptions.checkNotNullParameter(component, "component");
    Exceptions.checkNotNullParameter(name, "name");
    beers.put(name, component);
    if (component instanceof Brewable) {
      ((Brewable)component).brew(this);
    }
    if (component instanceof Stoppable) {
      addStoppable((Stoppable) component);
    }
  }

  /** Adds an uninitialized component to the brewery.
   * It will be possible to lookup this component by all it's
   * superclasses and interfaces it implements.
   * @throws RuntimeException if component is null */
  public void ingredient(Object component) {
    Exceptions.checkNotNullParameter(component, "component");
    String name = component.getClass().getName();
    alias(name, component.getClass());
    ingredient(component, name);
    if (component instanceof Stoppable) {
      addStoppable((Stoppable) component);
    }
  }

  /** Adds an uninitialized component to the brewery
   * under the given name.
   * @throws RuntimeException if component is null
   * @throws RuntimeException if name is null */
  public void ingredient(Object component, String name) {
    Exceptions.checkNotNullParameter(component, "component");
    Exceptions.checkNotNullParameter(name, "name");
    // log.debug("ingredient("+ingredient+")-->"+name);
    ingredients.put(name, component);
  }

  /** Adds a factory to the brewery that will be able to
   * create components when it's requested by type.
   * @throws RuntimeException if supplier is null
   * @throws RuntimeException if type is null */
  public void supplier(Supplier supplier, Class<?> type) {
    Exceptions.checkNotNullParameter(supplier, "supplier");
    Exceptions.checkNotNullParameter(type, "type");
    String name = type.getName();
    alias(name, type);
    supplier(supplier, name);
  }

  /** Adds a factory to the brewery that will be able to
   * create components when it's requested by name.
   * The supplier will be {@link Supplier#supply(Brewery) asked}
   * to instantiate a component when it's being retrieved from
   * the brewery, aka lazy initialization.
   * @throws RuntimeException if supplier is null
   * @throws RuntimeException if name is null */
  public void supplier(Supplier supplier, String name) {
    Exceptions.checkNotNullParameter(supplier, "supplier");
    Exceptions.checkNotNullParameter(name, "name");
    suppliers.put(name, supplier);
    if (supplier instanceof Stoppable) {
      addStoppable((Stoppable) supplier);
    }
  }

  /** Adds an alias so that a component with the
   * given name is also available as the given alias.
   * @throws RuntimeException if alias is null
   * @throws RuntimeException if name is null */
  public void alias(String alias, String name) {
    Exceptions.checkNotNullParameter(alias, "alias");
    Exceptions.checkNotNullParameter(name, "name");
    if (!alias.equals(name)) {
      aliases.put(alias, name);
    }
  }

  /** Removes a component from the brewery by type. */
  public void remove(Class<?> type) {
    Exceptions.checkNotNullParameter(type, "type");
    remove(type.getName());
  }

  /** Removes a component from the brewery by name. */
  public void remove(String name) {
    Exceptions.checkNotNullParameter(name, "name");
    String realName = name;
    if (aliases.containsKey(name)) {
      realName = aliases.get(name);
    }
    if (realName!=null) {
      ingredients.remove(realName);
      beers.remove(realName);
      ArrayList<String> keys = new ArrayList<>(aliases.keySet());
      for (String key: keys) {
        String value = aliases.get(key);
        if (realName.equals(value)) {
          aliases.remove(key);
        }
      }
    }
  }

  /** Adds an alias so that a component with the
   * given name is also available as the given alias. */
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

  protected synchronized void ensureStarted() {
    if (!isStarted) {
      start();
    }
  }

  protected void start(Collection<?> components) {
    for (Object o: components) {
      if (o instanceof Startable) {
        Startable startable = (Startable) o;
        startable.start(this);
      }
    }
  }

  protected void addStoppable(Stoppable stoppable) {
    for (Stoppable existing: stoppables) {
      if (existing==stoppable) {
        return;
      }
    }
    stoppables.add(stoppable);
  }
}
