package fzn.projects.networkstatistics.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.SparseArray;

import org.joda.time.LocalTime;

import java.util.HashMap;

import fzn.projects.networkstatistics.Constants;
import fzn.projects.networkstatistics.db.NetworkStatisticsContract.ComboEntry;
import fzn.projects.networkstatistics.db.NetworkStatisticsDbHelper;

/**
 * Created by fzn on 15-6-21.
 */
public class RulesDaemon {
    private static final String TAG = RulesDaemon.class.getSimpleName();

    @android.support.annotation.NonNull
    private final Context mContext;
    @android.support.annotation.NonNull
    private final AlarmManager mAlarmMgr;
    @android.support.annotation.NonNull
    private final ConnectivityManager mConnMgr;
    private final LocalBroadcastManager mLocalBcMgr;
    @android.support.annotation.NonNull
    private final IntentFilter mRulesDaemonIntentFilter;
    @android.support.annotation.NonNull
    private final BroadcastReceiver mRulesDaemonBcRecvr;
    private final SparseArray<Rule> rules = new SparseArray<>();
    private final HashMap<Long, Rule> inIntervalRules = new HashMap<>();
    private final SparseArray<Rule> runningRules = new SparseArray<>();
    private boolean hasAllDayRule;
    private SQLiteDatabase mDb;
    private Cursor mCursor;
    private Handler mHandler;
    private final String[] projection = new String[] { ComboEntry._ID, ComboEntry.COLUMN_CONN,
            ComboEntry.COLUMN_PERIOD, ComboEntry.COLUMN_QUANTUM, ComboEntry.COLUMN_USED,
            ComboEntry.COLUMN_PRIORITY,
            ComboEntry.COLUMN_TIME_INTERVAL_FROM, ComboEntry.COLUMN_TIME_INTERVAL_TO};

    public RulesDaemon(@android.support.annotation.NonNull Context context) {
        mContext = context;
        mAlarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mConnMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        mLocalBcMgr = LocalBroadcastManager.getInstance(context);
        mRulesDaemonIntentFilter = new IntentFilter(Constants.Intent.ACTION_RULE_CHANGED);
        mRulesDaemonIntentFilter.addAction(Constants.Intent.ACTION_RULE_DELETED);
        mRulesDaemonBcRecvr = new RulesDaemonBroadcastReceiver();
        mDb = NetworkStatisticsDbHelper.getInstance(context).getWritableDatabase();
        hasAllDayRule = false;
    }

