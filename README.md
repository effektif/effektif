# Effektif

Effektif is a flexible workflow engine that you can embed into your own applicaitons.
Developing business applications with a worfklow engine like Effektif
saves time and energy that you can spend on cool features for your own application.

## What a workflow does

A workflow is based on a model (as in a flow chart or BPMN diagram)
and specifies an execution flow to coordinate tasks, automatic activities and timers.
The workflow engine keeps track of each execution (aka workflow instance)
and executes the activities as specified in the workflow.
Effektif handles activities that are synchronous (automatic actions), asynchronous (wait states).

## Why we created Effektif

Workflows increasingly interact with cloud services or micro services
over REST APIs. Those services don’t participate in transactions. The engine
needs to save its state before each activity is executed.

Secondly, support for pluggable persistence.
To start with, for our cloud hosted solution, we use MongoDB for horizontal scalability.
Our engine is unique becaues it separates persistence from the core engine.
Advanced users can even plug in their own persistence.

Third, the internals are a lot simpler.
Effektif tracks the runtime state of workflows using nested activity instances.
This is a lot easier to understand and it fits better in a document database,
compared to a token-based approach.

# Feature highlights

* Create workflows in Java, JSON, BPMN or with our online workflow builder (coming soon).
* Specify flows including user tasks and other wait states 
* Keeps track of workflow instances state, position in the diagram and data
* Easy configuration of reminders, escalations and other persistent timers 
* Intuitive, fluent Java API & REST API
* Apache 2.0 license

## Example

Here's what a workflow could look like:

![Example diagram](files/README-diagram.png?raw=true "Workflow diagram")

Here's how you can create this workflow in Java:

```java
// Create a workflow
Workflow workflow = new Workflow()
  .sourceWorkflowId("Release")
  .activity("Move open issues", new UserTask()
    .transitionToNext())
  .activity("Check continuous integration", new UserTask()
    .transitionToNext())
  .activity("Notify community", new EmailTask()
    .to("releases@example.com")
    .subject("New version released")
    .bodyText("Enjoy!"));
```

The workflow model can be easily converted to/from JSON:
```json
{
  "id" : "25077976-bd66-432e-a269-68de86ad78b7",
  "activities" : [ {
    "type" : "userTask",
    "id" : "Move open issues"
  }, {
    "type" : "userTask",
    "id" : "Check continuous integration"
  }, {
    "type" : "email",
    "id" : "Notify community",
    "toEmailAddresses" : [ {
      "value" : "releases@example.com"
    } ],
    "subject" : "New version released",
    "bodyText" : "Enjoy!"
  } ],
  "transitions" : [ {
    "from" : "Move open issues",
    "to" : "Check continuous integration"
  }, {
    "from" : "Check continuous integration",
    "to" : "Notify community"
  } ],
  "sourceWorkflowId" : "Release",
  "createTime" : "2015-03-10T13:53:09.396"
}
```

or BPMN:
```xml
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:effektif="effektif.com:1">
  <process id="Release">
    <userTask id="Move open issues"/>
    <userTask id="Check continuous integration"/>
    <serviceTask id="Notify community" effektif:type="email">
      <extensionElements>
        <effektif:subject value="New version released"/>
        <effektif:bodyText>Enjoy!</effektif:bodyText>
        <effektif:to value="releases@example.com"/>
      </extensionElements>
    </serviceTask>
    <sequenceFlow sourceRef="Move open issues" targetRef="Check continuous integration"/>
    <sequenceFlow sourceRef="Check continuous integration" targetRef="Notify community"/>
  </process>
</definitions>
```

The workflow engine can execute these workflows.  Here's how you start a new workflow instance: 

```java
WorkflowInstance workflowInstance = workflowEngine
  .start(new TriggerInstance()
    .workflowId(workflowId));
```

The workflow engine includes a task service that you can use 
in your app to create task lists for people. 

```java 
List<Task> tasks = taskService.findTasks(new TaskQuery());
assertEquals("Move open issues", tasks.get(0).getName());
assertEquals(1, tasks.size());
```

## Documentation

### [The Effektif Wiki](https://github.com/effektif/effektif/wiki)
Is the central place to
