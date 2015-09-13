package fzn.projects.networkstatistics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

/**
 * 广播接收
 * 开机启动，根据网络情况和屏幕状态显示或取消通知栏信息
 */
public class NetworkStatisticsReceiver extends BroadcastReceiver {
    protected static final String TAG = "NetStatReceiver";
    private SharedPreferences mSharedPref;
    private SharedPreferences.Editor mEditor;
    public NetworkStatisticsReceiver() {
    }

    @Override
    public void onReceive(@android.support.annotation.NonNull Context context, @android.support.annotation.NonNull Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Intent innerIntent = new Intent(context, NetworkStatisticsService.class);
        innerIntent.setAction(intent.getAction());
        IBinder binder = peekService(context, innerIntent);
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            mSharedPref = context.getSharedPreferences(Constants.SHARED_PREFERENCES,
                    Context.MODE_PRIVATE);
            mEditor = mSharedPref.edit();
            mEditor.putLong(Constants.BOOT_TIMESTAMP, System.currentTimeMillis());
            mEditor.apply();
            context.startService(innerIntent);
        }
        else {
            if (binder != null) {
                if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                    Log.d(TAG, intent.getAction());
                    ((NetworkStatisticsService.NotificationControlBinder) binder).cancelNotification();
                    return;
                }
                Log.d(TAG, intent.getAction());
                ((NetworkStatisticsService.NotificationControlBinder) binder).switchNotification();
            }
        }
    }
}
