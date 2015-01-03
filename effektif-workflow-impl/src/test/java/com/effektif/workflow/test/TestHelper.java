/*
 * Copyright 2014 Heisenberg Enterprises Ltd.
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
package com.effektif.workflow.test;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;

import com.effektif.workflow.api.workflowinstance.ActivityInstance;
import com.effektif.workflow.api.workflowinstance.ScopeInstance;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;

/**
 * @author Walter White
 */
public class TestHelper {
  
  public static void assertTextPresent(String expected, String actual) {
    if (actual==null || !actual.contains(expected)) {
      Assert.fail("Expected "+expected+" but was "+actual);
    }
  }

  public static ActivityInstance findActivityInstanceOpen(WorkflowInstance workflowInstance, Object activityDefinitionId) {
    return findActivityInstanceOpen(workflowInstance.getActivityInstances(), activityDefinitionId); 
  }

  static ActivityInstance findActivityInstanceOpen(List<? extends ActivityInstance> activityInstances, Object activityDefinitionId) {
    if (activityInstances!=null) {
      for (ActivityInstance activityInstance: activityInstances) {
        ActivityInstance theOne = findActivityInstanceOpen(activityInstance, activityDefinitionId);
        if (theOne!=null) {
          return theOne;
        }
      }
    }
    return null;
  }
  
  static ActivityInstance findActivityInstanceOpen(ActivityInstance activityInstance, Object activityDefinitionId) {
    if (activityDefinitionId.equals(activityInstance.getActivityId())) {
      return activityInstance;
    }
    return findActivityInstanceOpen(activityInstance.getActivityInstances(), activityDefinitionId);
  }

  public static void assertOpen(WorkflowInstance workflowInstance, String... expectedActivityNames) {
    Map<String,Integer> expectedActivityCounts = new HashMap<String, Integer>();
    if (expectedActivityNames!=null) {
      for (String expectedActivityName: expectedActivityNames) {
        Integer count = expectedActivityCounts.get(expectedActivityName);
        expectedActivityCounts.put(expectedActivityName, count!=null ? count+1 : 1);
      }
    }
    Map<String,Integer> activityCounts = new HashMap<String, Integer>();
    scanActivityCounts(workflowInstance, activityCounts);
    assertEquals(expectedActivityCounts, activityCounts);
  }
  
  static void scanActivityCounts(ScopeInstance scopeInstance, Map<String, Integer> activityCounts) {
    List< ? extends ActivityInstance> activityInstances = scopeInstance.getActivityInstances();
    if (activityInstances!=null) {
      for (ActivityInstance activityInstance : activityInstances) {
        if (!activityInstance.isEnded()) {
          Object activityId = activityInstance.getActivityId();
          Integer count = activityCounts.get(activityId);
          activityCounts.put(activityId.toString(), count != null ? count + 1 : 1);
          scanActivityCounts(activityInstance, activityCounts);
        }
      }
    }
  }

//  public static String getActivityInstanceId(WorkflowInstance workflowInstance, String activityDefinitionId) {
//    ActivityInstance activityInstance = workflowInstance.findActivityInstanceByActivityId(activityDefinitionId);
//    Assert.assertNotNull("No open activity instance found "+activityDefinitionId+" not found", activityInstance);
//    return activityInstance.getId();
//  }
//
//  public static WorkflowInstance endTask(WorkflowEngine workflowEngine, WorkflowInstance workflowInstance, String activityDefinitionId) {
//    return workflowEngine.newMessage()
//      .activityInstanceId(getActivityInstanceId(workflowInstance, activityDefinitionId))
//      .send();
//  }
//  
//  public static void mongoDeleteAllCollections(WorkflowEngine workflowEngine) {
//    MongoWorkflowEngine mongoWorkflowEngine = (MongoWorkflowEngine) workflowEngine;
//    mongoDeleteAllDocumentsInCollection(mongoWorkflowEngine, MongoWorkflowStore.class);
//    mongoDeleteAllDocumentsInCollection(mongoWorkflowEngine, MongoWorkflowInstanceStore.class);
//    mongoDeleteAllDocumentsInCollection(mongoWorkflowEngine, MongoJobs.class);
//  }
//
//  public static void mongoDeleteAllDocumentsInCollection(MongoWorkflowEngine processEngine, Class<? extends MongoCollection> mongoCollectionClass) {
//    MongoCollection mongoCollection = processEngine.getServiceRegistry().getService(mongoCollectionClass);
//    DBCollection dbCollection = mongoCollection.getDbCollection();
//    DBCursor documents = dbCollection.find();
//    while (documents.hasNext()) {
//      dbCollection.remove(documents.next());
//    }
//  }
}
