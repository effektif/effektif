package com.effektif.mongo;

public interface WorkflowFields {

  String NAME = "name";
  String ORGANIZATION_ID = "organizationId";
  String DEPLOYED_BY = "deployedBy";
  String SOURCE_WORKFLOW_ID = "sourceWorkflowId";
  String CREATE_TIME = "createTime";
  String TRIGGER = "trigger";
  String VARIABLES = "variables";

  interface Versions {
    String WORKFLOW_NAME = "workflowName";
    String VERSION_IDS = "versionIds";
    String LOCK = "lock";
  }

  interface VersionsLock {
    String OWNER = "owner";
    String TIME = "time";
  }

  //  interface Scope {
  //    String _ID = "_id";
  //    String ACTIVITIES = "activities";
  //    String VARIABLES = "variables";
  //    String TRANSITIONS = "transitions";
  //    String TIMERS = "timers";
  //  }
  //
  //  interface Activity extends FieldsScope {
  //    String DEFAULT_TRANSITION_ID = "defaultTransitionId";
  //    String MULTI_INSTANCE = "multiInstance";
  //    String ACTIVITY_TYPE = "type";
  //  }
  //
  //  interface Binding {
  //    String EXPRESSION = "expression";
  //    String VARIABLE_ID = "variableId";
  //    String TYPED_VALUE = "value";
  //  }
  //
  //  interface TypedValue {
  //    String TYPE = "type";
  //    String VALUE = "value";
  //  }
  //
  //  interface MultiInstance {
  //    String ELEMENT_VARIABLE = "elementVariable";
  //    String VALUE_BINDINGS = "valueBindings";
  //  }
  //
  //  interface Transition {
  //    String _ID = "_id";
  //    String FROM = "from";
  //    String TO = "to";
  //    String CONDITION = null;
  //  }
  //
  //  interface Variable {
  //    String _ID = "_id";
  //    String TYPE = "type";
  //    String INITIAL_VALUE = "initialValue";
  //  }
}
