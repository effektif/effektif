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
import com.effektif.workflow.api.workflow.WorkflowInstanceMigrator;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.memory.MemoryConfiguration;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class FindAndMigrateTest {

    @Test
    public void testMemoryConfig() {
        testFindAndMigrate(new MemoryConfiguration());
    }

    @Test
    public void testMongoConfig() {
        Configuration mongoConfiguration = new MongoConfiguration()
                .server("localhost")
                .databaseName("effektif");
        testFindAndMigrate(mongoConfiguration);
    }

    public static void testFindAndMigrate(Configuration configuration) {

        WorkflowEngine workflowEngine = configuration.getWorkflowEngine();

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

        workflowEngine.start(start);

        // Now find all workflowInstances in a particular ActivityId, they should be in "Three"
        WorkflowInstanceQuery activityIdQuery = new WorkflowInstanceQuery()
                .activityId("Three");

        WorkflowInstanceQuery workflowIdQuery = new WorkflowInstanceQuery().workflowId(deployment.getWorkflowId().getInternal());

        List<WorkflowInstance> workflowInstancesInThree = workflowEngine.findWorkflowInstances(activityIdQuery);
        List<WorkflowInstance> workflowInstancesInWorkflow = workflowEngine.findWorkflowInstances(workflowIdQuery);

        System.out.println("Nr of workflowInstances in activity Three: " + workflowInstancesInThree.size() + " (should be 1)");
        System.out.println("Nr of workflowInstances in the Workflow: " + workflowInstancesInWorkflow.size() + " (should be 1)");

        Assert.assertEquals(workflowInstancesInThree.size(), 1);
        Assert.assertEquals(workflowInstancesInWorkflow.size(), 1);

        workflowEngine.start(start);

        workflowInstancesInThree = workflowEngine.findWorkflowInstances(activityIdQuery);
        workflowInstancesInWorkflow = workflowEngine.findWorkflowInstances(workflowIdQuery);

        System.out.println("Now nr of workflowInstances in activity Three: " + workflowInstancesInThree.size() + " (should be 2)");
        System.out.println("Now nr of workflowInstances in the Workflow: " + workflowInstancesInWorkflow.size() + " (should be 2)");

        Assert.assertEquals(workflowInstancesInThree.size(), 2);
        Assert.assertEquals(workflowInstancesInWorkflow.size(), 2);

        // Test migration, just redeploy the workflow and migrate workflowInstances.
        WorkflowInstanceMigrator migrator = new WorkflowInstanceMigrator().originalWorkflowId(deployment.getWorkflowId().getInternal());

        workflow1.setId(null);
        Deployment deployment2 = workflowEngine.deployWorkflow(workflow1, migrator);

        // check migration
        WorkflowInstanceQuery newWorkflowIdQuery = new WorkflowInstanceQuery().workflowId(deployment2.getWorkflowId().getInternal());

        workflowInstancesInThree = workflowEngine.findWorkflowInstances(activityIdQuery);
        workflowInstancesInWorkflow = workflowEngine.findWorkflowInstances(newWorkflowIdQuery);
        List<WorkflowInstance> workflowInstancesInOldWorkflow = workflowEngine.findWorkflowInstances(workflowIdQuery);

        System.out.println("After migration, nr of workflowInstances in activity Three: " + workflowInstancesInThree.size() + " (should be 2)");
        System.out.println("After migration, nr of workflowInstances in the new Workflow: " + workflowInstancesInWorkflow.size() + " (should be 2)");
        System.out.println("After migration, nr of workflowInstances in the new Workflow: " + workflowInstancesInOldWorkflow.size() + " (should be 0)");

        Assert.assertEquals(workflowInstancesInThree.size(), 2);
        Assert.assertEquals(workflowInstancesInWorkflow.size(), 2);
        Assert.assertEquals(workflowInstancesInOldWorkflow.size(), 0);

        // clean up
        workflowEngine.deleteWorkflowInstances(newWorkflowIdQuery);
        workflowEngine.deleteWorkflowInstances(workflowIdQuery);
        workflowEngine.deleteWorkflows(new WorkflowQuery().workflowId(deployment.getWorkflowId()));
        workflowEngine.deleteWorkflows(new WorkflowQuery().workflowId(deployment2.getWorkflowId()));

    }

}

