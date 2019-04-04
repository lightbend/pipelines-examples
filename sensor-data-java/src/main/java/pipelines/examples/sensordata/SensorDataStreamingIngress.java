package pipelines.examples.sensordata;

import akka.http.javadsl.common.EntityStreamingSupport;
import akka.http.javadsl.marshallers.jackson.Jackson;

import pipelines.akkastream.javadsl.*;

public class SensorDataStreamingIngress extends HttpIngress<SensorData> {

  public SensorDataStreamingIngress() {
    super(KeyedSchemas.instance.sensorDataKeyed, Jackson.byteStringUnmarshaller(SensorData.class));
  }
  public HttpLogic createLogic() {
    EntityStreamingSupport entityStreamingSupport = EntityStreamingSupport.json();
    return defaultStreamingHttpLogic(entityStreamingSupport);
  }
}
