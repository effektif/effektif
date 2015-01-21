Effektif is an easy, scalable workflow engine for designed for the cloud.  It's embeddable in JVM applications like Java and scala.  Effektif runs in memory or with a persistence engine like eg MongoDB.

A workflow is based on a diagram (eg BPMN or nodes and edges) and specify an execution flow to coordinate tasks, automatic activities and timers.  The workflow engine keeps track of each execution (aka workflow instance) and executes the activities as specified in the workflow.

```java
// Create the in-memory workflow engine, easy for testing
WorkflowEngine workflowEngine = new MemoryWorkflowEngineConfiguration()
  .initialize()
  .getWorkflowEngine();

// Create a workflow
Workflow workflow = new Workflow()
  .activity(new NoneTask("a")
    .transitionTo("b"))
  .activity(new NoneTask("b"));

// Deploy the workflow to the engine
workflow = workflowEngine.deployWorkflow(workflow);

// Start a new workflow instance
WorkflowInstance workflowInstance = workflowEngine
  .startWorkflowInstance(workflow);

assertTrue(workflowInstance.isEnded());

```

# Contents

* [Getting started](Getting-started)
* [Workflow engine types](Workflow-engine-types)
* [Activities](Activities)
* [Variables](Variables)
* [Create your own activity](Create-your-own-activity)
* [Create your own datasource](Create-your-own-datasource)
