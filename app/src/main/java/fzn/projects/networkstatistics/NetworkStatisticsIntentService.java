package fzn.projects.networkstatistics;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * 意图服务类
 * 待使用
 */
public class NetworkStatisticsIntentService extends IntentService {
	protected static final String TAG = "NetStatIntentService";

	/**
	 * Creates an IntentService.  Invoked by your subclass's constructor.
	 *
	 * @param name Used to name the worker thread, important only for debugging.
	 */
	public NetworkStatisticsIntentService(String name) {
		super(name);
	}

	public NetworkStatisticsIntentService() {
		this(TAG);
	}

	/**
	 * Sets intent redelivery preferences.  Usually called from the constructor
	 * with your preferred semantics.
	 * <p/>
	 * <p>If enabled is true,
	 * {@link #onStartCommand(Intent, int, int)} will return
	 * {@link Service#START_REDELIVER_INTENT}, so if this process dies before
	 * {@link #onHandleIntent(Intent)} returns, the process will be restarted
	 * and the intent redelivered.  If multiple Intents have been sent, only
	 * the most recent one is guaranteed to be redelivered.
	 * <p/>
	 * <p>If enabled is false (the default),
	 * {@link #onStartCommand(Intent, int, int)} will return
	 * {@link Service#START_NOT_STICKY}, and if the process dies, the Intent
	 * dies along with it.
	 *
	 * @param enabled
	 */
	@Override
	public void setIntentRedelivery(boolean enabled) {
		super.setIntentRedelivery(enabled);
	}

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind");
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/**
	 * This method is invoked on the worker thread with a request to process.
	 * Only one Intent is processed at a time, but the processing happens on a
	 * worker thread that runs independently from other application logic.
	 * So, if this code takes a long time, it will hold up other requests to
	 * the same IntentService, but it will not hold up anything else.
	 * When all requests have been handled, the IntentService stops itself,
	 * so you should not call {@link #stopSelf}.
	 *
	 * @param intent The value passed to {@link
	 *               Context#startService(Intent)}.
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "onHandleIntent");
	}

}
