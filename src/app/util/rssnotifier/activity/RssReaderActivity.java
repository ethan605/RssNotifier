package app.util.rssnotifier.activity;

import java.util.ArrayList;

import android.app.*;
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
	protected static boolean isTaskRunning = false;
	private RssFeed rssFeed = null;
	private RssProviderList rssProvider = null;
	private ArrayList<RssItem> rssList = null;
	private RssItemAdapter rssAdapter = null;
	private int curProvider, maxItemLoad = 10;
	private DatabaseQuery dbQuery;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.rss_reader);
	    this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	    
	    Button btnManage = (Button) findViewById(R.id.btn_manage),
	    		btnBrowse = (Button) findViewById(R.id.btn_browse),
	    		btnRefresh = (Button) findViewById(R.id.btn_refresh),
	    		btnSearch = (Button) findViewById(R.id.btn_search);
	    btnManage.setOnClickListener(this);
	    btnBrowse.setOnClickListener(this);
	    btnRefresh.setOnClickListener(this);
	    btnSearch.setOnClickListener(this);
	    
	    init();
	    loadData(null, maxItemLoad, false, true);
	}
	
	@Override
	public void onDestroy() {
		dbQuery.closeDB();
		super.onDestroy();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		RssItem item = rssList.get(position);
		Intent intent = new Intent(RssReaderActivity.this, RssDetailActivity.class);
		intent.putExtra("rss_title", item.getTitle());
		intent.putExtra("rss_link", item.getLink());
		RssReaderActivity.this.startActivity(intent);
	}
	
	private class RssDownloadTask extends AsyncTask<String, Void, Void> {
		private ProgressDialog progDialog;
		private String name;
		private int limit;
		private boolean dialogShow;
		private ArrayList<Integer> addList;
		
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
			}
			
			isTaskRunning = true;
		}
		
		@Override
		protected Void doInBackground(String... urls) {
			addList = new ArrayList<Integer>();
			rssProvider = dbQuery.getRssProviderList(name); 
			String[] providerNames = rssProvider.getProviderNames();
			
			for (int i = 0; i < providerNames.length; i++) {
				String[] providerLinks = rssProvider.getProviderLinks(providerNames[i]);
				for (int j = 0; j < providerLinks.length; j++)
					addList = fetchRss(providerNames[i], providerLinks[j]);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			if (dialogShow)
				progDialog.cancel();
			if (addList != null) {
				Toast.makeText(RssReaderActivity.this, R.string.rss_items_update_done, Toast.LENGTH_SHORT).show();
				rssFeed = dbQuery.getRssFeed(name, limit);
				updateListView();
			} else
				Toast.makeText(RssReaderActivity.this, R.string.rss_item_not_update, Toast.LENGTH_SHORT).show();
			isTaskRunning = false;
		}
	}
	
	@Override
	public void onClick(View v) {
		Button btnManage = (Button) findViewById(R.id.btn_manage),
	    		btnBrowse = (Button) findViewById(R.id.btn_browse),
	    		btnRefresh = (Button) findViewById(R.id.btn_refresh),
	    		btnSearch = (Button) findViewById(R.id.btn_search);
		EditText txtSearch = (EditText) findViewById(R.id.txt_search);
		txtSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				rssList.clear();
				rssList.addAll(dbQuery.searchRssItem(s.toString()));
				rssAdapter.notifyDataSetChanged();
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
							if (isTaskRunning)
								return;
							if (item == 0) {
								loadData(null, maxItemLoad, false, false);
								return;
							}
							loadData(rssProvider.getProviderNames()[item-1], 0, false, false);
							curProvider = item-1;
							updateListView();
						}
					});
			builder.create().show();
			break;
		
		case R.id.btn_refresh:
			if (isTaskRunning)
				Toast.makeText(RssReaderActivity.this, R.string.rss_keep_updating, Toast.LENGTH_SHORT);
			else if (curProvider == -1)
				loadData(null, maxItemLoad, true, true);
			else
				loadData(dbQuery.getRssProviderList(null).getProviderNames()[curProvider], 0, true, true);
			break;
		
		case R.id.btn_search:
			if (btnSearch.getText().toString().equals(getString(R.string.btn_search_text))) {
				btnManage.setVisibility(View.GONE);
				btnBrowse.setVisibility(View.GONE);
				btnRefresh.setVisibility(View.GONE);
				btnSearch.setText(R.string.btn_done_text);
				txtSearch.setVisibility(View.VISIBLE);
				txtSearch.requestFocus();
			} else {
				btnManage.setVisibility(View.VISIBLE);
				btnBrowse.setVisibility(View.VISIBLE);
				btnRefresh.setVisibility(View.VISIBLE);
				btnSearch.setText(R.string.btn_search_text);
				txtSearch.setVisibility(View.GONE);
				txtSearch.setText("");
				loadData(null, maxItemLoad, false, false);
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
				if (isTaskRunning)
					break;
				String[] addProvider = data.getStringArrayExtra("add-provider");
				dbQuery.insertRssProvider(addProvider[0], addProvider[1]);
				loadData(addProvider[0], 0, true, true);
				curProvider = rssProvider.length()-1;
				break;
			case RES_RSS_DELETE:
				loadData(null, maxItemLoad, false, false);
				break;
			default:
				break;
			}
			break;
		default:
			break;
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
	}
	
	private void loadData(String providerName, int limit, boolean dialogShow, boolean update) {
		rssFeed = dbQuery.getRssFeed(providerName, limit);
		
		if (providerName == null)
			curProvider = -1;
		updateListView();
		
		if (update)
			new RssDownloadTask(providerName, limit, dialogShow).execute();
	}
	
	private ArrayList<Integer> fetchRss(String _provider, String _url) {
		RssFeed _feed = new XmlPullHandler(_provider, _url).getFeed();
		ArrayList<Integer> addList = new ArrayList<Integer>();
		if (_feed != null)
	    	addList = dbQuery.insertRssFeed(_feed);
		
		return addList;
	}
	
	private void updateListView() {
		rssList.clear();
		if (rssFeed != null)
			rssList.addAll(rssFeed.getList());
		rssAdapter.notifyDataSetChanged();
		this.getListView().setSelection(0);
	}
}