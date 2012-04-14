package app.util.rssnotifier.database;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import app.util.rssnotifier.base.RssFeed;
import app.util.rssnotifier.base.RssItem;
import app.util.rssnotifier.base.RssProviderList;

public class DatabaseQuery {	
	public static final String TABLE_ID = "_id";
	
	public static final String TABLE_RSS_ITEM = "RssItem";
	public static final String RSS_ITEM_PROVIDER = "provider";
	public static final String RSS_ITEM_TITLE = "title";
	public static final String RSS_ITEM_TITLE_CLEAN = "title_clean";
	public static final String RSS_ITEM_DESCRIPTION = "description";
	public static final String RSS_ITEM_DESCRIPTION_CLEAN = "description_clean";
	public static final String RSS_ITEM_LINK = "link";
	public static final String RSS_ITEM_PUBDATE = "pubdate";
	
	public static final String QUOTE_WRAPER = "\"";
	
	private static final String[] RSS_ITEM_KEYS = {TABLE_ID, RSS_ITEM_PROVIDER, RSS_ITEM_TITLE, RSS_ITEM_DESCRIPTION, RSS_ITEM_LINK, RSS_ITEM_PUBDATE};
	
	public static final String TABLE_RSS_PROVIDER = "RssProvider";
	public static final String RSS_PROVIDER_NAME = "name";
	public static final String RSS_PROVIDER_LINK = "link";
	
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
	
	private long insertRssItem(RssItem item) throws SQLiteConstraintException {
		if (existRssItem(item.getLink()))
			return -1;
		
		ContentValues value = new ContentValues();
		value.put(RSS_ITEM_PROVIDER, item.getProvider());
		value.put(RSS_ITEM_TITLE, item.getTitle());
		value.put(RSS_ITEM_TITLE_CLEAN, UnicodeToAscii.convertToLatin(item.getTitle()).toLowerCase());
		value.put(RSS_ITEM_DESCRIPTION, item.getDescription());
		value.put(RSS_ITEM_DESCRIPTION_CLEAN, UnicodeToAscii.convertToLatin(item.getDescription()).toLowerCase());
		value.put(RSS_ITEM_LINK, item.getLink());
		value.put(RSS_ITEM_PUBDATE, item.getPubDate());
		return db.insert(TABLE_RSS_ITEM, null, value);
	}
	
	private Cursor getRssItems(String provider, int limit) {
		String criteria = RSS_ITEM_PROVIDER + "=" + textWrap(provider);
		String strLimit = null;
		if (limit > 0)
			strLimit = "0," + String.valueOf(limit);
		return db.query(TABLE_RSS_ITEM, RSS_ITEM_KEYS, criteria, null, null, null, RSS_ITEM_PUBDATE + " DESC", strLimit);
	}

	private Cursor getRssProviders() {
		return db.query(TABLE_RSS_PROVIDER, RSS_PROVIDER_KEYS, null, null, null, null, null);
	}
	
	private Cursor getRssProviders(String name) {
		return db.query(TABLE_RSS_PROVIDER, RSS_PROVIDER_KEYS, RSS_PROVIDER_NAME + "=" + textWrap(name), null, null, null, null);
	}
	
	public ArrayList<RssItem> searchRssItem(String searchWord) {
		searchWord = UnicodeToAscii.convertToLatin(searchWord).toLowerCase();
		String criteria = RSS_ITEM_TITLE_CLEAN + " LIKE \"%" + searchWord + "%\"" +
					" OR " + RSS_ITEM_DESCRIPTION_CLEAN + " LIKE \"%" + searchWord + "%\"";
		Cursor cursor = db.query(TABLE_RSS_ITEM, RSS_ITEM_KEYS, criteria, null, null, null, RSS_ITEM_PUBDATE + " DESC");
		
		ArrayList<RssItem> ret = null;
		if (cursor != null) {
			ret = new ArrayList<RssItem>();
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				ret.add(new RssItem(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5)));
				cursor.moveToNext();
			}
		}
		
		return ret;
	}
	
	public ArrayList<Integer> insertRssFeed(RssFeed feed) {
		ArrayList<Integer> addList = new ArrayList<Integer>();
		for (int i = 0; i < feed.getList().size(); i++)
			if (insertRssItem(feed.getList().get(i)) != -1)
				addList.add(i);
		
		if (addList.size() > 0)
			return addList;
		else
			return null;
	}
	
	public long insertRssProvider(String provider, String link){
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
	
	public RssFeed getRssFeed(String[] providers, int limit) {
		Cursor cursor = null;
		RssFeed feed = new RssFeed();
		
		for (int i = 0; i < providers.length; i++) {
			cursor = getRssItems(providers[i], limit);
			if (cursor != null) {
				cursor.moveToFirst();
				while (!cursor.isAfterLast()) {
					feed.addItem(new RssItem(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5)));
					cursor.moveToNext();
				}
			}
		}
        
		if (feed.size() > 0)
			return feed;
		return null;
	}
	
	public RssFeed getRssFeed(String[] providers) {
		return getRssFeed(providers, 0);
	}
	
	public RssProviderList getRssProviderList() {
		Cursor cursor = getRssProviders();
		RssProviderList providers = null;
        if (cursor != null) {
        	providers = new RssProviderList();
        	cursor.moveToFirst();
        	while (!cursor.isAfterLast()) {
        		providers.addProvider(cursor.getString(1), cursor.getString(2));
        		cursor.moveToNext();
        	}
        }
        
        return providers;
	}
	
	public RssProviderList getRssProviderList(String name) {
		Cursor cursor = getRssProviders(name);
		RssProviderList providers = null;
        if (cursor != null) {
        	providers = new RssProviderList();
        	cursor.moveToFirst();
        	while (!cursor.isAfterLast()) {
        		providers.addProvider(cursor.getString(1), cursor.getString(2));
        		cursor.moveToNext();
        	}
        }
        
        return providers;
	}
}