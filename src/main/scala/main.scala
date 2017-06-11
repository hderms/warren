package warren

import akka.actor.{ActorSystem, Props}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import com.spingo.op_rabbit.{RabbitControl, SubscriptionRef}
import sync.Sync
import rabbitadmin.RabbitAdmin
import sync.db.Db.FireHoseDb

import scala.util.{Failure, Success}
/**
  * Created by rtuser on 6/10/17.
  */


  object LoggingDecider {
  val decider: Supervision.Decider = { e =>
    println("Unhandled exception in stream", e)
    Supervision.Stop
  }
}


object Main extends App {
  implicit val actorSystem = ActorSystem("such-system")
  val actorMaterializerSettings = ActorMaterializerSettings(actorSystem).withSupervisionStrategy(LoggingDecider.decider)
  implicit val materializer = ActorMaterializer(actorMaterializerSettings)(actorSystem)
  implicit val ec = materializer.executionContext
  val rabbitControl = actorSystem.actorOf(Props[RabbitControl])
  val sync = new Sync(rabbitControl)(FireHoseDb.SyncFlow)
  RabbitAdmin.firehose.start
  //FireHoseDb.init
  sync.run { _ => RabbitAdmin.firehose.stop }
}
