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

import scala.util.Properties



/** Factory for Reactive stream publisher/subscriber instances. */
object Reactive {
  /*
   * Reactive$ is a collection of publisher and subscriber for making FirehoseMessage searches or indexes reactively
   */
  val defaultDuration = Properties.envOrElse("SEARCH_TIMEOUT_DURATION", "60").toInt.seconds
  val config = SubscriberConfig[FirehoseMessage](batchSize = 1, concurrentRequests = 5)

  implicit val builder = new RequestBuilder[FirehoseMessage] {
    def request(t: FirehoseMessage): BulkCompatibleDefinition = {
      indexInto("messages" / "message") doc t
    }
  }

  /**
   * Creates a subscriber which lets you index messages that come down (functions like a Sink in Akka)
   *
   *  @param client an elastic search http client that can be used
   *  @param sys actor system to be able to instantiate actors which allow us to materialize a stream
   *  @return reactive stream subscriber which can be used as a Sink in Akka like `Sink.fromSubscriber(subscriber)`
   */
  def subscriber(client: HttpClient)(implicit sys: ActorSystem): BulkIndexingSubscriber[FirehoseMessage] = ReactiveElastic(client).subscriber[FirehoseMessage](config)


  /**
   * Creates a publisher that you can use for streaming results of queries
   *
   *  @param client an elastic search http client that can be used
   *  @param sys actor system to be able to instantiate actors which allow us to materializer a stream
   *  @return reactive stream publisher which can be used as a Source in akka like `Source.fromPublisher(publisher)`
   */
  def publisher(client: HttpClient)(implicit sys: ActorSystem) = client.publisher {
    search("messages").query("exchange_name:amq*").scroll("1m").timeout(defaultDuration)
  }
}

