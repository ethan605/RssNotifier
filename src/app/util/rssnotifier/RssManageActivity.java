package app.util.rssnotifier;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import app.util.rssnotifier.R;
import app.util.rssnotifier.base.RssProviderList;
import app.util.rssnotifier.base.XmlPullHandler;
import app.util.rssnotifier.database.DatabaseQuery;

public class RssManageActivity extends Activity implements OnClickListener {
	final String TAG = "RssManageActivity";
	private ArrayList<String> providerList;
	private ArrayAdapter<String> providerAdapter;
	private ListView lstProviderList;
	private RssProviderList rssProviders;
	private DatabaseQuery dbQuery;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rss_manage);
        
        lstProviderList = (ListView) findViewById(R.id.lst_rss_provider);
        lstProviderList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		Button btnAdd = (Button) findViewById(R.id.btn_add),
				btnSetting = (Button) findViewById(R.id.btn_setting),
				btnSelect = (Button) findViewById(R.id.btn_select);
		btnAdd.setOnClickListener(this);
		btnSetting.setOnClickListener(this);
		btnSelect.setOnClickListener(this);
		
		dbQuery = new DatabaseQuery(this);
		dbQuery.openDB();
		rssProviders = dbQuery.getRssProviderList(null);
        
		providerList = new ArrayList<String>();
		if (rssProviders != null)
			for (String name : rssProviders.getProviderNames())
				providerList.add(name);
		
		providerAdapter = new ArrayAdapter<String>(this,
			android.R.layout.simple_list_item_1, providerList);
        lstProviderList.setAdapter(providerAdapter);
	}
	
	@Override
	public void onDestroy() {
		dbQuery.closeDB();
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		Button btnAdd = (Button) findViewById(R.id.btn_add);
		Button btnSetting = (Button) findViewById(R.id.btn_setting);
		Button btnSelect = (Button) findViewById(R.id.btn_select);
		switch (v.getId()) {
		case R.id.btn_add:
			if (btnAdd.getText().toString().equals(getString(R.string.btn_add_text)))
				new RssAddDialog(this).show();
			else if (btnAdd.getText().toString().equals(getString(R.string.btn_select_all_text))) {
				for (int i = 0; i < lstProviderList.getCount(); i++)
					lstProviderList.setItemChecked(i, true);
				btnAdd.setText(R.string.btn_select_none_text);
			} else if (btnAdd.getText().toString().equals(getString(R.string.btn_select_none_text))) {
				for (int i = 0; i < lstProviderList.getCount(); i++)
					lstProviderList.setItemChecked(i, false);
				btnAdd.setText(R.string.btn_select_all_text);
			}
			break;
		case R.id.btn_setting:
			if (btnSetting.getText().toString().equals("Setting")) {
				break;
			}
			
			if (lstProviderList.getCheckItemIds().length == 0)
				break;
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.rss_delete_provider)
					.setCancelable(false)
					.setPositiveButton(R.string.btn_ok_text, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							long[] checkedItems = lstProviderList.getCheckItemIds();
							for (int i = checkedItems.length-1; i >= 0; i--) {
								dbQuery.deleteRssProvider(providerList.get((int) checkedItems[i]));
								providerList.remove((int) checkedItems[i]);
							}
							providerAdapter.notifyDataSetChanged();
							lstProviderList.clearChoices();
							setResult(RssReaderActivity.RES_RSS_DELETE, new Intent().putExtra("delete-provider", checkedItems));
						}
					})
					.setNegativeButton(R.string.btn_cancel_text, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {	
						}
					}).show();
			break;
		case R.id.btn_select:
			if (btnSelect.getText().toString().equals(getString(R.string.btn_select_mode_text))) {
				providerAdapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_multiple_choice, providerList);
				btnAdd.setText(R.string.btn_select_all_text);
				btnSetting.setText(R.string.btn_delete_text);
				btnSelect.setText(R.string.btn_browse_mode_text);
			} else {
				providerAdapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, providerList);
				btnAdd.setText(R.string.btn_add_text);
				btnSetting.setText(R.string.btn_setting_text);
				btnSelect.setText(R.string.btn_select_mode_text);
			}
			lstProviderList.setAdapter(providerAdapter);
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onBackPressed() {
		dbQuery.closeDB();
		finish();
	}
	
	private class RssAddDialog extends Dialog implements View.OnClickListener {
    	private EditText txtProviderName, txtProviderLink;
    	private Button btnOk, btnClear;
    	private boolean validate;
    	
    	private class RssValidate extends AsyncTask<String, Void, Void> {
        	private ProgressDialog progDialog;
        	private String name, link;
        	
        	public RssValidate(String _name, String _link) {
        		name = _name;
        		link = _link;
			}
        	
        	@Override
        	protected void onPreExecute() {
        		progDialog = new ProgressDialog(RssManageActivity.this);
    			progDialog.setCancelable(false);
    			progDialog.setMessage(getString(R.string.rss_source_validate));
    			progDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.btn_cancel_text), new DialogInterface.OnClickListener() {
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					dialog.dismiss();
    					RssValidate.this.cancel(true);
    				}
    			});
    			progDialog.show();
        	}
    		@Override
    		protected Void doInBackground(String... urls) {
    			validate = XmlPullHandler.feedValidate(urls[0]);
    			return null;
    		}
    		@Override
    		protected void onPostExecute(Void result) {
    			progDialog.cancel();
    			if (!validate)
    				Toast.makeText(RssManageActivity.this, R.string.rss_source_invalid, Toast.LENGTH_SHORT).show();
    			else if (dbQuery.insertRssProvider(name, link) == -1)
    				Toast.makeText(RssManageActivity.this, R.string.rss_provider_link_exists, Toast.LENGTH_SHORT).show();
    			else
    				process(name, link);
    		}
        }
    	
		public RssAddDialog(Context context) {
			super(context);
			setContentView(R.layout.rss_add);
			setTitle(R.string.rss_add_provider);
			
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
				
				if (name.equals("vne"))
					link = "http://vnexpress.net/rss/gl/trang-chu.rss";
				else if (name.equals("vnn"))
					link = "http://vietnamnet.vn/rss/moi-nong.rss";
				else if (name.equals("dan"))
					link = "http://dantri.com.vn/trangchu.rss";
				else if (name.equals("cnn"))
					link = "http://rss.cnn.com/rss/edition.rss";
				else if (name.equals("bbc"))
					link = "http://feeds.bbci.co.uk/news/rss.xml";
				else if (name.equals("gen"))
					link = "http://genk.vn/trang-chu.rss";
				else if (name.equals("fsl"))
					link = "http://fslink.us/?feed=rss2";
				else if (name.equals("hdv"))
					link = "http://hdvnbits.org/torrentrss.php?rows=10&cat=23,24,124,128";
				
				if (!name.equals("") && !link.equals(""))
					new RssValidate(name, link).execute(link);
				else
					Toast.makeText(RssManageActivity.this, R.string.rss_add_blank_error, Toast.LENGTH_SHORT).show();
				break;
			case R.id.btn_clear:
				txtProviderName.setText("");
				txtProviderLink.setText("http://");
				txtProviderName.requestFocus();
				break;
			default:
				break;
			}
		}
		
		private void process(String name, String link) {
			cancel();
			setResult(RssReaderActivity.RES_RSS_ADD, new Intent().putExtra("add-provider", new String[] {name, link}));
			finish();
		}
    }
}
