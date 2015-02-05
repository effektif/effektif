# Effektif

Effektif is a flexible workflow engine that you can leverage when building your apps.  

* Easy workflow engine to embed in your Java application
* REST API
* Pluggable persistence
  * In-memory (no serialization)
  * Horizontally scalable NoSQL persistence
* Microservice architecture
* Easy to extend and customize
* Designed for the cloud
* Horizontally scalable
* Apache 2.0 license

A workflow is based on a diagram (eg BPMN or nodes and edges) and specify an execution flow to coordinate tasks, automatic activities and timers.  The workflow engine keeps track of each execution (aka workflow instance) and executes the activities as specified in the workflow.

## Example

![Example diagram](README-diagram.png?raw=true "Workflow diagram")

```java
// Create the default (in-memory) workflow engine
WorkflowEngineConfiguration configuration = new MemoryWorkflowEngineConfiguration()
  .initialize();
WorkflowEngine workflowEngine = configuration.getWorkflowEngine();
TaskService taskService = configuration.getTaskService();

// Create a workflow
Workflow workflow = new Workflow()
  .name("Release")
  .activity("Move open issues", new UserTask()
    .transitionToNext())
  .activity("Check continuous integration", new UserTask()
    .transitionToNext())
  .activity("Notify community", new EmailTask()
    .to("releases@example.com")
    .subject("New version released")
    .bodyText("Enjoy!"));

// Deploy the workflow to the engine
workflow = workflowEngine.deployWorkflow(workflow);

// Start a new workflow instance
WorkflowInstance workflowInstance = workflowEngine.startWorkflowInstance(workflow);

List<Task> tasks = taskService.findTasks(new TaskQuery());
assertEquals("Move open issues", tasks.get(0).getName());
assertEquals(1, tasks.size());
```

## Introduction

* Who is behind this project?
* [Open source versus commercial](https://github.com/effektif/effektif-oss/wiki/Open-source-versus-commercial)

## User Documentation

* [Getting started](https://github.com/effektif/effektif-oss/wiki/Getting-started)
* [Workflow engine types](https://github.com/effektif/effektif-oss/wiki/Workflow-engine-types)
* Building workflows
* Create your own activity
* Create your own datasource
* Run the REST service
* [Advanced features](https://github.com/effektif/effektif-oss/wiki/Advanced-features)

## Developer Documentation

* [Building the sources](https://github.com/effektif/effektif-oss/wiki/Building-the-sources)
* Working with MongoDB
* Architecture
* How to contribute
* Coding style
