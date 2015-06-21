package fzn.projects.networkstatistics.db;

import android.provider.BaseColumns;

/**
 * 数据库契约类
 * 记录数据库使用的表和表内各列的列名及属性
 */
public final class NetworkStatisticsContract {

	public NetworkStatisticsContract() {}
	
	public static abstract class AppEntry implements BaseColumns {
		public static final String TYPE_INT = "INTEGER";
		public static final String TABLE_NAME = "Application";
		public static final String COLUMN_UID = "UID";
		public static final String COLUMN_UID_TYPE = "INTEGER NOT NULL";
		public static final String COLUMN_PACKAGE_NAME = "PackageName";
		public static final String COLUMN_PACKAGE_NAME_TYPE = "TEXT NOT NULL";
		public static final String COLUMN_WL_TRANSMITTED = "WlTransmitted";
		public static final String COLUMN_WL_RECEIVED = "WlReceived";
		public static final String COLUMN_MO_TRANSMITTED = "MoTransmitted";
		public static final String COLUMN_MO_RECEIVED = "MoReceived";
		public static final String COLUMN_EXIST = "Exist";
		public static final String COLUMN_EXIST_TYPE = "SMALLINT NOT NULL";
	}
	
	public static abstract class ComboEntry implements BaseColumns {
		public static final String TABLE_NAME = "Combo";
		public static final String COLUMN_TIMESTAMP = "TimeStamp";
		public static final String COLUMN_TIMESTAMP_TYPE = "TEXT NOT NULL";
		public static final String COLUMN_COMBO_NAME = "ComboName";
		public static final String COLUMN_COMBO_NAME_TYPE = "TEXT";
		public static final String COLUMN_COMBO_CONN = "Conn";
		public static final String COLUMN_COMBO_CONN_TYPE = "SMALLINT NOT NULL";
		public static final String COLUMN_QUANTUM = "Quantum";
		public static final String COLUMN_QUANTUM_TYPE = "INTEGER NOT NULL";
		public static final String COLUMN_PERIOD = "Period";
		public static final String COLUMN_PERIOD_TYPE = "TEXT NOT NULL";
		public static final String COLUMN_PERIOD_REMAIN = "PeriodRemain";
		public static final String COLUMN_TIME_RANGE_FROM = "TimeRangeFrom";
		public static final String COLUMN_TIME_RANGE_FROM_TYPE = "TEXT";
		public static final String COLUMN_TIME_RANGE_TO = "TimeRangeTo";
		public static final String COLUMN_TIME_RANGE_TO_TYPE = "TEXT";
		public static final String COLUMN_USED = "Used";
		public static final String COLUMN_USED_TYPE = "INTEGER";
		public static final String COLUMN_REMAIN_DERIVED = "(" + ComboEntry.COLUMN_QUANTUM + " - " + ComboEntry.COLUMN_USED + ")" + " AS Remain";
		public static final String COLUMN_REMAIN = "Remain";
	}
	
	public static abstract class ComboHistory implements BaseColumns {
		public static final String TABLE_NAME = "ComboHistory";
		public static final String COLUMN_TIMESTAMP = "TimeStamp";
		public static final String COLUMN_TIMESTAMP_TYPE = "TEXT NOT NULL";
		public static final String COLUMN_COMBO_NAME = "ComboName";
		public static final String COLUMN_COMBO_NAME_TYPE = "TEXT";
		public static final String COLUMN_QUANTUM = "Quantum";
		public static final String COLUMN_QUANTUM_TYPE = "INTEGER NOT NULL";
		public static final String COLUMN_USED = "Used";
		public static final String COLUMN_USED_TYPE = "INTEGER";
		public static final String COLUMN_MONTH = "Month";
		public static final String COLUMN_MONTH_TYPE = "DATE";
	}
	
	public static abstract class ComboHistoryEntry implements BaseColumns {
		
	}
}
