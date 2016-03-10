package io.retxt.dispatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;



/**
 * A dispatch group is an association of one or more blocks submitted to dispatch queues for asynchronous invocation.
 * Applications may use dispatch groups to wait for the completion of blocks associated with the group.
 * <p>
 * Created by kdubb on 1/30/16.
 */
public class DispatchGroup {

  private static final Logger logger = LogManager.getLogger();



  private static class Notification {

    DispatchQueue queue;
    Block block;

    Notification(DispatchQueue queue, Block block) {
      this.queue = queue;
      this.block = block;
    }

  }



  private AtomicInteger count = new AtomicInteger();
  private List<Notification> notifications = new ArrayList<>();
  private CountDownLatch completionLatch = new CountDownLatch(1);

  public DispatchGroup() {
  }

  public synchronized void setNotification(DispatchQueue notificationQueue, Block block) {

    notifications.add(new Notification(notificationQueue, block));

    if(count.get() == 0) {
      broadcastNotifications();
    }
  }

  public void enter() {

    // reset completion latch on when starting a new block
    if(completionLatch.getCount() == 0) {
      completionLatch = new CountDownLatch(1);
    }

    count.incrementAndGet();
  }

  public void leave() {

    int count = this.count.decrementAndGet();
    if(count == 0) {

      logger.trace("Group completed");

      broadcastNotifications();

      completionLatch.countDown();
    }
    else if(count < 0) {
      throw new IllegalStateException("Mismatched enter & leave");
    }

  }

  public void dispatch(DispatchQueue queue, Block block) {

    enter();

    queue.dispatch(() -> {
      try {

        block.run();

      }
      finally {
        leave();
      }
    });
  }

  public boolean waitForCompletion(long timeout, TimeUnit timeUnit) throws InterruptedException {

    return completionLatch.await(timeout, timeUnit);
  }

  private synchronized void broadcastNotifications() {

    for(Notification notification : notifications) {
      notification.queue.dispatch(notification.block);
    }

    notifications.clear();
  }

}
