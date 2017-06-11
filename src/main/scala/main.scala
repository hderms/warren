
import akka.actor.{ActorSystem, Props}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import com.spingo.op_rabbit.RabbitControl
import rabbitadmin.RabbitAdmin
import sync.Sync
import sync.db.Db.FireHoseDb
import sync.db.reactive.Reactive

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
  val sync = new Sync(rabbitControl)(Reactive.subscriber(FireHoseDb.client))
  RabbitAdmin.firehose.start
  http.Service.builder.start.runAsync(x => println(x))
  sync.run { _ => RabbitAdmin.firehose.stop }
}
