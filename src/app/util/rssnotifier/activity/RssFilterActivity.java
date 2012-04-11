package app.util.rssnotifier.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import app.util.rssnotifier.R;
import app.util.rssnotifier.base.RssProviderList;
import app.util.rssnotifier.database.DatabaseQuery;

public class RssFilterActivity extends Activity implements View.OnClickListener {
	private ListView lstRssProvider;
	private DatabaseQuery dbQuery;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rss_filter);
		
		lstRssProvider = (ListView) findViewById(R.id.lst_rss_provider);
		Button btnConfirm = (Button) findViewById(R.id.btn_confirm),
				btnClear = (Button) findViewById(R.id.btn_clear),
				btnCancel = (Button) findViewById(R.id.btn_cancel);
		
		RssProviderList rssProviderList = new RssProviderList();
		dbQuery = new DatabaseQuery(this);
		dbQuery.openDB();
		
		rssProviderList = dbQuery.getRssProviderList();
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
    		android.R.layout.simple_list_item_checked, rssProviderList.getProviderNames());
        lstRssProvider.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lstRssProvider.setAdapter(adapter);
        
        btnConfirm.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		dbQuery.closeDB();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_confirm:
			Intent intent = getIntent();
			intent.putExtra("filter-list", lstRssProvider.getCheckItemIds());
			setResult(RssReaderActivity.FILTER_REQ_CODE, intent);
			finish();
			break;
		case R.id.btn_clear:
			lstRssProvider.clearChoices();
			((ArrayAdapter<?>) lstRssProvider.getAdapter()).notifyDataSetChanged();
			break;
		case R.id.btn_cancel:
			finish();
			break;
		default:
			break;
		}
	}
}
