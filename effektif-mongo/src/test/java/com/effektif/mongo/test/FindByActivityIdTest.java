package com.effektif.mongo.test;

import com.effektif.mongo.MongoConfiguration;
import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.activities.*;
import com.effektif.workflow.api.model.Deployment;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.query.WorkflowQuery;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;

import java.util.List;

/**
 * Template for unit test to be created...
 */
public class FindByActivityIdTest {

    public static Configuration cachedConfiguration;

    public static void main(String[] args) {

        cachedConfiguration = new MongoConfiguration()
                .server("localhost")
                .databaseName("effektif");

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

        // Now check that the workflowInstance is in ActivityId "Three".
        WorkflowInstanceQuery workflowInstanceQuery = new WorkflowInstanceQuery()
                .activityId("Three");
        List<WorkflowInstance> workflowInstances = workflowEngine.findWorkflowInstances(workflowInstanceQuery);
        System.out.println("Nr of workflowInstances found: " + workflowInstances.size() + " (should be 1)");

        // Find all workflowInstances in the workflow
        WorkflowInstanceQuery workflowInstanceQuery2 = new WorkflowInstanceQuery();
        List<WorkflowInstance> workflowInstances2 = workflowEngine.findWorkflowInstances(workflowInstanceQuery2);
        System.out.println("Nr of workflowInstances found: " + workflowInstances2.size() + " (should be 1)");

        // Now move the workflowInstance back to activityId "Two", the workflow should automatically advance it to "Three" again
        workflowEngine.move(workflowInstance.getId(), "Two");
        List<WorkflowInstance> workflowInstances4 = workflowEngine.findWorkflowInstances(workflowInstanceQuery);

        // Add another workflowInstance to the workflow
        workflowEngine.start(start);

        List<WorkflowInstance> workflowInstances3 = workflowEngine.findWorkflowInstances(workflowInstanceQuery2);

        System.out.println("Nr of workflowInstances found: " + workflowInstances.size() + " (should be 1)");
        System.out.println("Nr of workflowInstances found: " + workflowInstances2.size() + " (should be 1)");
        System.out.println("Nr of workflowInstances found: " + workflowInstances3.size() + " (should be 2)");
        if(workflowInstances4.size() == 1) {
            System.out.println("Number of activityInstances should be 5 now, it is: " + workflowInstances4.get(0).getActivityInstances().size());
        }
        else System.out.println("Move went wrong....");

        // Cleanup
        workflowEngine.deleteWorkflowInstances(new WorkflowInstanceQuery().activityId("Three"));
        workflowEngine.deleteWorkflows(new WorkflowQuery().workflowId(workflow1.getId()));
    }

}

