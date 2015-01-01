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
package com.effektif.workflow.impl.activitytypes;

import java.util.List;

import com.effektif.workflow.api.activities.EmbeddedSubprocess;
import com.effektif.workflow.impl.definition.ActivityImpl;
import com.effektif.workflow.impl.plugin.AbstractActivityType;
import com.effektif.workflow.impl.plugin.ConfigurationClass;
import com.effektif.workflow.impl.plugin.ControllableActivityInstance;
import com.effektif.workflow.impl.plugin.Validator;


@ConfigurationClass(EmbeddedSubprocess.class)
public class EmbeddedSubprocessImpl extends AbstractActivityType<EmbeddedSubprocess> {

  public static final EmbeddedSubprocessImpl INSTANCE = new EmbeddedSubprocessImpl();
  
  List<ActivityImpl> startActivities;
  
  @Override
  public void validate(ActivityImpl activity, EmbeddedSubprocess embeddedSubprocess, Validator validator) {
    this.startActivities = validator.getStartActivities(activity);
  }

  @Override
  public void start(ControllableActivityInstance activityInstance) {
    if (startActivities!=null && !startActivities.isEmpty()) {
      for (ActivityImpl startActivity: startActivities) {
        activityInstance.execute(startActivity);
      }
    } else {
      activityInstance.onwards();
    }
  }
}
