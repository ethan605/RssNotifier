package app.util.rssnotifier.activity;

import android.app.*;
import android.content.DialogInterface;
import android.os.*;
import android.graphics.Bitmap;
import android.webkit.*;
import android.widget.*;
import app.util.rssnotifier.R;

public class RssDetailActivity extends Activity {
	private TextView txtTitle;
	private WebView webContent;
	private Bundle intentBundle;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rss_item_detail);
		
		intentBundle = getIntent().getExtras();

		txtTitle = (TextView) findViewById(R.id.rss_item_title);
		txtTitle.setText(intentBundle.getString("rss_title"));
		
		webContent = (WebView) findViewById(R.id.rss_item_content);
		webContent.getSettings().setJavaScriptEnabled(true);
		webContent.setWebViewClient(new WebViewClient() {
			private ProgressDialog progDialog;
			@Override
			public void	onPageStarted(WebView view, String url, Bitmap favicon) {
				progDialog = new ProgressDialog(RssDetailActivity.this);
	    		progDialog.setCancelable(false);
	    		progDialog.setMessage("Loading RSS content");
	    		progDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
	    		progDialog.show();
			}
			@Override
			public void	onPageFinished(WebView view, String url) {
				progDialog.cancel();
			}
		});
		webContent.loadUrl(intentBundle.getString("rss_link"));
	}
}
