package pipelines.examples.sensordata

import java.nio.file
import java.nio.file._

import akka.NotUsed
import akka.stream.IOResult
import akka.stream.alpakka.file.scaladsl.Directory
import akka.stream.scaladsl._
import akka.util.ByteString
import pipelines.akkastream._
import pipelines.akkastream.scaladsl._
import pipelines.streamlets._
import pipelines.streamlets.avro._
import spray.json.JsonParser

import scala.concurrent.Future
import scala.concurrent.duration._

class SensorDataFileIngress extends AkkaStreamlet {

  import SensorDataJsonSupport._

  val out = AvroOutlet[SensorData]("out").withPartitioner(RoundRobinPartitioner)
  def shape = StreamletShape.withOutlets(out)

  private val sourceData = VolumeMount("source-data-mount", "/mnt/data", ReadWriteMany)

  override def volumeMounts = Vector(sourceData)

  // Streamlet processing steps
  // 1. Every X seconds
  // 2. Enumerate all files in the mounted path
  // 3. Read each file *)
  // 4. Deserialize file content to a SensorData value *)

  // *) Note that reading and deserializing the file content is done in separate steps for readability only, in production they should be merged into one step for performance reasons.

  override def createLogic = new RunnableGraphStreamletLogic() {
    val listFiles: NotUsed ⇒ Source[file.Path, NotUsed] = { _ ⇒ Directory.ls(getMountedPath(sourceData)) }
    val readFile: Path ⇒ Source[ByteString, Future[IOResult]] = { path: Path ⇒ FileIO.fromPath(path).via(JsonFraming.objectScanner(Int.MaxValue)) }
    val parseFile: ByteString ⇒ SensorData = { jsonByteString ⇒ JsonParser(jsonByteString.utf8String).convertTo[SensorData] }

    val emitFromFilesContinuously = Source.tick(1.second, 5.second, NotUsed)
      .flatMapConcat(listFiles)
      .flatMapConcat(readFile)
      .map(parseFile)
    def runnableGraph = emitFromFilesContinuously.to(plainSink(out))
  }
}
