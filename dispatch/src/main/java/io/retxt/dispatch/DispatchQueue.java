package io.retxt.dispatch;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;



/**
 * Primary dispatch queue interface.
 *
 * Created by kdubb on 11/21/14.
 */
public interface DispatchQueue extends Executor {

  void executeSync(Runnable task);

  boolean executeSync(long timeout, TimeUnit timeUnit, Runnable task);

  void execute(Runnable task);

  void executeAfter(long time, TimeUnit timeUnit, Runnable runnable);

}
