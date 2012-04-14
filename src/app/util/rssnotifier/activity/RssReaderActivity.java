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
        
        dbQuery = new DatabaseQuery(this);
        dbQuery.openDB();
        rssFeed = new RssFeed();
        rssProvider = new RssProviderList();
        rssList = new ArrayList<RssItem>();
        rssAdapter = new RssItemAdapter(this, R.layout.rss_item_list, rssList);
        setListAdapter(rssAdapter);
        loadData(true);        
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
    
    private void loadData(boolean update) {
        rssProvider = dbQuery.getRssProviderList();
        rssFeed = dbQuery.getRssFeed(rssProvider.getProviderNames(), maxItemLoad);
        
        String[] providerNames = rssProvider.getProviderNames();
        curProvider = -1;
        if (rssFeed != null) {
        	rssList.clear();
        	rssList.addAll(rssFeed.getList());
        }
        rssAdapter.notifyDataSetChanged();
        
        if (update)
	        for (int i = 0; i < rssProvider.length(); i++) {
	        	String[] providerLinks = rssProvider.getProviderLinks(providerNames[i]);
	        	for (int j = 0; j < providerLinks.length; j++)
	        		new RssDownloadTask(false, maxItemLoad).execute(providerNames[i], providerLinks[j]);
	        }
    }
    
    private ArrayList<Integer> fetchRss(String _provider, String _url) {
    	RssFeed _feed = new XmlPullHandler(_provider, _url).getFeed();
    	ArrayList<Integer> addList = null;
		if (_feed != null)
	    	addList = dbQuery.insertRssFeed(_feed);
		
		return addList;
    }
    
    private class RssDownloadTask extends AsyncTask<String, Void, Void> {
    	private ProgressDialog progDialog;
    	private boolean dialogShow;
    	private int limit;
    	private ArrayList<Integer> addList;
    	
    	public RssDownloadTask(boolean _dialogShow) {
    		dialogShow = _dialogShow;
    		limit = 0;
    	}
    	
    	public RssDownloadTask(boolean _dialogShow, int _limit) {
    		dialogShow = _dialogShow;
    		limit = _limit;
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
			addList = fetchRss(urls[0], urls[1]);
			return null;
		}
		
    	@Override
    	protected void onPostExecute(Void result) {
    		if (dialogShow)
    			progDialog.cancel();
    		if (addList != null) {
    			Toast.makeText(RssReaderActivity.this, R.string.rss_items_update_done, Toast.LENGTH_SHORT).show();
    			if (rssFeed == null)
    				rssFeed = new RssFeed();
    			if (limit != 0)
    				rssFeed.addList(dbQuery.getRssFeed(new String[] {rssProvider.getProviderNames()[curProvider]}, limit));
    			else
    				rssFeed = dbQuery.getRssFeed(new String[] {rssProvider.getProviderNames()[curProvider]}, limit);
    			
        		if (rssFeed != null) {
        			rssList.clear();
        			rssList.addAll(rssFeed.getList());
        			rssAdapter.notifyDataSetChanged();
        		}
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
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.rss_choose_provider)
			.setItems(rssProvider.getProviderNames(), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int item) {
					if (isTaskRunning)
						return;
					String name = rssProvider.getProviderNames()[item];
					curProvider = item;
					rssFeed = dbQuery.getRssFeed(new String[] {name});
					rssList.clear();
					rssList.addAll(rssFeed.getList());
					rssAdapter.notifyDataSetChanged();
				}
			});
			builder.create().show();
			break;
		
		case R.id.btn_refresh:
			if (isTaskRunning || curProvider == -1) {
				Toast.makeText(RssReaderActivity.this, R.string.rss_keep_updating, Toast.LENGTH_SHORT);
				break;
			}
			String name = rssProvider.getProviderNames()[curProvider];
			String[] link = rssProvider.getProviderLinks(name);
			for (int i = 0; i < link.length; i++)
				new RssDownloadTask(true).execute(name, link[i]);
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
				loadData(false);
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
				rssProvider = dbQuery.getRssProviderList();
				curProvider = rssProvider.length()-1;
				new RssDownloadTask(true).execute(addProvider[0], addProvider[1]);
				break;
			case RES_RSS_DELETE:
				loadData(false);
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
	}
}