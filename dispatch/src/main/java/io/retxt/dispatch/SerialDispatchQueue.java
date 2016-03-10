package io.retxt.dispatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;



/**
 * Serial dispatch queue
 * <p>
 * Executes blocks in sequentially in FIFO order on a target dispatch queue.
 * <p>
 * Created by kdubb on 11/21/14.
 */
public class SerialDispatchQueue extends InternalDispatchQueue implements UserDispatchQueue {

  private static final Logger logger = LogManager.getLogger();

  private DispatchQueue target;
  private ConcurrentLinkedQueue<Block> blockQueue = new ConcurrentLinkedQueue<>();
  private AtomicBoolean suspended = new AtomicBoolean(false);
  private boolean dispatching = false;

  public SerialDispatchQueue(DispatchQueue target) {
    this.target = target;
  }

  @Override
  public void _dispatch(Block block) {

    blockQueue.add(block);

    if(suspended.get()) {
      return;
    }

    ensureDispatching();
  }

  @Override
  public boolean isSuspended() {
    return suspended.get();
  }

  @Override
  public void suspend() {
    suspended.set(true);
  }

  @Override
  public void resume() {

    if(suspended.compareAndSet(true, false)) {

      ensureDispatching();

    }

  }

  private synchronized void ensureDispatching() {

    if(!dispatching && !blockQueue.isEmpty()) {

      logger.trace("Restarting dispatcher");

      dispatching = true;

      target.dispatch(this::dispatch);
    }

  }

  void dispatch() {

    try {

      blockQueue.poll().run();

    }
    catch(Throwable e) {

      Thread thread = Thread.currentThread();
      thread.getUncaughtExceptionHandler().uncaughtException(thread, e);

    }
    finally {

      synchronized(this) {

        if(blockQueue.isEmpty() || suspended.get()) {

          logger.trace("Stopped dispatcher");

          dispatching = false;
        }
        else {

          target.dispatch(this::dispatch);
        }

      }

    }

  }

}
