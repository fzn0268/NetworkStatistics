package fzn.projects.networkstatistics;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import fzn.projects.networkstatistics.util.Util;

/**
 * 通知栏守护
 * 在通知栏产生通知，显示网速、当前所用网络状态、自开机起已用的无线网络
 * 及数据网络流量，并每隔1秒刷新。
 */
public class NotificationsDaemon {
	protected static final String TAG = NotificationsDaemon.class.getSimpleName();
	private static final int INTERVAL = 1000;

	private Context baseContext;
	private Resources res;
	private NotificationManager mNotificationManager;
	private Notification notification;
	private ConnectivityManager connMgr;
	private TelephonyManager teleMgr;
	private WifiManager wifiMgr;
	private boolean notifShowing;
	
	private long lastTotalRxBytes = 0;
	private long lastTimeStamp = 0;
	private long speed;
	
	private int uid;
	
	private String titleExtra;

	private final Handler showSpeedHandler = new Handler(Looper.getMainLooper());
	private final Runnable showSpeedRunnable = new ShowSpeedRunnable();

	public NotificationsDaemon(@android.support.annotation.NonNull Context context) {
		Intent notificationIntent = new Intent(context, MainActivity.class);
		baseContext = context;
		res = context.getResources();
		uid = context.getApplicationInfo().uid;
		mNotificationManager = 
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notification = MeterNotification.notify(context, "...", notificationIntent);
		
		connMgr = (ConnectivityManager) 
		        context.getSystemService(Context.CONNECTIVITY_SERVICE);
		teleMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

		lastTotalRxBytes = getTotalRxBytes();  
		lastTimeStamp = System.currentTimeMillis();
	}

	/**
	 * 显示速率
	 * 生成信息并显示通知
	 */
	private void showNetSpeed() {
        long nowTotalRxBytes = getTotalRxBytes();
        long nowTimeStamp = System.currentTimeMillis();
        speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));//毫秒转换
		String strSpeed = Util.byteConverter(speed, true, "0.##");
		Intent speedIntent = new Intent();
		speedIntent.setAction(Constants.Intent.ACTION_NET_INFO);
		speedIntent.putExtra(Constants.Extra.SPEED, speed);
		LocalBroadcastManager.getInstance(baseContext).sendBroadcast(speedIntent);
        
        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;
        switch (connMgr.getActiveNetworkInfo().getType()) {
			case ConnectivityManager.TYPE_WIFI:
			{
				WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
				String extra = wifiInfo.getSSID();
				titleExtra = extra.substring(1, extra.length() - 1) +
						"(" + WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 101) + "%)";
				break;
			}
			case ConnectivityManager.TYPE_MOBILE:
			{
				titleExtra = teleMgr.getNetworkOperatorName();
				break;
			}
		}

        notification.contentView.setTextViewText(R.id.notificationRate, strSpeed);
        notification.contentView.setTextViewText(R.id.notificationConnInfo, titleExtra);
        notification.contentView.setTextViewText(R.id.notificationWLANUsed,
				res.getString(R.string.net_speed_notification_used) +
						res.getString(R.string.wlanNotifLable) +
						Util.byteConverter((TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes()), false, "0.##"));
        notification.contentView.setTextViewText(R.id.notificationMobileUsed,
				res.getString(R.string.mobileNotifLable) +
						Util.byteConverter(TrafficStats.getMobileRxBytes(), false, "0.##"));

		mNotificationManager.notify(MeterNotification.TAG, uid, notification);
	}

    private long getTotalRxBytes() {
        return TrafficStats.getUidRxBytes(uid) == 
        		TrafficStats.UNSUPPORTED ? 0 : TrafficStats.getTotalRxBytes();
    }

	/**
	 * 设定定时，显示通知
	 */
	protected void scheduleNotification() {
		if (connMgr.getActiveNetworkInfo() != null) {
			if (!notifShowing) {
				showSpeedHandler.post(showSpeedRunnable);
				notifShowing = true;
			}
		}
		else Log.d(TAG, "Network unavailable.");
	}

	/**
	 * 取消定时
	 */
	protected void cancelNotification() {
		if (notifShowing) {
			showSpeedHandler.removeCallbacks(showSpeedRunnable);
			mNotificationManager.cancel(MeterNotification.TAG, uid);
			speed = 0;
			notifShowing = false;
		}
	}

	private class ShowSpeedRunnable implements Runnable {
		/**
		 * Starts executing the active part of the class' code. This method is
		 * called when a thread is started that has been created with a class which
		 * implements {@code Runnable}.
		 */
		@Override
		public void run() {
			showNetSpeed();
			showSpeedHandler.postDelayed(this, INTERVAL);
		}
	}
}
