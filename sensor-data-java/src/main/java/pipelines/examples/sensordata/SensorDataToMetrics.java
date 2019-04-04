package pipelines.examples.sensordata;

import java.util.Arrays;

import akka.stream.javadsl.*;

import akka.NotUsed;

import pipelines.akkastream.*;
import pipelines.akkastream.javadsl.*;

public class SensorDataToMetrics extends FlowProcessor<SensorData, Metric> {

  public SensorDataToMetrics() {
    super(KeyedSchemas.instance.sensorDataKeyed, KeyedSchemas.instance.metricKeyed);
  }

  public FlowLogic createLogic() {
    return new FlowLogic(this) {

      @Override
      public FlowWithContext<SensorData, PipelinesContext, Metric, PipelinesContext, NotUsed> createFlow() {
        return createFlowWithPipelinesContext()
          .mapConcat(data ->
            Arrays.asList(
              new Metric(data.getDeviceId(), data.getTimestamp(), "power", data.getMeasurements().getPower()),
              new Metric(data.getDeviceId(), data.getTimestamp(), "rotorSpeed", data.getMeasurements().getRotorSpeed()),
              new Metric(data.getDeviceId(), data.getTimestamp(), "windSpeed", data.getMeasurements().getWindSpeed())
            )
          );
        }
    };
  }
}
