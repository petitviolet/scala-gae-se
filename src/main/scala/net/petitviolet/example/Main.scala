package net.petitviolet.example

import javax.servlet.ServletContext

import skinny.logging.Logger
import skinny.micro._

object Web extends WebApp {
  override protected val logger = Logger(this.getClass)
  get("/say-hello") {
    logger.info(s"params: $params")
    s"Hello, ${params.getOrElse("name", "Anonymous")}!\n"
  }
}

class Bootstrap extends LifeCycle {
  override def init(ctx: ServletContext) {
    Web.mount(ctx)
  }
}
