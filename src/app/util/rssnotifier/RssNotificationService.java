package app.util.rssnotifier;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import app.util.rssnotifier.base.RssFeed;
import app.util.rssnotifier.base.RssProviderList;
import app.util.rssnotifier.base.RssContentHandler;
import app.util.rssnotifier.database.DatabaseQuery;

public class RssNotificationService extends Service implements Runnable {
	protected final static String ACTION = "RssNotificationServiceAction";
	protected static final int RSS_NOTIFICATION_ID = 1;
	protected static final int ONE_MINUTE = 1000*60;
	final String TAG = "RssNotificationService";
	
	private NotifyServiceReceiver notifyServiceReceiver;
	private NotificationManager notificationManager;
	private Handler handler;
	private Context mContext;
	private String mContent;
	private PendingIntent mPendingIntent;
	
	private DatabaseQuery dbQuery;
	
	@Override
	public void onCreate() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ACTION);
		notifyServiceReceiver = new NotifyServiceReceiver();
		registerReceiver(notifyServiceReceiver, intentFilter);		
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mContext = getApplicationContext();
		
		handler = new Handler();
		
		dbQuery = new DatabaseQuery(this);
		dbQuery.openDB();
		
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handler.post(RssNotificationService.this);
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		handler.removeCallbacks(this);
		unregisterReceiver(notifyServiceReceiver);
		dbQuery.closeDB();
		super.onDestroy();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void run() {
		new AsyncTask<Void, Void, ArrayList<String>>() {
			@Override
			protected ArrayList<String> doInBackground(Void... params) {
				return updateRss();
			}
			
			protected void onPostExecute(ArrayList<String> result) {
				if (result.size() > 0) {
					mContent = "New RSS updated for: ";
					for (int i = 0; i < result.size(); i++)
						mContent += result.get(i) +  " ";
					updateNotification();
				}

				handler.postDelayed(RssNotificationService.this, ONE_MINUTE*dbQuery.getRssSettings()[0]);
			}
		}.execute();
	}
	
	private class NotifyServiceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			int rqs = intent.getIntExtra("REQ", 0);
			if (rqs == RssReaderActivity.REQ_STOP_SERVICE)
				stopSelf();
		}
	}
	
	private ArrayList<String> updateRss() {
		ArrayList<String> updatedProvider = new ArrayList<String>();
		RssProviderList rssProvider = dbQuery.getRssProviderList(null); 
		String[] providerNames = rssProvider.getProviderNames();
		
		for (int i = 0; i < providerNames.length; i++) {
			String[] providerLinks = rssProvider.getProviderLinks(providerNames[i]);
			for (int j = 0; j < providerLinks.length; j++)
				if (fetchRss(providerNames[i], providerLinks[j]))
					updatedProvider.add(providerNames[i]);
		}
		
		return updatedProvider;
	}
	
	private boolean fetchRss(String _provider, String _url) {
		RssFeed _feed = new RssContentHandler(_provider, _url).getFeed();
		if (_feed != null)
	    	return dbQuery.insertRssFeed(_feed);
		
		return false;
	}
	
	private void updateNotification() {
		Intent intent = new Intent(RssNotificationService.this, RssReaderActivity.class);
		intent.putExtra("rss_update", true);
		PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
				0, intent, Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		Notification notification = new Notification(R.drawable.ic_launcher,
			getString(R.string.rss_update_notification),
			System.currentTimeMillis());
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.setLatestEventInfo(mContext, getString(R.string.rss_update_notification), mContent, pendingIntent);
		notificationManager.notify(RSS_NOTIFICATION_ID, notification);
	}
}