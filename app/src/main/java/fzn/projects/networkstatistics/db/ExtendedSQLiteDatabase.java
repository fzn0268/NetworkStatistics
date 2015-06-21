package fzn.projects.networkstatistics.db;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ExtendedSQLiteDatabase {
	
	public enum SQLiteDataBasicType { NULL, INTEGER, REAL, TEXT, BLOB }

	private ExtendedSQLiteDatabase() {
		// TODO 自动生成的构造函数存根
	}

	public static void createTable(SQLiteOpenHelper dbHelper, String tableName, String[] columns, String[] types) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		String sql = "CREATE TABLE " + tableName + "(";
		for (int i = 0; i < columns.length; i++) {
			sql += columns[i] + " " + types[i];
			if (i != columns.length - 1) sql += ",";
		}
		sql += ")";
		db.execSQL(sql);
	}
}
