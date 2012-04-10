package app.util.rssnotifier.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	public static final String DATABASE_NAME = "RssNotifier";
	public static final String DATABASE_PATH = "/data/data/app.util.test/databases/";
	public static final int DATABASE_VERSION = 1;

	public static final String TABLE_RSS_ITEM_CREATE = 
		"create table " + DatabaseQuery.TABLE_RSS_ITEM + " (" +
		DatabaseQuery.TABLE_ID + " integer primary key autoincrement, " +
		DatabaseQuery.RSS_ITEM_PROVIDER + " text not null, " +
		DatabaseQuery.RSS_ITEM_TITLE + " text unique not null, " +
		DatabaseQuery.RSS_ITEM_DESCRIPTION + " text, " +
		DatabaseQuery.RSS_ITEM_LINK + " text, " +
		DatabaseQuery.RSS_ITEM_PUBDATE + " text not null);";
	
	public static final String TABLE_RSS_PROVIDER_CREATE = 
		"create table " + DatabaseQuery.TABLE_RSS_PROVIDER + " (" +
		DatabaseQuery.TABLE_ID + " integer primary key autoincrement, " +
		DatabaseQuery.RSS_PROVIDER_NAME + " text unique not null, " + 
		DatabaseQuery.RSS_PROVIDER_LINK + " text unique not null);";
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_RSS_ITEM_CREATE);
		db.execSQL(TABLE_RSS_PROVIDER_CREATE);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}