    private int loadRules() {
        Log.d(TAG, "loadRules");
        mCursor = mDb.query(ComboEntry.TABLE_NAME, projection,
                null, null, null, null, ComboEntry.COLUMN_PRIORITY);
        rules.clear();
        for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
            Rule rule = new Rule(mCursor.getLong(mCursor.getColumnIndex(ComboEntry._ID)),
                    (byte) mCursor.getShort(mCursor.getColumnIndex(ComboEntry.COLUMN_CONN)),
                    Util.resolveComboPeriod(mCursor.getString(mCursor.getColumnIndex(ComboEntry.COLUMN_PERIOD))),
                    mCursor.getLong(mCursor.getColumnIndex(ComboEntry.COLUMN_QUANTUM)),
                    mCursor.getInt(mCursor.getColumnIndex(ComboEntry.COLUMN_PRIORITY)),
                    mCursor.getString(mCursor.getColumnIndex(ComboEntry.COLUMN_TIME_INTERVAL_FROM)),
                    mCursor.getString(mCursor.getColumnIndex(ComboEntry.COLUMN_TIME_INTERVAL_TO))
            );
            rules.put((int) rule.id, rule);
            if (rule.isAllDay)
                hasAllDayRule = true;
            registerRule(rule);
        }
        if (hasAllDayRule) {
            Intent intent = new Intent(mContext, RulesDaemonBroadcastReceiver.class);
            intent.setAction(Constants.Intent.ACTION_RULE_ALARM);
            mAlarmMgr.setInexactRepeating(AlarmManager.RTC,
                    LocalTime.parse("00:00").toDateTimeToday().getMillis(),
                    AlarmManager.INTERVAL_DAY,
                    PendingIntent.getBroadcast(mContext,
                            Constants.RULE_ALARM_PENDINGINTENT_REQUEST,
                            intent, 0));
        }
        mCursor.close();
        return mCursor.getCount();
    }

    private void reloadRule(long id) {
        Log.d(TAG, "reloadRule");
        mCursor = mDb.query(ComboEntry.TABLE_NAME, projection,
                ComboEntry._ID + " LIKE ?",
                new String[] { String.valueOf(id) }, null, null, null);
        mCursor.moveToFirst();
        Rule rule = new Rule(mCursor.getLong(mCursor.getColumnIndex(ComboEntry._ID)),
                (byte) mCursor.getShort(mCursor.getColumnIndex(ComboEntry.COLUMN_CONN)),
                Util.resolveComboPeriod(mCursor.getString(mCursor.getColumnIndex(ComboEntry.COLUMN_PERIOD))),
                mCursor.getLong(mCursor.getColumnIndex(ComboEntry.COLUMN_QUANTUM)),
                mCursor.getInt(mCursor.getColumnIndex(ComboEntry.COLUMN_PRIORITY)),
                mCursor.getString(mCursor.getColumnIndex(ComboEntry.COLUMN_TIME_INTERVAL_FROM)),
                mCursor.getString(mCursor.getColumnIndex(ComboEntry.COLUMN_TIME_INTERVAL_TO))
        );
        rules.put((int) id, rule);
        registerRule(rule);
        mCursor.close();
    }

    private void removeRule(long id) {
        Log.d(TAG, "removeRule");
         if (rules.get((int) id) != null) {
            unregisterRule(rules.get((int) id));
            rules.delete((int) id);
        }
    }

    private void registerRule(@android.support.annotation.NonNull Rule rule) {
        Log.d(TAG, "registerRule");
        if (rule.isAllDay) {
            inIntervalRules.put(rule.id, rule);
            beginRule(rule);
        }
        else {
            Intent intent = new Intent(mContext, RulesDaemonBroadcastReceiver.class);
            intent.setAction(Constants.Intent.ACTION_RULE_ALARM);
            intent.putExtra(Constants.Extra.COMBO_ID, rule.id);
            intent.putExtra(Constants.Extra.COMBO_TIMEFROM, rule.timeFrom.toString());
            rule.timeFromIntent = PendingIntent.getBroadcast(mContext,
                    Constants.RULE_ALARM_PENDINGINTENT_REQUEST,
                    intent, 0);
            mAlarmMgr.setInexactRepeating(AlarmManager.RTC,
                    rule.timeFrom.toDateTimeToday().getMillis(),
                    AlarmManager.INTERVAL_DAY,
                    rule.timeFromIntent);
            intent = new Intent(mContext, RulesDaemonBroadcastReceiver.class);
            intent.setAction(Constants.Intent.ACTION_RULE_ALARM);
            intent.putExtra(Constants.Extra.COMBO_ID, rule.id);
            intent.putExtra(Constants.Extra.COMBO_TIMETO, rule.timeTo.toString());
            rule.timeToIntent = PendingIntent.getBroadcast(mContext,
                    Constants.RULE_ALARM_PENDINGINTENT_REQUEST,
                    intent, 0);
            mAlarmMgr.setInexactRepeating(AlarmManager.RTC,
                    rule.timeTo.toDateTimeToday().getMillis(),
                    AlarmManager.INTERVAL_DAY,
                    rule.timeToIntent);
            if (rule.checkTime())
                beginRule(rule);
        }
    }

    private void unregisterRule(@android.support.annotation.NonNull Rule rule) {
        Log.d(TAG, "unregisterRule");
        if (inIntervalRules.get(rule.id) != null) {
            endRule(rule);
            inIntervalRules.remove(rule.id);
            if (!rule.isAllDay) {
                mAlarmMgr.cancel(rule.timeFromIntent);
                rule.timeFromIntent.cancel();
                mAlarmMgr.cancel(rule.timeToIntent);
                rule.timeToIntent.cancel();
            }
        }
    }

    private void beginRule(@android.support.annotation.NonNull Rule rule) {
        Log.d(TAG, "beginRule");
        // check priorities between parameter and every of currents.
        for (Rule curRule : inIntervalRules.values()) {
            if (rule.conn == curRule.conn) {
                if (rule.priority > curRule.priority) {
                    rule.obtainDataUsedAndStart();
                    curRule.commitDataUsedAndStop();
                    runningRules.put((int) rule.id, rule);
                } else if (rule.priority == curRule.priority) {
                    rule.obtainDataUsedAndStart();
                    runningRules.put((int) rule.id, rule);
                }
            }
        }
    }

    private void endRule(@android.support.annotation.NonNull Rule rule) {
        Log.d(TAG, "endRule");
        if (runningRules.get((int) rule.id) != null) {
            for (Rule curRule : inIntervalRules.values()) {
                if (rule.conn == curRule.conn) {
                    if (rule.priority > curRule.priority) {
                        rule.commitDataUsedAndStop();
                        curRule.obtainDataUsedAndStart();
                        runningRules.remove((int) rule.id);
                    } else if (rule.priority == curRule.priority) {
                        rule.commitDataUsedAndStop();
                        runningRules.remove((int) rule.id);
                    }
                }
            }
        }
    }

    public void start() {
        Log.d(TAG, "start");
        if (loadRules() == 0) {
            Log.i(TAG, "No rule exist.");
            return;
        }
        mLocalBcMgr.registerReceiver(mRulesDaemonBcRecvr, mRulesDaemonIntentFilter);
        mContext.registerReceiver(mRulesDaemonBcRecvr, new IntentFilter("android.intent.action.ACTION_SHUTDOWN"));
    }

    public void stop() {
        Log.d(TAG, "stop");
        for (Rule rule : inIntervalRules.values()) {
            unregisterRule(rule);
        }
        mLocalBcMgr.unregisterReceiver(mRulesDaemonBcRecvr);
        mContext.unregisterReceiver(mRulesDaemonBcRecvr);
    }

    private class Rule {
        protected final long id;
        private final byte conn;
        private final short period, periodUnit;
        private final long quantum;
        private final int priority;
        private LocalTime timeFrom, timeTo;
        protected final boolean isAllDay;
        protected long curUsed, oldUsed;
        protected PendingIntent timeFromIntent, timeToIntent;

        public Rule(long id, byte conn, short[] period, long quantum, int priority, @android.support.annotation.NonNull String timeFrom, String timeTo) {
            this.id = id;
            this.conn = conn;
            this.period = period[0];
            this.periodUnit = period[1];
            this.quantum = quantum;
            this.priority = priority;
            if (timeFrom.equals(timeTo)) {
                isAllDay = true;
            }
            else {
                isAllDay = false;
                this.timeFrom = LocalTime.parse(timeFrom);
                this.timeTo = LocalTime.parse(timeTo);
            }
        }

        protected boolean checkTime() {
            Log.d(TAG, "checkTime");
            if (isAllDay)
                return true;
            LocalTime nowTime = LocalTime.now();
            return nowTime.isAfter(timeFrom) && nowTime.isBefore(timeTo);
        }

        protected synchronized void obtainDataUsedAndStart() {
            Log.d(TAG, "obtainDataUsedAndStart");
            Cursor c = mDb.query(ComboEntry.TABLE_NAME, new String[]{ComboEntry.COLUMN_USED},
                    ComboEntry._ID + " LIKE ?", new String[]{String.valueOf(id)},
                    null, null, null);
            c.moveToFirst();
            oldUsed = c.getLong(c.getColumnIndex(ComboEntry.COLUMN_USED));
            c.close();
            if (conn == ConnectivityManager.TYPE_MOBILE)
                curUsed = TrafficStats.getMobileTxBytes() + TrafficStats.getMobileRxBytes();
            else
                curUsed = TrafficStats.getTotalTxBytes() + TrafficStats.getTotalRxBytes()
                        - TrafficStats.getMobileTxBytes() - TrafficStats.getMobileRxBytes();
        }

        protected synchronized void commitDataUsedAndStop() {
            Log.d(TAG, "commitDataUsedAndStop");
            if (conn == ConnectivityManager.TYPE_MOBILE)
                curUsed = TrafficStats.getMobileTxBytes() + TrafficStats.getMobileRxBytes() - curUsed;
            else
                curUsed = TrafficStats.getTotalTxBytes() + TrafficStats.getTotalRxBytes()
                        - TrafficStats.getMobileTxBytes() - TrafficStats.getMobileRxBytes() - curUsed;
            ContentValues values = new ContentValues();
            values.put(ComboEntry.COLUMN_USED, curUsed + oldUsed);
            mDb.update(ComboEntry.TABLE_NAME, values,
                    ComboEntry._ID + " LIKE ?", new String[] { String.valueOf(id) });
        }
    }

    private class RulesDaemonBroadcastReceiver extends BroadcastReceiver {
        /**
         * This method is called when the BroadcastReceiver is receiving an Intent
         * broadcast.  During this time you can use the other methods on
         * BroadcastReceiver to view/modify the current result values.  This method
         * is always called within the main thread of its process, unless you
         * explicitly asked for it to be scheduled on a different thread using
         * {@link Context#registerReceiver(BroadcastReceiver,
         * IntentFilter, String, Handler)}. When it runs on the main
         * thread you should
         * never perform long-running operations in it (there is a timeout of
         * 10 seconds that the system allows before considering the receiver to
         * be blocked and a candidate to be killed). You cannot launch a popup dialog
         * in your implementation of onReceive().
         * <p>
         * <p><b>If this BroadcastReceiver was launched through a &lt;receiver&gt; tag,
         * then the object is no longer alive after returning from this
         * function.</b>  This means you should not perform any operations that
         * return a result to you asynchronously -- in particular, for interacting
         * with services, you should use
         * {@link Context#startService(Intent)} instead of
         * {@link Context#bindService(Intent, ServiceConnection, int)}.  If you wish
         * to interact with a service that is already running, you can use
         * {@link #peekService}.
         * <p>
         * <p>The Intent filters used in {@link Context#registerReceiver}
         * and in application manifests are <em>not</em> guaranteed to be exclusive. They
         * are hints to the operating system about how to find suitable recipients. It is
         * possible for senders to force delivery to specific recipients, bypassing filter
         * resolution.  For this reason, {@link #onReceive(Context, Intent) onReceive()}
         * implementations should respond only to known actions, ignoring any unexpected
         * Intents that they may receive.
         *
         * @param context The Context in which the receiver is running.
         * @param intent  The Intent being received.
         */
        @Override
        public void onReceive(Context context, @android.support.annotation.NonNull Intent intent) {
            Log.d(TAG, "onReceive");
            long rowID = 0;
            if (intent.getAction().equals(Constants.Intent.ACTION_RULE_CHANGED)) {
                rowID = intent.getLongExtra(Constants.Extra.COMBO_ID, -1);
                Log.d(TAG, "rowID" + rowID);
                reloadRule(rowID);
                return;
            }
            if (intent.getAction().equals(Constants.Intent.ACTION_RULE_DELETED)) {
                rowID = intent.getLongExtra(Constants.Extra.COMBO_ID, -1);
                Log.d(TAG, "rowID" + rowID);
                removeRule(rowID);
                return;
            }
            if (intent.getAction().equals(Constants.Intent.ACTION_RULE_ALARM)) {
                if (hasAllDayRule) {
                    for (int i = 0; i < runningRules.size(); i++) {
                        if (runningRules.get(i) != null && runningRules.get(i).isAllDay) {
                            runningRules.get(i).commitDataUsedAndStop();
                            runningRules.get(i).obtainDataUsedAndStart();
                        }
                    }
                }
                rowID  = intent.getLongExtra(Constants.Extra.COMBO_ID, -1);
                Rule rule = rules.get((int) rowID);
                if (intent.hasExtra(Constants.Extra.COMBO_TIMEFROM)) {
                    beginRule(rule);
                    inIntervalRules.put(rowID, rule);
                    return;
                }
                if (intent.hasExtra(Constants.Extra.COMBO_TIMETO)) {
                    inIntervalRules.remove(rowID);
                    endRule(rule);
                    return;
                }
            }
            if (intent.getAction().equals(Constants.Intent.ACTION_RULE_UPDATE)) {
                for (int i = 0; i < runningRules.size(); i++) {
                    if (runningRules.get(i) != null) {
                        runningRules.get(i).commitDataUsedAndStop();
                        runningRules.get(i).obtainDataUsedAndStart();
                    }
                }
            }
            if (intent.getAction().equals("android.intent.action.ACTION_SHUTDOWN")) {
                stop();
            }
        }
    }

}
