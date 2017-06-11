package sync.db

import akka.actor.ActorSystem
import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.bulk.BulkCompatibleDefinition
import com.sksamuel.elastic4s.http.HttpClient
import com.sksamuel.elastic4s.http.index.IndexResponse
import com.sksamuel.elastic4s.http.search.SearchResponse
import com.sksamuel.elastic4s.streams.ReactiveElastic._
import com.sksamuel.elastic4s.streams.{BulkIndexingSubscriber, RequestBuilder, SubscriberConfig}
import org.elasticsearch.action.support.WriteRequest.RefreshPolicy
import play.api.libs.json._
import sync.FirehoseMessage

import scala.concurrent.Future
import scala.concurrent.duration._


object Db {

  object FireHoseDb {

    implicit object FirehoseMessageIndexable extends Indexable[FirehoseMessage] {
      implicit val jsonWriter = Json.format[FirehoseMessage]

      override def json(t: FirehoseMessage): String = {
        println(t)
        println("about to mkae json")

        val json: JsValue = Json.obj(
          "message_body" -> t.body,
          "user_id" -> t.userId,
          "correlation_id" -> t.correlationId,
          "exchange_name" -> t.exchangeName,
          "message_id" -> t.messageId.toString,
          "app_id" -> t.appId.toString,
          "routingKeys" -> t.routingKey
        )

        json.toString
      }

    }

    implicit object FirehoseMessageHitReader extends HitReader[FirehoseMessage] {
      override def read(hit: Hit): Either[Throwable, FirehoseMessage] = {
        val body = Json.parse(hit.sourceAsString)
        Json.fromJson[JsObject](body) match {
          case s: JsSuccess[FirehoseMessage] => Right(s.get)
          case e: JsError => Left(new Exception("couldnt parse search result"))

        }
      }
    }


    /*
    implicit object HashMapWriter extends  Writes[java.util.HashMap[String, String]] {
      /**
        * Convert the object into a JsValue
        */
      def writes(o: java.util.HashMap[String, String]) = new JSONObject()

    }
    */
    val client = HttpClient(ElasticsearchClientUri("127.0.0.1", 9200)) //TcpClient.transport(ElasticsearchClientUri("127.0.0.1", 9300).copy(options = Map[String, String]("cluster.name" -> "firehose")  ))

    //val client = TcpClient.transport("elasticsearch://localhost:9300?cluster.name=firehose")


    // we must import the dsl
    import com.sksamuel.elastic4s.http.ElasticDsl._


    // Next we create an index in advance ready to receive documents.
    // await is a helper method to make this operation synchronous instead of async
    // You would normally avoid doing this in a real program as it will block the calling thread
    // but is useful when testing
    def init = client.execute {
      createIndex("messages").mappings(
        mapping("body") as (
          textField("body")
          ),

        mapping("exchangeName") as (
          textField("exchange_name")
          ),
        mapping("routingKeys") as (
          objectField("routing_keys")
          ),
        mapping("appId") as (
          textField("app_id")
          ),
        mapping("messageId") as (
          textField("messageId")
          ),
        mapping("userId") as (
          textField("userId")
          ),
        mapping("correlationId") as (
          textField("correlationId")
          ),
        mapping("headers") as (
          objectField("headers")
          )
      ).shards(1).waitForActiveShards(1)

    }.await(10.seconds)

    // next we index a single document. Notice we can pass in the case class directly
    // and elastic4s will marshall this for us using the circe marshaller we imported earlier.
    // the refresh policy means that we want this document to flush to the disk immmediately.
    // see the section on Eventual Consistency.
    def insert(firehoseMessage: FirehoseMessage): Future[IndexResponse] = client.execute {
      println(firehoseMessage)
      indexInto("messages" / "message").doc(firehoseMessage).refresh(RefreshPolicy.IMMEDIATE)
    }

    // now we can search for the document we just indexed
    def query(queryList: List[(String, String)]): Future[SearchResponse] = client.execute {
      search("messages").query {
        must {
          queryList.map { tuple => termQuery(tuple) }
        }
      }
    }

    val config = SubscriberConfig[FirehoseMessage](batchSize = 1, concurrentRequests = 5)
    implicit val builder = new RequestBuilder[FirehoseMessage] {
      def request(t: FirehoseMessage): BulkCompatibleDefinition = {
        println("new request")
        indexInto("messages" / "message") doc t
      }
    }

    def subscriber(implicit sys: ActorSystem): BulkIndexingSubscriber[FirehoseMessage] = ReactiveElastic(client).subscriber[FirehoseMessage](config)

    def publisher(implicit sys: ActorSystem) = client.publisher {
      search("messages").query("exchange_name:amq*").scroll("1m").timeout(60.seconds)
    }


  }

}
