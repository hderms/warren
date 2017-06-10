package sync

import akka.actor.ActorRef
import akka.stream.scaladsl.Sink
import com.spingo.op_rabbit.Directives._
import com.spingo.op_rabbit.PlayJsonSupport._
import com.spingo.op_rabbit.stream.RabbitSource
import com.spingo.op_rabbit.{Exchange, RecoveryStrategy}
import play.api.libs.json.JsObject



/**
  * Created by rtuser on 6/10/17.
  */
class Sync(rabbitControl: ActorRef) {
  private val firehoseExchange = Exchange.topic("amq.rabbitmq.trace")

  private val firehoseBinding = topic(queue(
    "firehose",
    durable = true,
    exclusive = false,
    autoDelete = false), List("#"), exchange = firehoseExchange)


  implicit val recoveryStrategy = RecoveryStrategy.nack(false)

  val source =  RabbitSource(
    rabbitControl,
    channel(qos = 3),
    consume(firehoseBinding),
    body(as[JsObject]))

  def run = source.acked.to(Sink.foreach(println _)).run()
}
