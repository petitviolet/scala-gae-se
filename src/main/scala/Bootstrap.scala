import javax.servlet.ServletContext

import _root_.controller._
import skinny.micro.WebServer

class Bootstrap extends skinny.micro.LifeCycle {
  override def init(ctx: ServletContext) {
    web.mount(ctx)
  }

}

private object web extends MyController

object Main extends App {
  WebServer.mount(web).port(4567).start()
}

