package pipelines.examples.sensordata;

import pipelines.akkastream.AkkaServerStreamlet;

import pipelines.akkastream.StreamletLogic;
import pipelines.akkastream.util.javadsl.HttpServerLogic;

import pipelines.streamlets.RoundRobinPartitioner;
import pipelines.streamlets.StreamletShape;
import pipelines.streamlets.avro.AvroOutlet;

import akka.http.javadsl.marshallers.jackson.Jackson;

public class SensorDataIngress extends AkkaServerStreamlet {
  AvroOutlet<SensorData> out =  AvroOutlet.<SensorData>create("out", SensorData.class)
          .withPartitioner(RoundRobinPartitioner.getInstance());

  public StreamletShape shape() {
   return StreamletShape.createWithOutlets(out);
  }

  public StreamletLogic createLogic() {
    return HttpServerLogic.createDefault(this, out, Jackson.byteStringUnmarshaller(SensorData.class), getStreamletContext());
  }
}
