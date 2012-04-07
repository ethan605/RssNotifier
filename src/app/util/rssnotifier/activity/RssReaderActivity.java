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
import android.view.View.*;
import android.widget.*;
import app.util.rssnotifier.*;

public class RssReaderActivity extends ListActivity {
	private Button btnGet;
	private EditText txtUrl;
	private RssFeed rssFeed = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        
        btnGet = (Button) findViewById(R.id.btn_get);
        txtUrl = (EditText) findViewById(R.id.txt_url);
        
//        new RssDownloadTask().execute("http://vnexpress.net/rss/gl/trang-chu.rss");
        new RssDownloadTask().execute("http://fslink.us/?feed=rss2");
        btnGet.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new RssDownloadTask().execute(txtUrl.getText().toString());
			}
        });
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	RssItem item = this.rssFeed.getList().get(position);
    	Intent intent = new Intent(RssReaderActivity.this, RssDetailActivity.class);
    	intent.putExtra("rss_title", item.getTitle());
    	intent.putExtra("rss_link", item.getLink());
    	RssReaderActivity.this.startActivity(intent);
    }
    
    private void getXML(String _strUrl) throws ParserConfigurationException, SAXException, IOException {
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
    	XMLReader xmlReader = saxParser.getXMLReader();
    	xmlReader.setContentHandler(rssContentHandler);
    	xmlReader.parse(inputSource);
    	
    	this.rssFeed = rssContentHandler.getFeed();
    }
    
    private class RssDownloadTask extends AsyncTask<String, Integer, Long> {
    	private ProgressDialog progDialog;
    	
    	@Override
    	protected void onPreExecute() {
    		progDialog = new ProgressDialog(RssReaderActivity.this);
    		progDialog.setCancelable(true);
    		progDialog.setMessage("Fetching RSS");
    		progDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					RssDownloadTask.this.cancel(true);
					RssReaderActivity.this.finish();
				}
			});
    		progDialog.show();
    	}
    	
		@Override
		protected Long doInBackground(String... urls) {
			try {
				getXML(urls[0]);
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
    		progDialog.cancel();
    		if (rssFeed != null) {
    			RssItemAdapter itemAdater = new RssItemAdapter(RssReaderActivity.this, R.layout.rss_item_list, rssFeed.getList());
    			setListAdapter(itemAdater);
    		}
    	}
    }
}