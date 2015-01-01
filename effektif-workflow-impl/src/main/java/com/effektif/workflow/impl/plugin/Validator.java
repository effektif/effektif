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
package com.effektif.workflow.impl.plugin;

import java.util.List;

import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.impl.BindingImpl;
import com.effektif.workflow.impl.definition.ActivityImpl;


public interface Validator {

  void addError(String message, Object... messageArgs);

  void addWarning(String message, Object... messageArgs);

  ServiceRegistry getServiceRegistry();

  <T> BindingImpl<T> compileBinding(Binding<T> binding, String propertyName);
  <T> List<BindingImpl<T>> compileBinding(List<Binding<T>> binding, String propertyName);
  List<ActivityImpl> getStartActivities(ActivityImpl activity);

}
