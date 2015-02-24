package com.effektif.workflow.impl.bpmn;

/**
 * Values for the Effektif type attribute used to map BPMN service task elements to Effektif model types, as in the XML:
 * <pre>{@code
 *  <servicetask effektif:type="http"/>
 * }</pre>
 *
 * @author Peter Hilton
 */
public enum ServiceTaskType {
    JAVA, EMAIL, HTTP;

    public boolean hasValue(String value) { return value().equals(value); }

    public String value() { return name().toLowerCase(); }
}
