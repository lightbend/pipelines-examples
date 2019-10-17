package pipelines.examples.sensordata;

import akka.http.javadsl.common.EntityStreamingSupport;
import akka.http.javadsl.marshallers.jackson.Jackson;

import pipelines.akkastream.AkkaServerStreamlet;

import pipelines.akkastream.util.javadsl.HttpServerLogic;
import pipelines.akkastream.StreamletLogic;
import pipelines.streamlets.RoundRobinPartitioner;
import pipelines.streamlets.StreamletShape;
import pipelines.streamlets.avro.AvroOutlet;

public class SensorDataStreamingIngress extends AkkaServerStreamlet {

  AvroOutlet<SensorData> out =  AvroOutlet.<SensorData>create("out", SensorData.class)
          .withPartitioner(RoundRobinPartitioner.getInstance());

  public StreamletShape shape() {
   return StreamletShape.createWithOutlets(out);
  }

  public StreamletLogic createLogic() {
    EntityStreamingSupport ess = EntityStreamingSupport.json();
    return HttpServerLogic.createDefaultStreaming(this, out, Jackson.byteStringUnmarshaller(SensorData.class), ess, getStreamletContext());
  }
}
