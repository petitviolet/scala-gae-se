package common

import com.google.appengine.api.taskqueue._

trait TaskQueueHelper {
  private val MY_QUEUE = QueueFactory.getQueue("my-queue")

  def pushTask(key: String, body: String) = {
    MY_QUEUE.addAsync(TaskOptions.Builder.withUrl("/_ah/push-handlers/push").payload(s"$key=$body"))
  }

}

object TaskQueueHelper extends TaskQueueHelper
