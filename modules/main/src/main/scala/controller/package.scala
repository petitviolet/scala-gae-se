import org.slf4j.LoggerFactory
import skinny.micro.context.SkinnyContext

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.Try

package object controller {
  private lazy val logger = LoggerFactory.getLogger(getClass)

  def responseFuture[A](aF: Future[A])(implicit ctx: SkinnyContext): Any = {
    if (ctx.request.isAsyncSupported) {
      aF
    } else {
      // AppEngine SE, does not support async
      Try {
        Await.result(aF, 5.seconds)
      }.recover { case t: Throwable =>
        logger.error("something wrong.", t)
        s"failed: ${t.getMessage}"
      }.get
    }
  }

}
