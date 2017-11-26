import javax.servlet._
import _root_.controller._
import skinny.micro._

class Bootstrap extends skinny.micro.LifeCycle {
  override def init(ctx: ServletContext) {
    myController.mount(ctx)
    tqController.mount(ctx)
  }

}

private object myController extends MyController
private object tqController extends TQController

object Main extends App {
  WebServer.mount(myController).mount(tqController).port(4567).start()
}

