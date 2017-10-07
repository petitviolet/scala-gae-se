import javax.servlet.ServletContext

import _root_.controller._

class Bootstrap extends skinny.micro.LifeCycle {
  override def init(ctx: ServletContext) {
    Web.mount(ctx)
  }
}

//object Main extends App {
//  WebServer.mount(Web).port(4567).start()
//}

