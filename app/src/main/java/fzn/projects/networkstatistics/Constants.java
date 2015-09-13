package fzn.projects.networkstatistics;

import android.net.ConnectivityManager;

/**
 * Created by FzN on 15/6/7.
 * 常量
 * 管理应用内使用的常量
 */
public final class Constants {
    public static final char[] TIME_UNIT = { 'm', 'd', 'w', 'y' }; // 时间单位
    public static final int[] SUPPORTED_NETWORK_TYPE = {ConnectivityManager.TYPE_MOBILE, ConnectivityManager.TYPE_WIFI};
    public static final int RULE_ADD_REQUEST = 0;
    public static final int RULE_EDIT_REQUEST = 1;
    public static final int RULE_ALARM_PENDINGINTENT_REQUEST = 2;
    public static final String SHARED_PREFERENCES = Constants.class.getPackage().getName() + ".SHARED_PREFERENCES";

    public static final String BOOT_TIMESTAMP = "BOOT_TIMESTAMP";
    public static final class Intent {
        public static final String ACTION_NET_INFO = Constants.class.getPackage().getName() + ".ACTION_NET_INFO";
        public static final String ACTION_RULE_CHANGED = Constants.class.getPackage().getName() + ".ACTION_RULE_CHANGED";
        public static final String ACTION_RULE_DELETED = Constants.class.getPackage().getName() + ".ACTION_RULE_DELETED";
        public static final String ACTION_RULE_ALARM = Constants.class.getPackage().getName() + ".ACTION_RULE_ALARM";
        public static final String ACTION_RULE_UPDATE = Constants.class.getPackage().getName() + ".ACTION_RULE_UPDATE";
    }
    public static final class Extra {
        public static final String SPEED = "SPEED";
        public static final String COMBO_ID = "COMBOID";
        public static final String COMBO_TIMEFROM = "RULE_TIMEFROM";
        public static final String COMBO_TIMETO = "RULE_TIMETO";
    }
}
