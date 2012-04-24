package app.util.rssnotifier;

import java.io.InputStream;
import java.util.ArrayList;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import app.util.rssnotifier.R;
import app.util.rssnotifier.base.RssPreset;
import app.util.rssnotifier.base.RssProvider;
import app.util.rssnotifier.base.RssProviderList;
import app.util.rssnotifier.base.RssContentHandler;
import app.util.rssnotifier.database.DatabaseQuery;

public class RssManageActivity extends Activity implements OnClickListener {
	final String TAG = "RssManageActivity";
	
	private static RssPreset rssPreset = null;
	private ArrayList<String> providerList;
	private ArrayAdapter<String> providerAdapter;
	private ListView lstProviderList;
	private RssProviderList rssProviders;
	private DatabaseQuery dbQuery;
	
	Button btnAdd, btnSetting, btnSelect;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rss_manage);
        
        lstProviderList = (ListView) findViewById(R.id.lst_rss_provider);
        lstProviderList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		btnAdd = (Button) findViewById(R.id.btn_add);
		btnSetting = (Button) findViewById(R.id.btn_setting);
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
        
        if (rssPreset == null)
        	parsePreset();
	}
	
	@Override
	public void onDestroy() {
		dbQuery.closeDB();
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		TextView txtAddLabel, txtSettingLabel, txtSelectLabel;
		txtAddLabel = (TextView) findViewById(R.id.txt_add_label);
		txtSettingLabel = (TextView) findViewById(R.id.txt_setting_label);
		txtSelectLabel = (TextView) findViewById(R.id.txt_select_label);
		switch (v.getId()) {
		case R.id.btn_add:
			if (txtAddLabel.getText().toString().equals(getString(R.string.btn_add_text)))
				new RssAddDialog(this).show();
			else if (txtAddLabel.getText().toString().equals(getString(R.string.btn_select_all_text))) {
				for (int i = 0; i < lstProviderList.getCount(); i++)
					lstProviderList.setItemChecked(i, true);
				btnAdd.setBackgroundResource(R.drawable.btn_select_none_background);
				txtAddLabel.setText(R.string.btn_select_none_text);
			} else if (txtAddLabel.getText().toString().equals(getString(R.string.btn_select_none_text))) {
				for (int i = 0; i < lstProviderList.getCount(); i++)
					lstProviderList.setItemChecked(i, false);
				btnAdd.setBackgroundResource(R.drawable.btn_select_all_background);
				txtAddLabel.setText(R.string.btn_select_all_text);
			}
			break;
		case R.id.btn_setting:
			if (txtSettingLabel.getText().toString().equals(getString(R.string.btn_setting_text))) {
				startActivity(new Intent(RssManageActivity.this, RssSettingActivity.class));
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
			if (txtSelectLabel.getText().toString().equals(getString(R.string.btn_select_text))) {
				providerAdapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_multiple_choice, providerList);
				btnAdd.setBackgroundResource(R.drawable.btn_select_all_background);
				txtAddLabel.setText(R.string.btn_select_all_text);
				btnSetting.setBackgroundResource(R.drawable.btn_delete_background);
				txtSettingLabel.setText(R.string.btn_delete_text);
				btnSelect.setBackgroundResource(R.drawable.btn_browse_background);
				txtSelectLabel.setText(R.string.btn_browse_text);
			} else {
				providerAdapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, providerList);
				btnAdd.setBackgroundResource(R.drawable.btn_add_background);
				txtAddLabel.setText(R.string.btn_add_text);
				btnSetting.setBackgroundResource(R.drawable.btn_setting_background);
				txtSettingLabel.setText(R.string.btn_setting_text);
				btnSelect.setBackgroundResource(R.drawable.btn_select_background);
				txtSelectLabel.setText(R.string.btn_select_text);
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
    	private AutoCompleteTextView txtProviderName, txtProviderLink;
    	private Button btnOk, btnClear;
    	
    	private class RssValidate extends AsyncTask<String, Void, Boolean> {
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
    		protected Boolean doInBackground(String... urls) {
    			return RssContentHandler.feedValidate(link);
    		}
    		@Override
    		protected void onPostExecute(Boolean result) {
    			progDialog.cancel();
    			if (!result)
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
			
			final RssProviderList providers = new RssProviderList(rssPreset.getProvider());
			String[] nameList = providers.getProviderNames();
			ArrayAdapter<String> providerAdapter = new ArrayAdapter<String>(RssManageActivity.this, R.layout.list_item, nameList);

			txtProviderName = (AutoCompleteTextView) this.findViewById(R.id.txt_provider_name);
			txtProviderName.setAdapter(providerAdapter);
			
			txtProviderLink = (AutoCompleteTextView) this.findViewById(R.id.txt_provider_link);
			txtProviderName.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
					String[] linkList = providers.getProviderLinks(s.toString());
					if (linkList != null) {
						ArrayAdapter<String> providerAdapter = new ArrayAdapter<String>(RssManageActivity.this, R.layout.list_item, linkList);
						txtProviderLink.setAdapter(providerAdapter);
						txtProviderLink.setText(linkList[0]);
					}
				}
				
				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				}
				
				@Override
				public void afterTextChanged(Editable arg0) {
				}
			});
			
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
				
				if (!name.equals("") && !link.equals(""))
					new RssValidate(name, link).execute();
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
	
	private void parsePreset() {
		try {
			Serializer serializer = new Persister();
			InputStream inputStream = getAssets().open("preset.xml");
			rssPreset = new RssPreset();
			rssPreset = serializer.read(RssPreset.class, inputStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
