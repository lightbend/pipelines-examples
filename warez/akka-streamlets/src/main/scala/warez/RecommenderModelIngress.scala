package warez

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import pipelines.streamlets.avro._
import JsonFormats._
import warez.dsl._

/**
 * Ingress that reads the recommender model in base64 string format. We assume that the model
 * file is transferred after converting to base64. This should be the start of the model serving
 * pipeline.
 */
class RecommenderModelIngress extends HttpIngress[RecommenderModel](AvroOutlet[RecommenderModel]("out", _.modelId.toString))

