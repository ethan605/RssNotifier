package app.util.rssnotifier;

import java.util.ArrayList;

import android.app.*;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.*;
import android.os.*;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.view.View.OnKeyListener;
import android.widget.*;
import app.util.rssnotifier.R;
import app.util.rssnotifier.base.*;
import app.util.rssnotifier.database.DatabaseQuery;

public class RssReaderActivity extends ListActivity implements View.OnClickListener {
	final String TAG = "RssReaderActivity";
	protected static final int REQ_RSS_MANAGE = RESULT_FIRST_USER + 1;
	protected static final int RES_RSS_ADD = RESULT_FIRST_USER + 2;
	protected static final int RES_RSS_DELETE = RESULT_FIRST_USER + 3;
	protected static final int REQ_STOP_SERVICE = RESULT_FIRST_USER + 4;
	
	private static boolean isTaskRunning = false;
	private RssFeed rssFeed = null;
	private RssProviderList rssProvider = null;
	private ArrayList<RssItem> rssList = null;
	private RssItemAdapter rssAdapter = null;
	private int curProvider;
	private DatabaseQuery dbQuery;
	
	private Button btnManage, btnBrowse, btnRefresh, btnSearch;
	private EditText txtSearch;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rss_reader);
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		btnManage = (Button) findViewById(R.id.btn_manage);
		btnBrowse = (Button) findViewById(R.id.btn_browse);
		btnRefresh = (Button) findViewById(R.id.btn_refresh);
		btnSearch = (Button) findViewById(R.id.btn_search);
		
		btnManage.setOnClickListener(this);
		btnBrowse.setOnClickListener(this);
		btnRefresh.setOnClickListener(this);
		btnSearch.setOnClickListener(this);
		
		txtSearch = (EditText) findViewById(R.id.txt_search);
		
		init();

		boolean hasUpdate = false;
		try {
			hasUpdate = getIntent().getExtras().getBoolean("rss_update");
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		
		if (hasUpdate) {
			rssFeed = dbQuery.getUpdatedRssFeed();
			updateListView();
		} else
			loadData(null, true, false, true);
	}
	
	@Override
	public void onDestroy() {
		dbQuery.closeDB();
		super.onDestroy();
	}
	
	@Override
	public void onNewIntent(Intent intent) {
		try {
			if (intent.getExtras().getBoolean("rss_update")) {
				rssFeed = dbQuery.getUpdatedRssFeed();
				updateListView();
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		RssItem item = rssList.get(position);
		Intent intent = new Intent(RssReaderActivity.this, RssDetailActivity.class);
		intent.putExtra("rss_title", item.getTitle());
		intent.putExtra("rss_link", item.getLink());
		RssReaderActivity.this.startActivity(intent);
	}
	
	@Override
	public void onClick(View v) {
		TextView txtManageLabel, txtBrowseLabel, txtRefreshLabel, txtSearchLabel;
		txtManageLabel = (TextView) findViewById(R.id.txt_manage_label);
		txtBrowseLabel = (TextView) findViewById(R.id.txt_browse_label);
		txtRefreshLabel = (TextView) findViewById(R.id.txt_refresh_label);
		txtSearchLabel = (TextView) findViewById(R.id.txt_search_label);
		txtSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				new AsyncTask<String, Void, ArrayList<RssItem>>() {
					@Override
					protected ArrayList<RssItem> doInBackground(String... params) {
						return dbQuery.searchRssItem(params[0]);
					}
					
					@Override
					protected void onPostExecute(ArrayList<RssItem> result) {
						rssList.clear();
						rssList.addAll(result);
						rssAdapter.config().notifyDataSetChanged();						
					}
				}.execute(s.toString());
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		txtSearch.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER)
					return true;
				return false;
			}
		});
		switch (v.getId()) {
		case R.id.btn_manage:
			startActivityForResult(new Intent(RssReaderActivity.this, RssManageActivity.class), REQ_RSS_MANAGE);
			break;
		
		case R.id.btn_browse:
			rssProvider = dbQuery.getRssProviderList(null);
			int length = rssProvider.getProviderNames().length;
			if (length == 0)
				break;
			
			String[] providers = new String[length+1];
			providers[0] = "All";				
			for (int i = 0; i < length; i++)
				providers[i+1] = rssProvider.getProviderNames()[i];
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.rss_choose_provider)
					.setItems(providers, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int item) {
							if (item == 0) {
								loadData(null, true, false, false);
								return;
							}
							loadData(rssProvider.getProviderNames()[item-1], false, false, false);
							curProvider = item-1;
							updateListView();
						}
					});
			builder.create().show();
			break;
		
		case R.id.btn_refresh:
			if (curProvider == -1)
				loadData(null, true, true, true);
			else
				loadData(dbQuery.getRssProviderList(null).getProviderNames()[curProvider], false, true, true);
			break;
		
		case R.id.btn_search:
			if (txtSearchLabel.getText().toString().equals(getString(R.string.btn_search_text))) {
				btnManage.setVisibility(View.GONE);
				txtManageLabel.setVisibility(View.GONE);
				btnBrowse.setVisibility(View.GONE);
				txtBrowseLabel.setVisibility(View.GONE);
				btnRefresh.setVisibility(View.GONE);
				txtRefreshLabel.setVisibility(View.GONE);
				btnSearch.setBackgroundResource(R.drawable.btn_done_background);
				txtSearchLabel.setText(R.string.btn_done_text);
				txtSearch.setVisibility(View.VISIBLE);
				txtSearch.requestFocus();
			} else {
				btnManage.setVisibility(View.VISIBLE);
				txtManageLabel.setVisibility(View.VISIBLE);
				btnBrowse.setVisibility(View.VISIBLE);
				txtBrowseLabel.setVisibility(View.VISIBLE);
				btnRefresh.setVisibility(View.VISIBLE);
				txtRefreshLabel.setVisibility(View.VISIBLE);
				btnSearch.setBackgroundResource(R.drawable.btn_search_background);
				txtSearchLabel.setText(R.string.btn_search_text);
				txtSearch.setVisibility(View.GONE);
				txtSearch.setText("");
				loadData(null, true, false, false);
			}
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQ_RSS_MANAGE:
			switch (resultCode) {
			case RES_RSS_ADD:
				String[] addProvider = data.getStringArrayExtra("add-provider");
				loadData(addProvider[0], false, true, true);
				curProvider = dbQuery.getRssProviderList(null).length()-1;
				break;
			case RES_RSS_DELETE:
				loadData(null, true, false, false);
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater myMenuInflater = getMenuInflater();
		myMenuInflater.inflate(R.menu.menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		
		switch(item.getItemId()) {
		case R.id.mnu_exit:
			if (!isTaskRunning)
				finish();
			else
				Toast.makeText(RssReaderActivity.this, R.string.rss_keep_updating, Toast.LENGTH_SHORT).show();
			break;
		case R.id.mnu_about:
			break;
		default:
			break;
		}
		return true;
	}
	
	private class RssDownloadTask extends AsyncTask<String, Void, Boolean> {
		private ProgressDialog progDialog;
		private String name;
		private int limit;
		private boolean dialogShow;
		
		public RssDownloadTask(String _name, int _limit, boolean _dialogShow) {
			name = _name;
			limit = _limit;
			dialogShow = _dialogShow;
		}
		
		@Override
		protected void onPreExecute() {
			if (dialogShow) {
				progDialog = new ProgressDialog(RssReaderActivity.this);
				progDialog.setCancelable(false);
				progDialog.setMessage(getString(R.string.rss_fetching));
				progDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.btn_hide_text), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Toast.makeText(RssReaderActivity.this, R.string.rss_keep_updating, Toast.LENGTH_SHORT).show();
					}
				});
				progDialog.show();
			} else
				Toast.makeText(RssReaderActivity.this, R.string.rss_start_updating, Toast.LENGTH_SHORT).show();
			
			isTaskRunning = true;
		}
		
		@Override
		protected Boolean doInBackground(String... urls) {
			rssProvider = dbQuery.getRssProviderList(name); 
			String[] providerNames = rssProvider.getProviderNames();
			
			boolean hasUpdate = false;
			for (int i = 0; i < providerNames.length; i++) {
				String[] providerLinks = rssProvider.getProviderLinks(providerNames[i]);
				for (int j = 0; j < providerLinks.length; j++)
					if (fetchRss(providerNames[i], providerLinks[j]))
						hasUpdate = true;
			}
			return hasUpdate;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (dialogShow)
				progDialog.cancel();
			if (result) {
				Toast.makeText(RssReaderActivity.this, R.string.rss_items_update_done, Toast.LENGTH_SHORT).show();
				rssFeed = dbQuery.getRssFeed(name, limit);
				updateListView();
			} else
				Toast.makeText(RssReaderActivity.this, R.string.rss_item_not_update, Toast.LENGTH_SHORT).show();
			
			dbQuery.updateRssItems();
			isTaskRunning = false;
		}
	}
	
	private void init() {
		dbQuery = new DatabaseQuery(this);
		dbQuery.openDB();
		rssFeed = new RssFeed();
		rssProvider = dbQuery.getRssProviderList(null);
		rssList = new ArrayList<RssItem>();
		rssAdapter = new RssItemAdapter(this, R.layout.rss_item_list, rssList);
		setListAdapter(rssAdapter);
		
		if (!isServiceRunning(RssNotificationService.class.getCanonicalName()))
			startService(new Intent(RssReaderActivity.this, RssNotificationService.class));
	}
	
	private void loadData(String providerName, boolean limit, boolean dialogShow, boolean update) {
		int maxItemLoad = limit ? dbQuery.getRssSettings()[1] : 0;
		rssFeed = dbQuery.getRssFeed(providerName, maxItemLoad);
		
		if (providerName == null)
			curProvider = -1;
		updateListView();
		
		if (isTaskRunning)
			Toast.makeText(RssReaderActivity.this, R.string.rss_keep_updating, Toast.LENGTH_SHORT).show();
		else if (update)
			new RssDownloadTask(providerName, maxItemLoad, dialogShow).execute();
	}
	
	private boolean fetchRss(String _provider, String _url) {
		RssFeed _feed = new RssContentHandler(_provider, _url).getFeed();
		if (_feed != null)
	    	return dbQuery.insertRssFeed(_feed);
		
		return false;
	}
	
	private boolean isServiceRunning(String serviceName) {
	    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
	        if (service.service.getClassName().equals(serviceName))
	            return true;
	    return false;
	}
	
	private void updateListView() {
		rssList.clear();
		if (rssFeed != null)
			rssList.addAll(rssFeed.getList());
		rssAdapter.config().notifyDataSetChanged();
		dbQuery.updateRssItems();
		this.getListView().setSelection(0);
	}
}