package sync.db.reactive

import akka.actor.ActorSystem
import com.sksamuel.elastic4s.bulk.BulkCompatibleDefinition
import com.sksamuel.elastic4s.http.HttpClient
import com.sksamuel.elastic4s.streams.ReactiveElastic._
import com.sksamuel.elastic4s.streams.{BulkIndexingSubscriber, RequestBuilder, SubscriberConfig}
import sync.FirehoseMessage

import scala.concurrent.duration._
import sync.db.FirehoseEncodings._
import com.sksamuel.elastic4s.http.ElasticDsl._

object Reactive {
  val config = SubscriberConfig[FirehoseMessage](batchSize = 1, concurrentRequests = 5)
  implicit val builder = new RequestBuilder[FirehoseMessage] {
    def request(t: FirehoseMessage): BulkCompatibleDefinition = {
      indexInto("messages" / "message") doc t
    }
  }

  def subscriber(client: HttpClient)(implicit sys: ActorSystem): BulkIndexingSubscriber[FirehoseMessage] = ReactiveElastic(client).subscriber[FirehoseMessage](config)

  def publisher(client: HttpClient)(implicit sys: ActorSystem) = client.publisher {
    search("messages").query("exchange_name:amq*").scroll("1m").timeout(60.seconds)

  }
}

