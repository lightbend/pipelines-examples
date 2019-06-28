package warez
package dsl

import akka.http.scaladsl.unmarshalling._

import pipelines.streamlets._
import pipelines.akkastream._
import pipelines.akkastream.util.scaladsl.HttpServerLogic

abstract class HttpIngress[Out: FromByteStringUnmarshaller](val out: CodecOutlet[Out])
  extends AkkaServerStreamlet {

  final override val shape = StreamletShape.withOutlets(out)

  override final def createLogic = HttpServerLogic.default(this, out)
}
