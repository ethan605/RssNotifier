package app.util.rssnotifier.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class RssNotifierActivity extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RssNotifierActivity.this.startActivity(new Intent(RssNotifierActivity.this, RssReaderActivity.class));
	}
}
