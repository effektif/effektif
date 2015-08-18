package com.effektif.workflow.api.workflow;

/**
 * CumulusPro
 * Created by Jeroen on 13/08/15.
 */
public class WorkflowInstanceMigrator {

    public WorkflowInstanceMigrator WorkflowInstanceMigrator() {
        return this;
    }

    public WorkflowInstanceMigrator sourceWorkflowId(String sourceWorkflowId) {
        this.sourceWorkflowId = sourceWorkflowId;
        return this;
    }
    public String sourceWorkflowId;
}
