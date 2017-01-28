package com.orendainx.hortonworks.trucking.nifi.processors

import java.io.OutputStream
import java.nio.charset.StandardCharsets

import com.orendainx.hortonworks.trucking.simulator.simulators.ManualTickAndFetchSimulator$
import com.typesafe.config.ConfigFactory
import org.apache.nifi.annotation.behavior._
import org.apache.nifi.annotation.documentation.{CapabilityDescription, Tags}
import org.apache.nifi.annotation.lifecycle.{OnRemoved, OnShutdown}
import org.apache.nifi.components.PropertyDescriptor
import org.apache.nifi.logging.ComponentLog
import org.apache.nifi.processor._
import org.apache.nifi.processor.io.OutputStreamCallback

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
@Tags(Array("trucking", "data", "event", "generator", "simulator", "iot"))
@CapabilityDescription("Generates data for a trucking application. Sample project <a href=\"https://github.com/orendain/trucking-nifi-bundle\">found here</a>")
@WritesAttributes(Array(
  new WritesAttribute(attribute = "dataType", description = "The class name of the of the TruckingData this flowfile carries (e.g. \"TruckData\" or \"TrafficData\").")
))
@InputRequirement(InputRequirement.Requirement.INPUT_FORBIDDEN)
@TriggerSerially
class GetTruckingData extends AbstractProcessor with GetTruckingDataProperties with GetTruckingDataRelationships {

  import scala.collection.JavaConverters._

  private var log: ComponentLog = _
  private lazy val config = ConfigFactory.load()
  private lazy val simulator = ManualTickAndFetchSimulator()

  override def init(context: ProcessorInitializationContext): Unit = {
    // On initialization, tick the simulator forward
    simulator.tick()
    log = context.getLogger
  }

  override def onTrigger(context: ProcessContext, session: ProcessSession): Unit = {

    // Fetch results that have been generated by the simulator since last onTrigger
    val truckingData = simulator.fetch()
    log.debug(s"Received data: $truckingData")

    // For each piece of data generated, create FlowFile and process appropriately.
    truckingData.foreach { data =>
      log.debug(s"Processing data: $data")

      var flowFile = session.create()
      flowFile = session.putAttribute(flowFile, "dataType", data.getClass.getSimpleName)

      flowFile = session.write(flowFile, new OutputStreamCallback {
        override def process(outputStream: OutputStream) = {
          outputStream.write(data.toCSV.getBytes(StandardCharsets.UTF_8))
        }
      })

      session.getProvenanceReporter.route(flowFile, RelSuccess)
      //TODO: This does what, session.getProvenanceReporter.receive(flowFile, "Huh?")
      session.transfer(flowFile, RelSuccess)
      session.commit()
    }

    // Tick the simulator forward so that results are ready to be fetched by onTrigger's next invocation
    simulator.tick()
  }

  override def getSupportedPropertyDescriptors: java.util.List[PropertyDescriptor] = properties.asJava

  override def getRelationships: java.util.Set[Relationship] = relationships.asJava

  @OnRemoved
  @OnShutdown
  def cleanup(): Unit = {
    simulator.stop()
  }
}
