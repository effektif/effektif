Effektif
========

## Example API usage

```java
// Create the default (in-memory) workflow engine
WorkflowEngine workflowEngine = new MemoryWorkflowEngineConfiguration()
   // for test purposes it's best to avoid concurrency so 
   // the synchronous executor service is configured here
   .registerService(new SynchronousExecutorService())
   .buildWorkflowEngine();

// Create a workflow
Workflow workflow = new Workflow()
  .activity(new NoneTask()
    .id("a")
    .transitionTo("b"))
  .activity(new NoneTask()
    .id("b"));

// Deploy the workflow to the engine
String workflowId = workflowEngine.deployWorkflow(workflow)
  .checkNoErrorsAndNoWarnings()
  .getWorkflowId();

// Start a new workflow instance
StartCommand start = new StartCommand()
  .workflowId(workflowId);
WorkflowInstance workflowInstance = workflowEngine.startWorkflowInstance(start);

assertTrue(workflowInstance.isEnded());
```