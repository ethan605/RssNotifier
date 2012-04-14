package app.util.rssnotifier.activity;

import android.app.*;
import android.os.*;
import android.graphics.Bitmap;
import android.view.Window;
import android.webkit.*;
import android.widget.*;
import app.util.rssnotifier.R;

public class RssDetailActivity extends Activity {
	private TextView txtTitle;
	private WebView webContent;
	private Bundle intentBundle;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.rss_item_detail);
		
		intentBundle = getIntent().getExtras();

		txtTitle = (TextView) findViewById(R.id.rss_item_title);
		txtTitle.setText(intentBundle.getString("rss_title"));
		
		webContent = (WebView) findViewById(R.id.rss_item_content);
		webContent.getSettings().setJavaScriptEnabled(true);
		webContent.setWebViewClient(new WebViewClient() {
			@Override
			public void	onPageStarted(WebView view, String url, Bitmap favicon) {
				Toast.makeText(RssDetailActivity.this, R.string.rss_content_loading, Toast.LENGTH_SHORT).show();
				setProgressBarIndeterminateVisibility(true);
			}
			@Override
			public void	onPageFinished(WebView view, String url) {
				setProgressBarIndeterminateVisibility(false);
			}
		});
		webContent.loadUrl(intentBundle.getString("rss_link"));
	}
}
