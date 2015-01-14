Effektif
========

## Example API usage

```java
// Create the default (in-memory) workflow engine
WorkflowEngine workflowEngine = new MemoryWorkflowEngineConfiguration()
   .buildWorkflowEngine();

// Create a workflow
Workflow workflow = new Workflow()
  .activity(new NoneTask("a")
    .transitionTo("b"))
  .activity(new NoneTask("b"));

// Deploy the workflow to the engine
String workflowId = workflowEngine.deployWorkflow(workflow).getId();

// Start a new workflow instance
StartCommand start = new StartCommand()
  .workflowId(workflowId);
WorkflowInstance workflowInstance = workflowEngine.startWorkflowInstance(start);

assertTrue(workflowInstance.isEnded());
```

## [Creating workflows](https://github.com/effektif/effektif-oss/tree/master/docs/01-creating-workflows.md)
