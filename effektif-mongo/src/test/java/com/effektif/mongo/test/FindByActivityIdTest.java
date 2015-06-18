package com.effektif.mongo.test;

import com.effektif.mongo.MongoConfiguration;
import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.activities.*;
import com.effektif.workflow.api.model.ActivityId;
import com.effektif.workflow.api.model.Deployment;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;

import java.util.List;

/**
 * Created by Jeroen on 12/06/15.
 */
public class FindByActivityIdTest {

    public static Configuration cachedConfiguration;

    public static void main(String[] args) {

        Configuration mongoConfiguration = new MongoConfiguration()
                .server("localhost")
                .databaseName("effektif");

        cachedConfiguration = mongoConfiguration;

        WorkflowEngine workflowEngine = cachedConfiguration.getWorkflowEngine();

        // Create a workflow
        ExecutableWorkflow workflow1 = new ExecutableWorkflow()
                .sourceWorkflowId("Server test workflow")
                .activity("One", new StartEvent()
                        .transitionToNext())
                .activity("Two", new NoneTask()
                        .transitionToNext())
                .activity("Three", new ReceiveTask()
                        .transitionToNext())
                .activity("Four", new HttpServiceTask()
                        .transitionToNext())
                .activity("Five", new EndEvent());

        Deployment deployment = workflowEngine.deployWorkflow(workflow1);

        TriggerInstance start = new TriggerInstance()
                .workflowId(deployment.getWorkflowId());

        WorkflowInstance workflowInstance = workflowEngine.start(start);

        // Now find all workflowInstances in a particular ActivityId, they should be in "Three"
        WorkflowInstanceQuery workflowInstanceQuery = new WorkflowInstanceQuery()
                .activityId(new ActivityId("Three"));

        WorkflowInstanceQuery workflowInstanceQuery2 = new WorkflowInstanceQuery();
//                .workflowInstanceId(workflowInstance.getId());

        List<WorkflowInstance> workflowInstances = workflowEngine.findWorkflowInstances(workflowInstanceQuery);

        List<WorkflowInstance> workflowInstances2 = workflowEngine.findWorkflowInstances(workflowInstanceQuery2);

        System.out.println("Nr of workflowInstances found: " + workflowInstances.size());
        System.out.println("Nr of workflowInstances found: " + workflowInstances2.size());
    }

}

