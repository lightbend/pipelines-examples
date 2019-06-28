package warez
package dsl

import scala.util._

import akka._
import akka.actor._
import akka.http.scaladsl._
import akka.http.scaladsl.model._
import akka.stream._
import akka.stream.scaladsl._

import pipelines.akkastream._

trait HttpServer {
  def startServer(
      context: StreamletContext,
      handler: Flow[HttpRequest, HttpResponse, _],
      port: Int
  )(implicit system: ActorSystem, mat: Materializer): Unit = {
    import system.dispatcher
    Http()
      .bindAndHandle(handler, "0.0.0.0", port)
      .map { binding ⇒
        context.signalReady()
        system.log.info(s"Bound to ${binding.localAddress.getHostName}:${binding.localAddress.getPort}")
        // this only completes when StreamletRef executes cleanup.
        context.onStop { () ⇒
          system.log.info(s"Unbinding from ${binding.localAddress.getHostName}:${binding.localAddress.getPort}")
          binding.unbind().map(_ ⇒ Done)
        }
        binding
      }
      .andThen {
        case Failure(cause) ⇒
          system.log.error(cause, s"Failed to bind to $port.")
          context.stop()
      }
  }
}
