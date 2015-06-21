package fzn.projects.networkstatistics.db;

import fzn.projects.networkstatistics.db.NetworkStatisticsContract.AppEntry;
import fzn.projects.networkstatistics.db.NetworkStatisticsContract.ComboEntry;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 *  数据库辅助类
 *  提供数据库生成、更新方法
 */
public class NetworkStatisticsDbHelper extends SQLiteOpenHelper {
	
	public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "NetworkStatistics.db";
    
    private static final String COMMA = ",";
    private static final String SPACE = " ";

	/**
	 * 创建应用信息表命令
	 */
    private static final String CREATE_APPLICATION_TABLE =
    		"CREATE TABLE " + AppEntry.TABLE_NAME + "(" +
    				AppEntry._ID + " INTEGER PRIMARY KEY," +
    				AppEntry.COLUMN_UID + SPACE + AppEntry.COLUMN_UID_TYPE + COMMA +
    				AppEntry.COLUMN_PACKAGE_NAME + SPACE + AppEntry.COLUMN_PACKAGE_NAME_TYPE + COMMA +
    				AppEntry.COLUMN_WL_TRANSMITTED + SPACE + AppEntry.TYPE_INT + COMMA +
    				AppEntry.COLUMN_WL_RECEIVED + SPACE + AppEntry.TYPE_INT + COMMA +
    				AppEntry.COLUMN_MO_TRANSMITTED + SPACE + AppEntry.TYPE_INT + COMMA +
    				AppEntry.COLUMN_MO_RECEIVED + SPACE + AppEntry.TYPE_INT + COMMA +
    				AppEntry.COLUMN_EXIST + SPACE + AppEntry.COLUMN_EXIST_TYPE + ")";

	/**
	 * 创建规则表命令
	 */
    private static final String CREATE_COMBO_TABLE =
    		"CREATE TABLE " + ComboEntry.TABLE_NAME + "(" +
    				ComboEntry._ID + " INTEGER PRIMARY KEY," +
    				ComboEntry.COLUMN_TIMESTAMP + SPACE + ComboEntry.COLUMN_TIMESTAMP_TYPE + COMMA +
    				ComboEntry.COLUMN_COMBO_NAME + SPACE + ComboEntry.COLUMN_COMBO_NAME_TYPE + COMMA +
    				ComboEntry.COLUMN_COMBO_CONN + SPACE + ComboEntry.COLUMN_COMBO_CONN_TYPE + COMMA +
    				ComboEntry.COLUMN_QUANTUM + SPACE + ComboEntry.COLUMN_QUANTUM_TYPE + COMMA +
    				ComboEntry.COLUMN_PERIOD + SPACE + ComboEntry.COLUMN_PERIOD_TYPE + COMMA +
					ComboEntry.COLUMN_PERIOD_REMAIN + SPACE + ComboEntry.COLUMN_PERIOD_TYPE + COMMA +
    				ComboEntry.COLUMN_TIME_RANGE_FROM + SPACE + ComboEntry.COLUMN_TIME_RANGE_FROM_TYPE + COMMA +
    				ComboEntry.COLUMN_TIME_RANGE_TO + SPACE + ComboEntry.COLUMN_TIME_RANGE_TO_TYPE + COMMA +
    				ComboEntry.COLUMN_USED + SPACE + ComboEntry.COLUMN_USED_TYPE + ")";
    
    private static final String DELETE_APPLICATION_TABLE =
    		"DROP TABLE IF EXISTS " + AppEntry.TABLE_NAME;
    
    private static final String DELETE_COMBO_TABLE =
    		"DROP TABLE IF EXISTS " + ComboEntry.TABLE_NAME;

	public NetworkStatisticsDbHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO 自动生成的构造函数存根
	}

	public NetworkStatisticsDbHelper(Context context, String name,
			CursorFactory factory, int version,
			DatabaseErrorHandler errorHandler) {
		super(context, name, factory, version, errorHandler);
		// TODO 自动生成的构造函数存根
	}
	
	public NetworkStatisticsDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * 创建表方法
	 * @param db
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO 自动生成的方法存根
		db.execSQL(CREATE_APPLICATION_TABLE);
		db.execSQL(CREATE_COMBO_TABLE);

	}

	/**
	 * 升级数据表方法
	 * @param db
	 * @param oldVersion
	 * @param newVersion
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO 自动生成的方法存根
		db.execSQL(DELETE_APPLICATION_TABLE);
		db.execSQL(DELETE_COMBO_TABLE);
		onCreate(db);

	}

}
