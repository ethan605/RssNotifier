package app.util.rssnotifier;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import app.util.rssnotifier.database.DatabaseQuery;

public class RssSettingActivity extends Activity implements OnClickListener {
	final String TAG = "RssSettingActivity";
	private RelativeLayout serviceSetting;
	private TextView txtServiceDescription;
	private CheckBox ckboxServiceEnable;

	private RelativeLayout timeIntervalSetting;
	private TextView txtTimeIntervalValue;

	private RelativeLayout maxItemLoadSetting;
	private TextView txtMaxItemLoadValue;
	
	private RelativeLayout trimmedTextSize;
	private TextView txtTrimmedTextSizeValue;
	
	private DatabaseQuery dbQuery;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rss_setting);
		
		serviceSetting = (RelativeLayout) findViewById(R.id.service_setting);
		txtServiceDescription = (TextView) findViewById(R.id.txt_service_setting_description);
		ckboxServiceEnable = (CheckBox) findViewById(R.id.ckbox_service_enable);
		
		timeIntervalSetting = (RelativeLayout) findViewById(R.id.time_interval_setting);
		txtTimeIntervalValue = (TextView) findViewById(R.id.txt_time_interval_value);
		
		maxItemLoadSetting = (RelativeLayout) findViewById(R.id.max_item_load_setting);
		txtMaxItemLoadValue = (TextView) findViewById(R.id.txt_max_item_load_value);
		
		trimmedTextSize = (RelativeLayout) findViewById(R.id.trimmed_text_size);
		txtTrimmedTextSizeValue = (TextView) findViewById(R.id.txt_trimmed_text_size_value);
		
		serviceSetting.setOnClickListener(this);
		ckboxServiceEnable.setOnClickListener(this);
		timeIntervalSetting.setOnClickListener(this);
		maxItemLoadSetting.setOnClickListener(this);
		trimmedTextSize.setOnClickListener(this);
		
		if (isServiceRunning(RssNotificationService.class.getCanonicalName())) {
			ckboxServiceEnable.setChecked(true);
			txtServiceDescription.setText(R.string.service_running);
		} else {
			ckboxServiceEnable.setChecked(false);
			txtServiceDescription.setText(R.string.service_stopped);
		}
		
		dbQuery = new DatabaseQuery(this);
		dbQuery.openDB();
		
		int[] settings = dbQuery.getRssSettings();
		txtTimeIntervalValue.setText(settings[0] + " " + getString(R.string.minutes));
		txtMaxItemLoadValue.setText(settings[1] + " " + getString(R.string.items));
		txtTrimmedTextSizeValue.setText(settings[2] + " " + getString(R.string.characters));
	}
	
	@Override
	public void onDestroy() {
		dbQuery.closeDB();
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.ckbox_service_enable:
		case R.id.service_setting:
			if (!ckboxServiceEnable.isChecked()) {
				startService(new Intent(RssSettingActivity.this, RssNotificationService.class));
				ckboxServiceEnable.setChecked(true);
				txtServiceDescription.setText(R.string.service_running);
			} else {
				Intent intent = new Intent();
				intent.setAction(RssNotificationService.ACTION);
				intent.putExtra("REQ", RssReaderActivity.REQ_STOP_SERVICE);
				sendBroadcast(intent);
				ckboxServiceEnable.setChecked(false);
				txtServiceDescription.setText(R.string.service_stopped);
			}
			break;
		case R.id.time_interval_setting:
			new InputNumberDialog(this, R.id.time_interval_setting, 1, 100).show();
			break;
		case R.id.max_item_load_setting:
			new InputNumberDialog(this, R.id.max_item_load_setting, 1, 100).show();
			break;
		case R.id.trimmed_text_size:
			new InputNumberDialog(this, R.id.trimmed_text_size, 1, 1000).show();
		default:
			break;
		}
	}
	
	private class InputNumberDialog extends Dialog {
		private EditText txtInputNumber;
		private Button btnSave;
		private int viewId, minValue, maxValue;
		public InputNumberDialog(Context context, int id, int min, int max) {
			super(context);
			setContentView(R.layout.input_number_dialog);
			setTitle(R.string.input_number_dialog_title);
			
			viewId = id;
			minValue = min;
			maxValue = max;
			txtInputNumber = (EditText) findViewById(R.id.txt_input_number);
			txtInputNumber.setHint("(" + minValue + " - " + maxValue + ")");
			btnSave = (Button) findViewById(R.id.btn_save);
			
			btnSave.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (txtInputNumber.getText().toString().equals(""))
						return;
					
					int inputNumber = Integer.parseInt(txtInputNumber.getText().toString());
					if (inputNumber < minValue || inputNumber > maxValue)
						Toast.makeText(RssSettingActivity.this, R.string.invalid_input_number, Toast.LENGTH_SHORT).show();
					else {
						if (viewId == R.id.time_interval_setting) {
							txtTimeIntervalValue.setText(inputNumber + " " + getString(R.string.minutes));
							dbQuery.updateRssSettings(new int[] {inputNumber, 0, 0});
						}
						else if (viewId == R.id.max_item_load_setting) {
							txtMaxItemLoadValue.setText(inputNumber + " " + getString(R.string.items));
							dbQuery.updateRssSettings(new int[] {0, inputNumber, 0});
						} else {
							Toast.makeText(RssSettingActivity.this, R.string.restart_to_change, Toast.LENGTH_SHORT).show();
							txtTrimmedTextSizeValue.setText(inputNumber + " " + getString(R.string.characters));
							dbQuery.updateRssSettings(new int[] {0, 0, inputNumber});
						}
						cancel();
					}
				}
			});
		}
	}
	
	private boolean isServiceRunning(String serviceName) {
	    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
	        if (service.service.getClassName().equals(serviceName))
	            return true;
	    return false;
	}
}
