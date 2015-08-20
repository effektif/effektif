package com.effektif.workflow.api.workflow;

/**
 * Created by Jeroen on 20/08/15.
 */
public class WorkflowInstanceMigrator {

    public WorkflowInstanceMigrator WorkflowInstanceMigrator() {
        return this;
    }

    public WorkflowInstanceMigrator sourceWorkflowId(String sourceWorkflowId) {
        this.fromWorkflowId = sourceWorkflowId;
        return this;
    }
    public String fromWorkflowId;
}
