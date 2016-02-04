package io.retxt.dispatch.android;

import android.os.Looper;
import io.retxt.dispatch.DispatchQueue;
import io.retxt.dispatch.PlatformQueues;



/**
 * Android platform queues implementation.
 * <p>
 * Created by kdubb on 1/28/16.
 */
public class AndroidPlatformQueues implements PlatformQueues {

  @Override
  public DispatchQueue createMainQueue() {
    return new LooperQueue(Looper.getMainLooper());
  }

  @Override
  public DispatchQueue createUIQueue() {
    return new LooperQueue(Looper.getMainLooper());
  }

}
