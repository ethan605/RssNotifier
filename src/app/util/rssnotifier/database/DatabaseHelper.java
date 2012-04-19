package app.util.rssnotifier.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import app.util.rssnotifier.RssReaderActivity;

public class DatabaseHelper extends SQLiteOpenHelper {
	public static final String DATABASE_NAME = "RssNotifier";
	public static final String DATABASE_PATH = "/data/data/app.util.rssnotifier/databases/";
	public static final int DATABASE_VERSION = 1;
	
	public static final String TABLE_RSS_SETTING_CREATE =
		"create table " + DatabaseQuery.TABLE_RSS_SETTING + " (" +
				DatabaseQuery.TABLE_ID + " integer primary key autoincrement, " +
				DatabaseQuery.RSS_SETTING_TIME_INTERVAL + " integer not null, " + 
				DatabaseQuery.RSS_SETTING_MAX_ITEM_LOAD + " integer not null, " +
				DatabaseQuery.RSS_SETTING_TRIMMED_TEXT_SIZE + " interger not null);";

	public static final String TABLE_RSS_ITEM_CREATE = 
		"create table " + DatabaseQuery.TABLE_RSS_ITEM + " (" +
		DatabaseQuery.TABLE_ID + " integer primary key autoincrement, " +
		DatabaseQuery.RSS_ITEM_PROVIDER + " text not null, " +
		DatabaseQuery.RSS_ITEM_TITLE + " text unique not null, " +
		DatabaseQuery.RSS_ITEM_TITLE_CLEAN + " text not null, " +
		DatabaseQuery.RSS_ITEM_DESCRIPTION + " text, " +
		DatabaseQuery.RSS_ITEM_DESCRIPTION_CLEAN + " text, " +
		DatabaseQuery.RSS_ITEM_LINK + " text, " +
		DatabaseQuery.RSS_ITEM_PUBDATE + " text not null, " +
		DatabaseQuery.RSS_ITEM_UPDATED + " integer not null);";
	
	public static final String TABLE_RSS_PROVIDER_CREATE = 
		"create table " + DatabaseQuery.TABLE_RSS_PROVIDER + " (" +
		DatabaseQuery.TABLE_ID + " integer primary key autoincrement, " +
		DatabaseQuery.RSS_PROVIDER_NAME + " text not null, " + 
		DatabaseQuery.RSS_PROVIDER_LINK + " text unique not null, " +
		DatabaseQuery.RSS_PROVIDER_ICON + " blob);";
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_RSS_SETTING_CREATE);
		db.execSQL(TABLE_RSS_ITEM_CREATE);
		db.execSQL(TABLE_RSS_PROVIDER_CREATE);
		
		ContentValues value = new ContentValues();
		value.put(DatabaseQuery.RSS_SETTING_TIME_INTERVAL, DatabaseQuery.DEF_TIME_INTERVAL);
		value.put(DatabaseQuery.RSS_SETTING_MAX_ITEM_LOAD, DatabaseQuery.DEF_MAX_ITEM_LOAD);
		value.put(DatabaseQuery.RSS_SETTING_TRIMMED_TEXT_SIZE, DatabaseQuery.DEF_TRIMMED_TEXT_SIZE);
		db.insert(DatabaseQuery.TABLE_RSS_SETTING, null, value);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}
