package sync


import akka.Done
import akka.actor.ActorRef
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.sksamuel.elastic4s.streams.BulkIndexingSubscriber
import com.spingo.op_rabbit.Directives._
import com.spingo.op_rabbit.PlayJsonSupport._
import com.spingo.op_rabbit._
import com.spingo.op_rabbit.stream.RabbitSource
import play.api.libs.json.JsObject

import scala.util.Try


class Sync(rabbitControl: ActorRef)(subscriber: BulkIndexingSubscriber[FirehoseMessage])(implicit actorMaterializer: ActorMaterializer) extends RabbitDataFetchers {
  val source =  RabbitSource(
    rabbitControl,
    channel(qos = 3),
    consume(firehoseBinding),
    body(as[JsObject])
      & property(Headers.ExchangeName)
      & property(Headers.RoutingKeys)
      & extract(getAppId _)
      & extract(getMessageId _)
      & extract(getUserId _)
      & extract(getCorrelationId _)
      & extract(getHeaders _)
  ).map{(FirehoseMessage.fromJavaHashMap _).tupled}
  private val firehoseExchange = Exchange.passive("amq.rabbitmq.trace")


  implicit val recoveryStrategy = RecoveryStrategy.nack(false)
  private val firehoseBinding = Queue.passive(topic(queue(
    "firehose",
    durable = false,
    exclusive = false,
    autoDelete = true),
    List("#"),
    exchange = firehoseExchange))

  def run(callback: Try[Done] => Unit): SubscriptionRef =
    source
      .acked
      .alsoTo(Sink.fromSubscriber(subscriber))
      .to(Sink.onComplete(callback))
      .run()
}
