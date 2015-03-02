package com.effektif.workflow.test.implementation;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.activity.ActivityTypeService;
import com.effektif.workflow.impl.bpmn.BpmnReader;
import com.effektif.workflow.impl.memory.TestConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.TestCase;
import org.xml.sax.SAXException;

/**
 * Set-up and convenience methods for BPMN reader and writer test cases.
 *
 * @author Peter Hilton
 */
public abstract class BpmnTestCase extends TestCase {

  protected static Configuration configuration;
  protected static File testResources;
  private static ActivityTypeService activityTypeService;
  private static ObjectMapper objectMapper;

  static {
    String dir = BpmnProcessTest.class.getProtectionDomain().getCodeSource().getLocation().toString();
    dir = dir.substring(5);
    testResources = new File(dir);
  }

  @Override
  public void setUp() throws Exception {
    if (configuration == null) {
      configuration = new TestConfiguration();
      configuration.getWorkflowEngine(); // to ensure initialization of the object mapper
      activityTypeService = configuration.get(ActivityTypeService.class);
      objectMapper = configuration.get(ObjectMapper.class);
    }
  }

  /**
   * Performs XML schema validation on the generated XML using the BPMN 2.0 schema.
   */
  protected void validateBpmnXml(String bpmnDocument) throws IOException {
    File schemaFile = new File(BpmnProcessTest.testResources, "bpmn/xsd/BPMN20.xsd");
    Source xml = new StreamSource(new StringReader(bpmnDocument));
    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    try {
      Schema schema = schemaFactory.newSchema(schemaFile);
      Validator validator = schema.newValidator();
      validator.validate(xml);
    } catch (SAXException e) {
      fail("BPMN XML validation error: " + e.getMessage());
    }
  }

  /**
   * Returns the workflow activity with the given ID, with the specified type.
   * Returns null if the ID isn’t found or the activity has the wrong type.
   */
  protected <T extends Activity> T findActivity(Workflow workflow, Class<T> activityType, String activityId) {
    for (Activity activity : workflow.getActivities()) {
      if (activity.getClass().equals(activityType) && activity.getId().equals(activityId)) {
        return (T) activity;
      }
    }
    return null;
  }

  /**
   * Returns the workflow transition with the given ID, or null if the ID isn’t found.
   */
  protected Transition findTransition(Workflow workflow, String transitionId) {
    List<Transition> transitions = workflow.getTransitions();
    if (transitions != null) {
      for (Transition transition : transitions) {
        if (transition.getId() != null && transition.getId().equals(transitionId)) {
          return transition;
        }
      }
    }
    return null;
  }

  protected Workflow readWorkflow(String filePath) throws IOException {File bpmn = new File(testResources, filePath);
    byte[] encoded = Files.readAllBytes(Paths.get(bpmn.getPath()));
    String bpmnXmlString = new String(encoded, StandardCharsets.UTF_8);
    BpmnReader reader = new BpmnReader(configuration);
    return reader.readBpmnDocument(new StringReader(bpmnXmlString));
  }

  protected void printBpmnXml(String generatedBpmnDocument) {
    System.out.println("--- GENERATED BPMN ------------------------------------------ ");
    System.out.println(generatedBpmnDocument);
    System.out.println("------------------------------------------------------------- ");
  }
}
