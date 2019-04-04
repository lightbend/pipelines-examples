package pipelines.examples.sensordata;

import pipelines.akkastream.javadsl.*;

import akka.http.javadsl.marshallers.jackson.Jackson;

public class SensorDataIngress extends HttpIngress<SensorData> {
  public SensorDataIngress() {
    super(KeyedSchemas.instance.sensorDataKeyed, Jackson.byteStringUnmarshaller(SensorData.class));
  }
}
