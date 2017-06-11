package warren

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import com.spingo.op_rabbit.{RabbitControl, SubscriptionRef}
import sync.Sync
import rabbitadmin.RabbitAdmin
/**
  * Created by rtuser on 6/10/17.
  */


object Main extends App {
  implicit val actorSystem = ActorSystem("such-system")
  implicit val materializer = ActorMaterializer()(actorSystem)
  val rabbitControl = actorSystem.actorOf(Props[RabbitControl])
  val sync = new Sync(rabbitControl)
  RabbitAdmin.firehose.start
  val runRef: SubscriptionRef = sync.run{ _ => RabbitAdmin.firehose.stop}
}
