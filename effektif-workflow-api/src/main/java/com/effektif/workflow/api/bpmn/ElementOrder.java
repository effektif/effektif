package com.effektif.workflow.api.bpmn;

/**
 * Defines a total ordering on BPMN element names (in all caps), so that a {@link BpmnWriter} can export elements in
 * schema-valid order. XML schema requires child elements in the following order:
 *
 * 1. child elements of the based type defined by xsd:extension, in order, recursively
 * 2. child elements in the order they appear in xsd:sequence.
 *
 * For example, the 'relationship' element extends the 'tBaseElement' type, and has the sequence 'source', 'target';
 * 'tBaseElement' does not extend another type, and has the sequence 'documentation', 'extensionElements'. The correct
 * order of a 'relationship' element's child elements is therefore:
 *
 * 1. documentation
 * 2. extensionElements
 * 3. source
 * 4. target.
 *
 * Note that some elements have multiple possible parent elements - they are used in more than one place.
 */
public enum ElementOrder {

  // Comments indicate parent elements that require the given order.
  // Substitution groups have been replaced with the elements in the group.

  // assignment
  FROM,
  TO,

  // baseElement
  DOCUMENTATION,
  EXTENSIONELEMENTS,

  // category
  CATEGORYVALUE,

  // interface
  OPERATION,

  // laneSet
  LANE,

  // eventDefinition, messageEventDefinition
  OPERATIONREF,

  // property
  DATASTATE,

  // resource
  RESOURCEPARAMETER,

  // resourceAssignmentExpression
  EXPRESSION,
  FORMALEXPRESSION,

  // choreographyActivity, conversationNode, partnerEntity, partnerRole
  PARTICIPANTREF,
  MESSAGEFLOWREF,

  // flowElement, process
  AUDITING,
  MONITORING,
  CATEGORYVALUEREF,

  // sequenceFlow
  CONDITIONEXPRESSION,

  // flowNode
  INCOMING,
  OUTGOING,
  GATEWAYDIRECTION,
  ACTIVATIONCONDITION,

  // activity, callableElement, collaboration, gateway, globalTask, process, subChoreography, subConversation,
  // subProcess
  PARTICIPANT,
  MESSAGEFLOW,
  SUPPORTEDINTERFACEREF,
  IOSPECIFICATION,
  IOBINDING,
  PROPERTY,
  LANESET,
  ADHOCSUBPROCESS,
  BOUNDARYEVENT,
  BUSINESSRULETASK,
  CALLACTIVITY,
  CALLCHOREOGRAPHY,
  CHOREOGRAPHYTASK,
  COMPLEXGATEWAY,
  DATAOBJECT,
  DATAOBJECTREFERENCE,
  DATASTOREREFERENCE,
  STARTEVENT,
  EVENTBASEDGATEWAY,
  EXCLUSIVEGATEWAY,
  IMPLICITTHROWEVENT,
  INCLUSIVEGATEWAY,
  INTERMEDIATECATCHEVENT,
  INTERMEDIATETHROWEVENT,
  MANUALTASK,
  PARALLELGATEWAY,
  RECEIVETASK,
  SCRIPTTASK,
  SENDTASK,
  SEQUENCEFLOW,
  SERVICETASK,
  SUBCHOREOGRAPHY,
  SUBPROCESS,
  TASK,
  TRANSACTION,
  USERTASK,
  ENDEVENT,
  ASSOCIATION,
  GROUP,
  TEXTANNOTATION,
  RESOURCEROLE,
  PERFORMER,
  HUMANPERFORMER,
  POTENTIALOWNER,
  MULTIINSTANCELOOPCHARACTERISTICS,
  STANDARDLOOPCHARACTERISTICS,
  CORRELATIONSUBSCRIPTION,
  SUPPORTS,
  CALLCONVERSATION,
  CONVERSATION,
  SUBCONVERSATION,
  CONVERSATIONASSOCIATION,
  PARTICIPANTASSOCIATION,
  MESSAGEFLOWASSOCIATION,
  CORRELATIONKEY,
  CHOREOGRAPHYREF,
  CONVERSATIONLINK,

  // catchEvent, ioSpecification, throwEvent
  DATAINPUT,
  DATAINPUTASSOCIATION,
  DATAOUTPUT,
  DATAOUTPUTASSOCIATION,
  INPUTSET,
  OUTPUTSET,
  CANCELEVENTDEFINITION,
  COMPENSATEEVENTDEFINITION,
  CONDITIONALEVENTDEFINITION,
  ERROREVENTDEFINITION,
  ESCALATIONEVENTDEFINITION,
  LINKEVENTDEFINITION,
  MESSAGEEVENTDEFINITION,
  SIGNALEVENTDEFINITION,
  TERMINATEEVENTDEFINITION,
  TIMEREVENTDEFINITION,
  EVENTDEFINITIONREF,

  // scriptTask, globalScriptTask
  SCRIPT,

  // complexBehaviorDefinition
  CONDITION,
  EVENT,

  // dataAssociation
  SOURCEREF,
  TARGETREF,
  TRANSFORMATION,
  ASSIGNMENT,

  // inputSet
  DATAINPUTREFS,
  OPTIONALINPUTREFS,
  WHILEEXECUTINGINPUTREFS,
  OUTPUTSETREFS,

  // lane
  PARTITIONELEMENT,
  FLOWNODEREF,
  CHILDLANESET,

  // linkEventDefinition, relationship
  SOURCE,
  TARGET,

  // standardLoopCharacteristics
  LOOPCONDITION,

  // multiInstanceLoopCharacteristics
  LOOPCARDINALITY,
  LOOPDATAINPUTREF,
  LOOPDATAOUTPUTREF,
  INPUTDATAITEM,
  OUTPUTDATAITEM,
  COMPLEXBEHAVIORDEFINITION,
  COMPLETIONCONDITION,

  // operation
  INMESSAGEREF,
  OUTMESSAGEREF,
  ERRORREF,

  // outputSet
  DATAOUTPUTREFS,
  OPTIONALOUTPUTREFS,
  WHILEEXECUTINGOUTPUTREFS,
  INPUTSETREFS,

  // participant
  INTERFACEREF,
  ENDPOINTREF,
  PARTICIPANTMULTIPLICITY,

  // participantAssociation
  INNERPARTICIPANTREF,
  OUTERPARTICIPANTREF,

  // resourceRole
  RESOURCEREF,
  RESOURCEPARAMETERBINDING,

  // textAnnotation
  TEXT,

  // userTask, globalUserTask
  RENDERING;
}
