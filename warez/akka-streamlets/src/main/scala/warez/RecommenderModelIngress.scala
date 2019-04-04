package warez

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import pipelines.akkastream.scaladsl._
import JsonFormats._
import KeyedSchemas._

/**
 * Ingress that reads the recommender model in base64 string format. We assume that the model
 * file is transferred after converting to base64. This should be the start of the model serving
 * pipeline.
 */
object RecommenderModelIngress extends HttpIngress[RecommenderModel]

