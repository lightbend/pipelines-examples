package warez

import scala.collection.immutable
import scala.util.Try

import akka.NotUsed
import akka.actor._
import akka.http.scaladsl.common._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.alpakka.elasticsearch._
import akka.stream.scaladsl._

import org.apache.commons.lang.StringEscapeUtils

import pipelines.streamlets._
import pipelines.streamlets.avro._
import pipelines.akkastream._
import pipelines.akkastream.scaladsl._
import pipelines.streamlets._
import warez.dsl._

/**
 * spray-json `JsonFormat[T]`s for Warez domain model (Product, Sku, etc.)
 */
import JsonFormats._
/**
 * spray-json extension methods for SerDes for domain types with defined json formats
 */
import spray.json._

object ProductSearchApiEgress extends AkkaStreamlet {

  val in = AvroInlet[Product]("in")
  val shape = StreamletShape.withInlets(in)

  /**
   * Indicate that this egress is hosting a Server by defining the `ServerAttribute`
   */
  override def attributes: immutable.Set[StreamletAttribute] = immutable.Set(ServerAttribute)

  private val HostName = RegExpConfigParameter(
    "es-hostname",
    "Elasticsearch hostname specified as a DNS label",
    "^(?![0-9]+$)(?!-)[a-zA-Z0-9-]{,63}(?<!-)$",
    Some("localhost")
  )

  private val PortNo = IntegerConfigParameter(
    "es-port",
    "Port number of Elasticsearch.",
    Some(9300))

  private val IndexName = StringConfigParameter(
    "es-index-name",
    "Elasticsearch index name.",
    None)

  override def configParameters = Vector(HostName, PortNo, IndexName)

  /**
   * Query API search result defaults
   */
  val defaultResultCount = 10
  val maxResultCount = 100

  /**
   * Mix in the `HttpServer` trait for access to a factory method to launch an akka-http server with `startServer`
   */
  override def createLogic: StreamletLogic = new RunnableGraphStreamletLogic with HttpServer {
    /**
     * An ElasticSearch REST client used by Alpakka ElasticSearch to connect to the query API.
     * TODO: Add ES connection info to streamlet config (`streamletConfig`). Hostname must be fully qualified K8s
     *       service name
     *
     *  hostname = "pipelines-elasticsearch-client.elasticsearch.svc.cluster.local",
     *  port = 9200,
     *  indexName = "products")
     */
    def getEsClient: ElasticSearchClient[Product] = {
      val esConfig = ElasticSearchClient.Config(
        hostname = streamletConfig.getString(HostName.key),
        port = streamletConfig.getInt(PortNo.key),
        indexName = streamletConfig.getString(IndexName.key))
      ElasticSearchClient[Product](esConfig)
    }

    /**
     * Request Handler to define the akka-http `Route` for our API
     */
    def requestHandler[In](implicit system: ActorSystem): Flow[HttpRequest, HttpResponse, _] = {
      /**
       * spray-json implicit conversions for JSON formats to akka-http marshallers. Used when handling requests.
       */
      import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
      /**
       * akka-http entity marshalling
       * i.e.) Used below to stream a `Source[T, NotUsed]` as a JavaScript array in the response.
       */
      implicit val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json()

      val esClient = getEsClient

      def intParam(countParam: Option[String], default: Int, max: Int): Int = {
        countParam
          .flatMap(c ⇒ Try(c.toInt).toOption)
          .filter(_ <= max)
          .getOrElse(default)
      }

      /**
       * Define the akka-http `Route` for handling request to the API.  Below we define one endpoint used to search
       * the ElasticSearch products index based on a string search criteria.  It will match against all fields in the
       * product including name, description, keywords, etc.  It takes an optional `count` query parameter to limit
       * the results returned.
       *
       * i.e.) `/products-search/search/Acme?count=5`
       *
       * The response will return a json array of 0 or more products.
       *
       * i.e.)
       *
       * ```
       * curl http://warez.apps.purplehat.lightbend.com/products-search/search/Acme?count=5
       * ```
       *
       * Pipelines will route the request to a path created for this streamlet.  It's important to include the
       * streamlet name in the akka-http path matcher using `streamletRef`.
       *
       * NOTE Akka Actor Materializer
       * i.e.) Used to transform an akka-http `Route` to a `Flow[HttpRequest, HttpResponse, _]`
       */
      path(streamletRef / "search" / RemainingPath) { searchCriteria ⇒
        get {
          parameters('count.?) { countParam ⇒
            val count = intParam(countParam, defaultResultCount, maxResultCount)

            /**
             * Escape the search criteria so that it can be encoded into an ElasticSearch json query
             * NOTE: `StringEscapeUtils` is from org.apache commons-lang transient dep.
             *       Is there a better way to do this?
             */
            val escapedSearchCriteria = StringEscapeUtils.escapeJavaScript(searchCriteria.toString)

            /**
             * NOTE: Is there a more appropriately scoped Akka logger to use in a streamlet?
             */
            system.log.debug(s"Search Criteria: $escapedSearchCriteria")
            val products: Source[Product, NotUsed] =
              esClient
                .querySource(escapedSearchCriteria)
                /**
                 * Log any upstream stream failures (i.e. transient errors from ElasticSearch) to ERROR.
                 * Log results to DEBUG.
                 * NOTE: Because we are streaming a response there's no time to change the response status to a helpful
                 *       HTTP code because by that point we've already begun returning the chunked response.  Therefore
                 *       it's important to log this error so it's not swallowed.
                 */
                .log("ES query")
                .take(count)
                .map { message: ReadResult[spray.json.JsObject] ⇒
                  system.log.info(s"Returning Product:\n${message.source.prettyPrint}")
                  message.source.convertTo[Product]
                }

            complete(products)
          }
        }
      }
    }

    /**
     * Start an akka-http server
     */
    startServer(
      context,
      requestHandler,
      ServerAttribute.containerPort(config)
    )

    override def runnableGraph =
      atLeastOnceSource(in)
        .via(flowWithContext.asFlow)
        .to(atLeastOnceSink)

    /**
     * Akka Streams Flow to process messages from the streamlet inlet
     */
    private def flowWithContext = {
      val esClient = getEsClient

      /**
       * Use `FlowWithContext` so that we can commit offsets using at-least-once semantics after
       * messages have been indexed to ES.
       */
      FlowWithPipelinesContext[Product]
        .log("ES index", product ⇒ s"Indexing Product:\n${product.toJson.prettyPrint}")
        .map { product ⇒
          WriteMessage.createUpsertMessage[Product](product.id.toString, product)
        }
        .via(esClient.indexFlow())
        .log("ES index")
    }
  }
}

