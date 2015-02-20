/* Copyright (c) 2014, Effektif GmbH.
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
package com.effektif.workflow.impl.slack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.activity.AbstractActivityType;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;


/**
 * @author Tom Baeyens
 */
public class SlackPostImpl extends AbstractActivityType<SlackPost> {
  
  private static final Logger log = LoggerFactory.getLogger(SlackPostImpl.class);
  
  SlackService slackService;

  public SlackPostImpl() {
    super(SlackPost.class);
  }
  
  @Override
  public void parse(ActivityImpl activityImpl, SlackPost slackPost, WorkflowParser parser) {
    super.parse(activityImpl, slackPost, parser);
    
    log.debug("parsing slack post");
    
    slackService = parser.getConfiguration(SlackService.class);
    
    
    
    if (false) {
      parser.addError("Something is wrong with this slackPost");
    }
  }

  @Override
  public void execute(ActivityInstanceImpl activityInstance) {
    SlackAccount slackAccount = slackService.findAccount(activity.getSlackAccountId());
    slackAccount.createPost(activity.getChannel(), activity.getMessage());
    activityInstance.onwards();
  }
}
