package fzn.projects.networkstatistics;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * 服务类
 * 处理后台任务，管理速率通知
 */
public class NetworkStatisticsService extends Service {
    protected static final String TAG = "NetStatService";

    private static NetworkStatisticsService instance = null;
    private NetworkStatisticsReceiver netStatReceiver;
    private IntentFilter intentFilter;

    private ConnectivityManager connManager;
    private NetworkInfo activeInfo;

    private NotificationsDaemon notifDaemon;

    public NetworkStatisticsService() {
    }

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        instance = this;
        netStatReceiver = new NetworkStatisticsReceiver();
        connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        activeInfo = connManager.getActiveNetworkInfo();
        intentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(netStatReceiver, intentFilter);
        notifDaemon = new NotificationsDaemon(this);
        if (connManager.getActiveNetworkInfo() != null)
            notifDaemon.scheduleNotification();
    }

    /**
     * Called by the system every time a client explicitly starts the service by calling
     * {@link Context#startService}, providing the arguments it supplied and a
     * unique integer token representing the start request.  Do not call this method directly.
     * <p/>
     * <p>For backwards compatibility, the default implementation calls
     * {@link #onStart} and returns either {@link #START_STICKY}
     * or {@link #START_STICKY_COMPATIBILITY}.
     * <p/>
     * <p>If you need your application to run on platform versions prior to API
     * level 5, you can use the following model to handle the older {@link #onStart}
     * callback in that case.  The <code>handleCommand</code> method is implemented by
     * you as appropriate:
     * <p/>
     * {@sample development/samples/ApiDemos/src/com/example/android/apis/app/ForegroundService.java
     * start_compatibility}
     * <p/>
     * <p class="caution">Note that the system calls this on your
     * service's main thread.  A service's main thread is the same
     * thread where UI operations take place for Activities running in the
     * same process.  You should always avoid stalling the main
     * thread's event loop.  When doing long-running operations,
     * network calls, or heavy disk I/O, you should kick off a new
     * thread, or use {@link AsyncTask}.</p>
     *
     * @param intent  The Intent supplied to {@link Context#startService},
     *                as given.  This may be null if the service is being restarted after
     *                its process has gone away, and it had previously returned anything
     *                except {@link #START_STICKY_COMPATIBILITY}.
     * @param flags   Additional data about this start request.  Currently either
     *                0, {@link #START_FLAG_REDELIVERY}, or {@link #START_FLAG_RETRY}.
     * @param startId A unique integer representing this specific request to
     *                start.  Use with {@link #stopSelfResult(int)}.
     * @return The return value indicates what semantics the system should
     * use for the service's current started state.  It may be one of the
     * constants associated with the {@link #START_CONTINUATION_MASK} bits.
     * @see #stopSelfResult(int)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.  The
     * service should clean up any resources it holds (threads, registered
     * receivers, etc) at this point.  Upon return, there will be no more calls
     * in to this Service object and it is effectively dead.  Do not call this method directly.
     */
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        instance = null;
        unregisterReceiver(netStatReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        if (intent != null) {
            return new NotificationControlBinder();
        }
        return null;
    }

    /**
     * 判断服务是否已启动
     * @return 服务状态
     */
    public static boolean isServiceCreated() {
        return instance != null;
    }

    /**
     * 获取服务实例
     * 若服务已启动，返回实例，否则返回null
     * @return 服务实例或null
     */
    protected static NetworkStatisticsService getInstance() { return instance != null ? instance : null; }

    /**
     * 通知控制连结类
     * 为控制通知是否显示提供方法
     */
    public class NotificationControlBinder extends Binder {
        /**
         * 切换通知状态方法
         * 根据活动网络的状态选择是否显示通知
         * 若网络可用，调用{@link NotificationsDaemon#scheduleNotification()}
         * 否则调用{@link NotificationsDaemon#cancelNotification()}
         */
        public void switchNotification() {
            if (activeInfo != null) {
                notifDaemon.scheduleNotification();
                Log.d(TAG, "Network Connected.");
            }
            else {
                notifDaemon.cancelNotification();
                Log.d(TAG, "Network Disconnected.");
            }
        }

        /**
         * 显示通知方法
         * 调用{@link NotificationsDaemon#scheduleNotification()}
         */
        public void scheduleNotification() { notifDaemon.scheduleNotification(); }

        /**
         * 取消通知方法
         * 调用{@link NotificationsDaemon#cancelNotification()}
         */
        public void cancelNotification() { notifDaemon.cancelNotification(); }
    }
}
