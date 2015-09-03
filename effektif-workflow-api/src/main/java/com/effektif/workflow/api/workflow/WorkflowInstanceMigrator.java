package com.effektif.workflow.api.workflow;

/**
 * Created by Jeroen on 20/08/15.
 */
public class WorkflowInstanceMigrator {

    public WorkflowInstanceMigrator originalWorkflowId(String sourceWorkflowId) {
        this.originalWorkflowId = sourceWorkflowId;
        return this;
    }

    public String originalWorkflowId;
}
