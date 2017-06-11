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
  implicit val recoveryStrategy = RecoveryStrategy.nack(false)

  val source =  RabbitSource(
    rabbitControl,
    channel(qos = 3),
    consume(Bindings.firehose),
    body(as[JsObject])
      & property(Headers.ExchangeName)
      & property(Headers.RoutingKeys)
      & extract(getAppId)
      & extract(getMessageId)
      & extract(getUserId)
      & extract(getCorrelationId)
      & extract(getHeaders)
  ).map{(FirehoseMessage.fromJavaHashMap _).tupled}

  def run(callback: Try[Done] => Unit): SubscriptionRef =
    source
      .acked
      .alsoTo(Sink.fromSubscriber(subscriber))
      .to(Sink.onComplete(callback))
      .run()
}
