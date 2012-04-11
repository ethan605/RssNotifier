package app.util.rssnotifier.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import app.util.rssnotifier.base.RssFeed;
import app.util.rssnotifier.base.RssItem;
import app.util.rssnotifier.base.RssProviderList;

public class DatabaseQuery {	
	public static final String TABLE_ID = "_id";
	
	public static final String TABLE_RSS_ITEM = "RssItem";
	public static final String RSS_ITEM_PROVIDER = "provider";
	public static final String RSS_ITEM_TITLE = "title";
	public static final String RSS_ITEM_DESCRIPTION = "description";
	public static final String RSS_ITEM_LINK = "link";
	public static final String RSS_ITEM_PUBDATE = "pubdate";
	
	public static final String QOUTE_WRAPER = "\'";
	
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
	
	private boolean existRssItem(String rssLink) {
		Cursor cursor = db.query(TABLE_RSS_ITEM, new String[] {TABLE_ID, RSS_ITEM_TITLE}, RSS_ITEM_LINK + "=" + QOUTE_WRAPER + rssLink + QOUTE_WRAPER, null, null, null, null);
		return (cursor.getCount() > 0);
	}
	
	private boolean existRssProvider(String providerLink) {
		Cursor cursor = db.query(TABLE_RSS_PROVIDER, new String[] {TABLE_ID, RSS_PROVIDER_NAME}, RSS_PROVIDER_NAME + "=" + QOUTE_WRAPER + providerLink + QOUTE_WRAPER, null, null, null, null);
		return (cursor.getCount() > 0);
	}
	

	public long insertRssItem(RssItem item) {
		if (existRssItem(item.getLink()))
			return -1;
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(RSS_ITEM_PROVIDER, item.getProvider());
		contentValues.put(RSS_ITEM_TITLE, item.getTitle());
		contentValues.put(RSS_ITEM_DESCRIPTION, item.getDescription());
		contentValues.put(RSS_ITEM_LINK, item.getLink());
		contentValues.put(RSS_ITEM_PUBDATE, item.getPubDate());
		return db.insert(TABLE_RSS_ITEM, null, contentValues);
	}
	
	public long insertRssProvider(String provider, String link){
		if (existRssProvider(link))
			return -1;
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(RSS_PROVIDER_NAME, provider);
		contentValues.put(RSS_PROVIDER_LINK, link);
		return db.insert(TABLE_RSS_PROVIDER, null, contentValues);
	}

	private Cursor getRssItems() {
		return db.query(TABLE_RSS_ITEM, RSS_ITEM_KEYS, null, null, null, null, RSS_ITEM_PUBDATE + " DESC");
	}
	
	private Cursor getRssItems(String[] providers) {
		String criteria = RSS_ITEM_PROVIDER + "=\'" + providers[0] + QOUTE_WRAPER;
		for (int i = 1; i < providers.length; i++)
			criteria += " OR " + RSS_ITEM_PROVIDER + "=" + QOUTE_WRAPER + providers[i] + QOUTE_WRAPER;
		return db.query(TABLE_RSS_ITEM, RSS_ITEM_KEYS, criteria, null, null, null, null);
	}

	private Cursor getRssProviders() {
		return db.query(TABLE_RSS_PROVIDER, RSS_PROVIDER_KEYS, null, null, null, null, null);
	}
	
	public RssFeed getRssFeed() {
		Cursor cursor = getRssItems();
		RssFeed feed = null;
        if (cursor != null) {
        	feed = new RssFeed();
        	cursor.moveToFirst();
        	while (!cursor.isAfterLast()) {
        		feed.addItem(new RssItem(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5)));
        		cursor.moveToNext();
        	}
        }
        
        return feed;
	}
	
	public RssFeed getRssFeed(String[] providers) {
		Cursor cursor = getRssItems(providers);
		RssFeed feed = null;
        if (cursor != null) {
        	feed = new RssFeed();
        	cursor.moveToFirst();
        	while (!cursor.isAfterLast()) {
        		feed.addItem(new RssItem(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5)));
        		cursor.moveToNext();
        	}
        }
        
        return feed;
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
}