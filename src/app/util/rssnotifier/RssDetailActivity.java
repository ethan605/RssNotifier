package app.util.rssnotifier;

import android.app.*;
import android.content.Intent;
import android.net.Uri;
import android.os.*;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.*;
import android.widget.*;
import app.util.rssnotifier.R;

public class RssDetailActivity extends Activity {
	final String TAG = "RssDetailActivity";
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.rss_item_detail);
		
		final Bundle bundle = getIntent().getExtras();

		TextView txtTitle = (TextView) findViewById(R.id.rss_item_title);
		txtTitle.setText(bundle.getString("rss_title"));
		txtTitle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(bundle.getString("rss_link")));
				RssDetailActivity.this.startActivity(intent);
			}
		});
		
		WebView webContent = (WebView) findViewById(R.id.rss_item_content);
		webContent.getSettings().setJavaScriptEnabled(true);
		webContent.getSettings().setBuiltInZoomControls(true);
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
		webContent.loadUrl(bundle.getString("rss_link"));
	}
}
