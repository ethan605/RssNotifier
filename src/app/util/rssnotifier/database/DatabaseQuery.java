package app.util.rssnotifier.database;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import app.util.rssnotifier.base.RssFeed;
import app.util.rssnotifier.base.RssItem;
import app.util.rssnotifier.base.RssProviderList;

public class DatabaseQuery {	
	final String TAG = "DatabaseQuery";

	public static final String TABLE_ID = "_id";
	public static final String QUOTE_WRAPER = "\"";
	
	public static final String TABLE_RSS_SETTING = "RssSeting";
	public static final String RSS_SETTING_TIME_INTERVAL = "time_interval";
	public static final String RSS_SETTING_MAX_ITEM_LOAD = "max_item_load";
	public static final int DEF_TIME_INTERVAL = 10;
	public static final int DEF_MAX_ITEM_LOAD = 20;
	
	public static final String TABLE_RSS_ITEM = "RssItem";
	public static final String RSS_ITEM_PROVIDER = "provider";
	public static final String RSS_ITEM_TITLE = "title";
	public static final String RSS_ITEM_TITLE_CLEAN = "title_clean";
	public static final String RSS_ITEM_DESCRIPTION = "description";
	public static final String RSS_ITEM_DESCRIPTION_CLEAN = "description_clean";
	public static final String RSS_ITEM_LINK = "link";
	public static final String RSS_ITEM_PUBDATE = "pubdate";
	public static final String RSS_ITEM_UPDATED = "updated";
	
	public static final String TABLE_RSS_PROVIDER = "RssProvider";
	public static final String RSS_PROVIDER_NAME = "name";
	public static final String RSS_PROVIDER_LINK = "link";
	
	private static final String[] RSS_SETTING_KEYS = {TABLE_ID, RSS_SETTING_TIME_INTERVAL, RSS_SETTING_MAX_ITEM_LOAD};
	private static final String[] RSS_ITEM_KEYS = {TABLE_ID, RSS_ITEM_PROVIDER, RSS_ITEM_TITLE, RSS_ITEM_DESCRIPTION, RSS_ITEM_LINK, RSS_ITEM_PUBDATE, RSS_ITEM_UPDATED};	
	private static final String[] RSS_PROVIDER_KEYS = {TABLE_ID, RSS_PROVIDER_NAME, RSS_PROVIDER_LINK};
	
	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	private Context mContext;
	
	public DatabaseQuery(Context c) {
		mContext = c;
	}
	
	public DatabaseQuery openDB() throws android.database.SQLException {
		dbHelper = new DatabaseHelper(mContext);
		db = dbHelper.getWritableDatabase();
		return this;
	}
	
	public void closeDB(){
		dbHelper.close();
	}
	
	private String textWrap(String text) {
		return QUOTE_WRAPER + text + QUOTE_WRAPER;
	}
	
	private boolean existRssItem(String rssLink) {
		Cursor cursor = db.query(TABLE_RSS_ITEM, new String[] {TABLE_ID, RSS_ITEM_TITLE}, RSS_ITEM_LINK + "=" + textWrap(rssLink), null, null, null, null);
		return (cursor.getCount() > 0);
	}
	
	private boolean existRssProvider(String providerLink) {
		Cursor cursor = db.query(TABLE_RSS_PROVIDER, new String[] {TABLE_ID, RSS_PROVIDER_NAME}, RSS_PROVIDER_NAME + "=" + textWrap(providerLink), null, null, null, null);
		return (cursor.getCount() > 0);
	}
	
	private long insertRssItem(RssItem item) {
		if (existRssItem(item.getLink()))
			return -1;
		
		ContentValues value = new ContentValues();
		value.put(RSS_ITEM_PROVIDER, item.getProvider());
		value.put(RSS_ITEM_TITLE, item.getTitle());
		value.put(RSS_ITEM_TITLE_CLEAN, UnicodeToAscii.convert(item.getTitle()).toLowerCase());
		value.put(RSS_ITEM_DESCRIPTION, item.getDescription());
		value.put(RSS_ITEM_DESCRIPTION_CLEAN, UnicodeToAscii.convert(item.getDescription()).toLowerCase());
		value.put(RSS_ITEM_LINK, item.getLink());
		value.put(RSS_ITEM_PUBDATE, item.getPubDate());
		value.put(RSS_ITEM_UPDATED, item.getUpdated());
		return db.insert(TABLE_RSS_ITEM, null, value);
	}
	
	private Cursor getRssItems(String provider, int limit) {
		String criteria = null;
		String strLimit = null;
		
		if (provider != null)
			criteria = RSS_ITEM_PROVIDER + "=" + textWrap(provider);
		if (limit > 0)
			strLimit = "0," + String.valueOf(limit);
		return db.query(TABLE_RSS_ITEM, RSS_ITEM_KEYS, criteria, null, null, null, RSS_ITEM_PUBDATE + " DESC", strLimit);
	}
	
	private Cursor getUpdatedRssItems() {
		return db.query(TABLE_RSS_ITEM, RSS_ITEM_KEYS, RSS_ITEM_UPDATED + "=" + textWrap("1"), null, null, null, RSS_ITEM_PUBDATE + " DESC");
	}
	
