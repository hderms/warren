package misc

import akka.stream.Supervision

object LoggingDecider {
  val decider: Supervision.Decider = { e =>
    println("Unhandled exception in stream", e)
    Supervision.Stop
  }
}
