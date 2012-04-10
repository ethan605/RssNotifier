package app.util.rssnotifier.activity;

import java.io.*;
import java.net.*;

import javax.xml.parsers.*;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.*;
import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import app.util.rssnotifier.*;
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
    
    private void getXML(String _strProvider, String _strUrl) throws ParserConfigurationException, SAXException, IOException {
    	HttpClient client = new DefaultHttpClient();
    	HttpGet request = new HttpGet();
    	try {
			request.setURI(new URI(_strUrl));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
    	HttpResponse response = client.execute(request);
    	Reader inputStream = new InputStreamReader(response.getEntity().getContent());
    	RssContentHandler rssContentHandler = new RssContentHandler();
    	InputSource inputSource = new InputSource();
    	inputSource.setCharacterStream(inputStream);
    	
    	SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
    	SAXParser saxParser = saxParserFactory.newSAXParser();
    	saxParser.parse(inputSource, rssContentHandler);
//    	XMLReader xmlReader = saxParser.getXMLReader();
//    	xmlReader.setContentHandler(rssContentHandler);
//    	xmlReader.parse(inputSource);
    	
    	RssFeed _feed = rssContentHandler.getFeed();
    	for (int i = 0; i < _feed.getList().size(); i++)
    		dbQuery.insertRssItem(_strProvider, _feed.getList().get(i));
    }
    
    private class RssDownloadTask extends AsyncTask<String, Integer, Long> {
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
    			progDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					dialog.cancel();
    					RssDownloadTask.this.cancel(true);
    				}
    			});
    			progDialog.show();    			
    		}
    	}
    	
		@Override
		protected Long doInBackground(String... urls) {
			try {
				getXML(urls[0], urls[1]);
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return (long) 0;
		}
		
    	@Override
    	protected void onPostExecute(Long result) {
    		if (dialogShow)
    			progDialog.cancel();
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
				
				if (!name.equals("") && !link.equals("")) {
					if (name.equals("vne"))
						link = "http://vnexpress.net/rss/gl/trang-chu.rss";
					else if (name.equals("fsl"))
						link = "http://fslink.us/?feed=rss2";
					else if (name.equals("cnn"))
						link = "http://rss.cnn.com/rss/edition.rss";
					else if (name.equals("genk"))
						link = "http://genk.vn/trang-chu.rss";
					curProvider = rssProvider.getProviderNames().length;
					rssProvider.addProvider(name, link);
					new RssDownloadTask(true).execute(name, link);
					dbQuery.insertRssProvider(name, link);
					dismiss();
				}
				else
					Toast.makeText(getApplicationContext(), "Please provide valid name and link", Toast.LENGTH_SHORT).show();
				break;
			case R.id.btn_clear:
				txtProviderName.setText("");
				txtProviderLink.setText("");
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