	private Cursor getRssProviders(String name) {
		String criteria = null;
		
		if (name != null)
			criteria = RSS_PROVIDER_NAME + "=" + textWrap(name);
		return db.query(TABLE_RSS_PROVIDER, RSS_PROVIDER_KEYS, criteria, null, null, null, null);
	}
	
	public void updateRssSettings(int[] settings) {
		ContentValues value = new ContentValues();
		if (settings[0] != 0)
			value.put(RSS_SETTING_TIME_INTERVAL, settings[0]);
		if (settings[1] != 0)
			value.put(RSS_SETTING_MAX_ITEM_LOAD, settings[1]);
		db.update(TABLE_RSS_SETTING, value, TABLE_ID + "=1", null);
	}
	
	public int[] getRssSettings() {
		int[] ret = null;
		Cursor cursor = db.query(TABLE_RSS_SETTING, RSS_SETTING_KEYS, null, null, null, null, null);
		cursor.moveToFirst();
		if (!cursor.isAfterLast())
			ret = new int[] {cursor.getInt(1), cursor.getInt(2)};
		return ret;
	}
	
	public ArrayList<RssItem> searchRssItem(String searchWord) {
		searchWord = UnicodeToAscii.convert(searchWord).toLowerCase();
		String criteria = RSS_ITEM_TITLE_CLEAN + " LIKE \"%" + searchWord + "%\"" +
					" OR " + RSS_ITEM_DESCRIPTION_CLEAN + " LIKE \"%" + searchWord + "%\"";
		Cursor cursor = db.query(TABLE_RSS_ITEM, RSS_ITEM_KEYS, criteria, null, null, null, RSS_ITEM_PUBDATE + " DESC");
		
		ArrayList<RssItem> ret = new ArrayList<RssItem>();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			ret.add(new RssItem(cursor.getString(1), cursor.getString(2), cursor.getString(3),
					cursor.getString(4), cursor.getString(5), cursor.getInt(6)));
			cursor.moveToNext();
		}
		
		return ret;
	}
	
	public boolean insertRssFeed(RssFeed feed) {
		boolean hasNewItem = false;
		for (int i = 0; i < feed.getList().size(); i++)
			if (insertRssItem(feed.getList().get(i)) != -1)
				hasNewItem = true;
		
		return hasNewItem;
	}
	
	public long insertRssProvider(String provider, String link) {
		if (existRssProvider(link))
			return -1;
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(RSS_PROVIDER_NAME, provider);
		contentValues.put(RSS_PROVIDER_LINK, link);
		return db.insert(TABLE_RSS_PROVIDER, null, contentValues);
	}
	
	public void deleteRssProvider(String name) {
		db.delete(TABLE_RSS_ITEM, RSS_ITEM_PROVIDER + "=" + textWrap(name), null);
		db.delete(TABLE_RSS_PROVIDER, RSS_PROVIDER_NAME + "=" + textWrap(name), null);
	}
	
	public void updateRssProviderName(String oldName, String newName) {
		ContentValues value = new ContentValues();
		value.put(RSS_ITEM_PROVIDER, newName);
		db.update(TABLE_RSS_PROVIDER, value, RSS_PROVIDER_NAME + "=" + textWrap(oldName), null);
	}
	
	public void updateRssProviderLink(String oldLink, String newLink) {
		ContentValues value = new ContentValues();
		value.put(RSS_ITEM_PROVIDER, newLink);
		db.update(TABLE_RSS_PROVIDER, value, RSS_PROVIDER_LINK + "=" + textWrap(oldLink), null);
	}
	
	public RssFeed getRssFeed(String provider, int limit) {
		Cursor cursor = getRssItems(provider, limit);
		RssFeed feed = new RssFeed();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			feed.addItem(new RssItem(cursor.getString(1), cursor.getString(2), cursor.getString(3),
					cursor.getString(4), cursor.getString(5), cursor.getInt(6)));
			cursor.moveToNext();
		}
		
		return feed;
	}
	
	public RssFeed getUpdatedRssFeed() {
		Cursor cursor = getUpdatedRssItems();
		RssFeed feed = new RssFeed();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			feed.addItem(new RssItem(cursor.getString(1), cursor.getString(2), cursor.getString(3),
						cursor.getString(4), cursor.getString(5), cursor.getInt(6)));
			cursor.moveToNext();
		}
		
		return feed;
	}
	
	public void updateRssItems() {
		ContentValues value = new ContentValues();
		value.put(RSS_ITEM_UPDATED, "0");
		db.update(TABLE_RSS_ITEM, value, RSS_ITEM_UPDATED + "=" + textWrap("1"), null);
	}
	
	public RssProviderList getRssProviderList(String name) {
		Cursor cursor = getRssProviders(name);
		RssProviderList providers = new RssProviderList();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			providers.addProvider(cursor.getString(1), cursor.getString(2));
			cursor.moveToNext();
		}
        return providers;
	}
}