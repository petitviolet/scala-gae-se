import java.util.logging.Level
import javax.servlet._

import _root_.controller._
import net.petitviolet.gae.infra.Database
import org.slf4j.LoggerFactory
import skinny.micro._

class Bootstrap extends skinny.micro.LifeCycle {
  override def init(ctx: ServletContext): Unit = {
    Initializer.run()
    new UserController().mount(ctx)
    new SelectController().mount(ctx)
  }

}

private object Initializer {
  private lazy val logger =LoggerFactory.getLogger(this.getClass)
  def run() = {
    logger.info("Initializer.run start")
    Database.setup()
    sys.addShutdownHook {
      Database.shutDown()
    }
    logger.info("Initializer.run end")
  }
}

object Main extends App {
  Initializer.run()
  WebServer.mount(new MyController)
    .mount(new TQController)
    .mount(new UserController)
    .port(4567).start()
}

