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
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import fzn.projects.networkstatistics.util.Util;

/**
 * 通知栏守护类
 * 在通知栏产生通知，显示网速、当前所用网络状态、自开机起已用的无线网络
 * 及数据网络流量，并每隔1秒刷新。
 */
public class NotificationsDaemon {
	protected static final String TAG = "NotificationsDaemon";
	
	private Intent notificationIntent;
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
	
	private TimerTask task;
	private Timer timer;

	/**
	 * 通知栏守护类构造方法
	 * @param context 应用上下文
	 */
	public NotificationsDaemon(Context context) {
		notificationIntent = new Intent(context, MainActivity.class);
		baseContext = context;
		res = context.getResources();
		uid = context.getApplicationInfo().uid;
		mNotificationManager = 
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notification = MeterNotification.notify(context, null, "...", "...", notificationIntent);
		
		connMgr = (ConnectivityManager) 
		        context.getSystemService(Context.CONNECTIVITY_SERVICE);
		teleMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

		lastTotalRxBytes = getTotalRxBytes();  
		lastTimeStamp = System.currentTimeMillis();
		//scheduleNotification();
	}

	/**
	 * 显示速率方法
	 * 生成信息并显示速率通知
	 */
	private void showNetSpeed() {
		/*
		if (connMgr.getActiveNetworkInfo() == null) {
			if (notifShowing)
				mNotificationManager.cancel(MeterNotification.NOTIFICATION_TAG, uid);
			notifShowing = false;
			return;
		} else if (!notifShowing)
			notifShowing = true;
		*/
        long nowTotalRxBytes = getTotalRxBytes();
        long nowTimeStamp = System.currentTimeMillis();
        speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));//����ת��
		String strSpeed = Util.byteConverter(speed, true);
		Intent speedIntent = new Intent();
		speedIntent.setAction(Constants.Intent.KEY_INTENT_SPEED_VALUE);
		speedIntent.putExtra(Constants.Extra.SPEED, strSpeed);
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
        /*
        Message msg = mHandler.obtainMessage();
        msg.what = 100;
        msg.obj = String.valueOf(speed) + " kb/s";
        
        mHandler.sendMessage(msg);//更新界面
        */

        notification.contentView.setTextViewText(R.id.notificationRate, strSpeed);
        notification.contentView.setTextViewText(R.id.notificationConnInfo, titleExtra);
        notification.contentView.setTextViewText(R.id.notificationWLANUsed,
				res.getString(R.string.net_speed_notification_used) +
						res.getString(R.string.wlanNotifLable) +
						Util.byteConverter((TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes()), false));
        notification.contentView.setTextViewText(R.id.notificationMobileUsed,
				res.getString(R.string.mobileNotifLable) +
						Util.byteConverter(TrafficStats.getMobileRxBytes(), false));

				mNotificationManager.notify(MeterNotification.NOTIFICATION_TAG, uid, notification);
	}

	/**
	 * 获取接收到的总流量
	 * @return 接收到的总流量
	 */
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
				timer = new Timer();
				task = new ShowSpeedTask();
				timer.schedule(task, 1000, 1000); // 1s后启动任务，每1s执行一次
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
			timer.cancel();
			task.cancel();
			mNotificationManager.cancel(MeterNotification.NOTIFICATION_TAG, uid);
			speed = 0;
			notifShowing = false;
		}
	}

	protected long getSpeed() {
		return speed;
	}

	/**
	 * 显示速率计时任务类
	 * 封装了显示速率方法{@link NotificationsDaemon#showNetSpeed}
	 */
	private class ShowSpeedTask extends TimerTask {

		/**
		 * The task to run should be specified in the implementation of the {@code run()}
		 * method.
		 */
		@Override
		public void run() {
			showNetSpeed();
		}
	}
}
