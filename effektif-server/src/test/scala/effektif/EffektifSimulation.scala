package effektif

import java.io.StringWriter

import com.effektif.workflow.api.model.{Message, TriggerInstance}
import com.effektif.workflow.api.workflowinstance.WorkflowInstance
import com.effektif.workflow.impl.json.JsonService
import com.effektif.workflow.impl.memory.MemoryConfiguration
import com.fasterxml.jackson.databind.ObjectMapper
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class EffektifSimulation extends Simulation {
	val httpConf = http
			.baseURL("http://localhost:9999")
			.acceptHeader("application/json")


	val headers = Map(
			"Cache-Control" -> "no-cache",
			"Content-Type" -> "application/json; charset=UTF-8",
			"Pragma" -> "no-cache"
	)


  val configuration = new MemoryConfiguration()
  val mapper = configuration.get(classOf[JsonService])


  val workflowId = "Server test workflow"


  val userDataFeeder = new Feeder[String] {
    def hasNext = true

    def next() = {
      val start = new TriggerInstance().sourceWorkflowId(workflowId)
			Map("startMessage" -> mapper.objectToJsonString(start))
	  }
  }

	val scn = scenario("Simple Create and Get")
    .repeat(1) {
      feed(userDataFeeder)
      .exec(
				http("start process")
            .post("/start/")
            .headers(headers)
            .body(StringBody("${startMessage}")).asJSON
            .check(status.is(200))
            .check(regex("""(.*)""").saveAs("startedWorkflowInstance"))
        ).exec { session =>
          // map result and create trigger message
          val workflowInstanceJson = session.get("startedWorkflowInstance").as[String]
          val workflowInstance = mapper.jsonToObject(workflowInstanceJson, classOf[WorkflowInstance])
          val message = new Message()
            .workflowInstanceId(workflowInstance.getId())
            .activityInstanceId(workflowInstance.findOpenActivityInstance("Three").getId())
          session
            .set("triggerMessage", mapper.objectToJsonString(message))
				}.exec(
					http("trigger activity")
					.post("/message/")
					.headers(headers)
					.body(StringBody("${triggerMessage}")).asJSON
					.check(status.is(200))
          .check(jsonPath("$.workflowId").exists)
          .check(jsonPath("$.start").exists)
          .check(jsonPath("$.end").exists)
				)
    }

	setUp(scn.inject(rampUsers(2) over 10.seconds)).protocols(httpConf)
}
