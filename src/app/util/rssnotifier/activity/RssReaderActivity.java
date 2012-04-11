package app.util.rssnotifier.activity;

import java.io.IOException;
import java.net.URL;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import app.util.rssnotifier.R;
import app.util.rssnotifier.base.*;
import app.util.rssnotifier.database.DatabaseQuery;

public class RssReaderActivity extends ListActivity implements View.OnClickListener {
	protected static final int FILTER_REQ_CODE = RESULT_FIRST_USER + 1;
	private RssFeed rssFeed = null;
	private RssProviderList rssProvider = null;
	private int curProvider;
	
	private DatabaseQuery dbQuery;
	
    /** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rss_reader);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        
        Button btnAdd = (Button) findViewById(R.id.btn_add),
		        btnBrowse = (Button) findViewById(R.id.btn_browse),
		        btnRefresh = (Button) findViewById(R.id.btn_refresh);
        
        loadData();
        
        btnAdd.setOnClickListener(this);
        btnBrowse.setOnClickListener(this);
        btnRefresh.setOnClickListener(this);
    }
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		dbQuery.closeDB();
	}
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	RssItem item = this.rssFeed.getList().get(position);
    	Intent intent = new Intent(RssReaderActivity.this, RssDetailActivity.class);
    	intent.putExtra("rss_title", item.getTitle());
    	intent.putExtra("rss_link", item.getLink());
    	RssReaderActivity.this.startActivity(intent);
    }
    
    private void loadData() {
    	dbQuery = new DatabaseQuery(this);
        dbQuery.openDB();
        
        rssFeed = new RssFeed();
        rssProvider = new RssProviderList();
        rssFeed = dbQuery.getRssFeed();
        rssProvider = dbQuery.getRssProviderList();
        curProvider = 0;
        
		setListAdapter(new RssItemAdapter(this, R.layout.rss_item_list, rssFeed.getList()));
    }
    
    private void fetchRss(String _provider, String _url) {
    	RssFeed _feed = new XmlPullHandler(_provider, _url).getFeed();
		if (_feed != null)
	    	for (int i = 0; i < _feed.getList().size(); i++)
	    		dbQuery.insertRssItem(_feed.getList().get(i));
    }
    
    private class RssDownloadTask extends AsyncTask<String, Void, Void> {
    	private ProgressDialog progDialog;
    	private boolean dialogShow;
    	
    	public RssDownloadTask(boolean _dialogShow) {
    		dialogShow = _dialogShow;
    	}
    	
    	@Override
    	protected void onPreExecute() {
    		if (dialogShow) {
    			progDialog = new ProgressDialog(RssReaderActivity.this);
    			progDialog.setCancelable(false);
    			progDialog.setMessage("Fetching RSS");
    			progDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Hide", new DialogInterface.OnClickListener() {
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					dialog.dismiss();
    					Toast.makeText(getApplicationContext(), "Keep updating RSS items", Toast.LENGTH_SHORT).show();
//    					RssDownloadTask.this.cancel(true);
    				}
    			});
    			progDialog.show();    			
    		}
    	}
    	
		@Override
		protected Void doInBackground(String... urls) {
			fetchRss(urls[0], urls[1]);
			return null;
		}
		
    	@Override
    	protected void onPostExecute(Void result) {
    		if (dialogShow)
    			progDialog.cancel();
    		Toast.makeText(getApplicationContext(), "RSS items updating done", Toast.LENGTH_SHORT).show();
    		rssFeed = dbQuery.getRssFeed(new String[] {rssProvider.getProviderNames()[curProvider]});
    		if (rssFeed != null)
    			setListAdapter(new RssItemAdapter(RssReaderActivity.this, R.layout.rss_item_list, rssFeed.getList()));
    	}
    }
    
    private class RssAddDialog extends Dialog implements View.OnClickListener {
    	EditText txtProviderName, txtProviderLink;
    	Button btnOk, btnClear;
    	
		public RssAddDialog(Context context) {
			super(context);
			setContentView(R.layout.rss_add);
			setTitle("Add new RSS Provider");
			
			txtProviderName = (EditText) this.findViewById(R.id.txt_provider_name);
			txtProviderLink = (EditText) this.findViewById(R.id.txt_provider_link);
			btnOk = (Button) this.findViewById(R.id.btn_ok);
			btnClear = (Button) this.findViewById(R.id.btn_clear);
			
			btnOk.setOnClickListener(this);
			btnClear.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_ok:
				String name = txtProviderName.getText().toString(),
						link = txtProviderLink.getText().toString();
				boolean validated = true;
				
				if (!name.equals("") && !link.equals("")) {
					try {
						new URL(link).openConnection().connect();
					} catch (IOException e) {
						Toast.makeText(getApplicationContext(), "RSS source is invalid, please try again", Toast.LENGTH_SHORT).show();
						validated = false;
					}
					if (validated) {
						if (name.equals("vnExpress"))
							link = "http://vnexpress.net/rss/gl/trang-chu.rss";
						else if (name.equals("fsLink"))
							link = "http://fslink.us/?feed=rss2";
						else if (name.equals("CNN"))
							link = "http://rss.cnn.com/rss/edition.rss";
						else if (name.equals("Genk"))
							link = "http://genk.vn/trang-chu.rss";
						else if (name.equals("HdVnBits"))
							link = "http://hdvnbits.org/torrentrss.php?rows=10&cat=23,24,124,128";
						curProvider = rssProvider.getProviderNames().length;
						rssProvider.addProvider(name, link);
						new RssDownloadTask(true).execute(name, link);
						dbQuery.insertRssProvider(name, link);
						dismiss();
					}
				}
				else
					Toast.makeText(getApplicationContext(), "Please provide valid name and link", Toast.LENGTH_SHORT).show();
				break;
			case R.id.btn_clear:
				txtProviderName.setText("");
				txtProviderLink.setText("http://");
				break;
			default:
				break;
			}
		}
    	
    }

	@Override
	public void onClick(View v) {
		AlertDialog.Builder builder;
		switch (v.getId()) {
		case R.id.btn_add:
			RssAddDialog dialog = new RssAddDialog(this);
			dialog.show();
			break;
		case R.id.btn_browse:
			builder = new AlertDialog.Builder(this);
			builder.setTitle("Choose an RSS provider")
			.setItems(rssProvider.getProviderNames(), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int item) {
					String name = rssProvider.getProviderNames()[item];
					String link = rssProvider.getProviderLinks()[item];
					curProvider = item;
					rssFeed = dbQuery.getRssFeed(new String[] {name});
					setListAdapter(new RssItemAdapter(RssReaderActivity.this, R.layout.rss_item_list, rssFeed.getList()));
					new RssDownloadTask(false).execute(name, link);
				}
			});
			builder.create().show();
			break;
		case R.id.btn_refresh:
			String name = rssProvider.getProviderNames()[curProvider];
			String link = rssProvider.getProviderLinks()[curProvider];
			new RssDownloadTask(true).execute(name, link);
			break;
		default:
			break;
		}
	}
}