package fzn.projects.networkstatistics;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import fzn.projects.networkstatistics.db.*;
import fzn.projects.networkstatistics.db.NetworkStatisticsContract.ComboEntry;
import fzn.projects.networkstatistics.util.Util;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * 添加规则活动类
 * 生成添加规则的对话框
 */
public class RuleOperationActivity extends Activity {
	protected static final String TAG = "RuleOperationActivity";
	
	private Spinner networkTypeSpinner, periodUnitSpinner, totalUnitSpinner, usedUnitSpinner;
	private final BiMap<Integer, Integer> networkType = HashBiMap.create();
	private TimePicker beginTimePicker, endTimePicker;
	private EditText ruleNameInput, totalDataInput, usedDataInput, periodInput, priorityInput;
	private RadioGroup timeIntervalRadioGroup;
	private LinearLayout timeIntervalLayout;
	private RadioButton allDay, partialTime;
	private Resources res;
	private Intent intent;
	private boolean bEdit;

	final char[] timeUnit = Constants.TIME_UNIT;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rule_operation);
		
		networkTypeSpinner = (Spinner) findViewById(R.id.networkSpinner);
		for (int i = 0; i < Constants.SUPPORTED_NETWORK_TYPE.length; i++) {
			networkType.put(i, Constants.SUPPORTED_NETWORK_TYPE[i]);
		}

		periodUnitSpinner = (Spinner) findViewById(R.id.periodUnitSpinner);
		final ArrayAdapter<CharSequence> periodUnitAdapter = ArrayAdapter.createFromResource(this,
				R.array.timeUnit, android.R.layout.simple_spinner_item);
		periodUnitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		periodUnitSpinner.setAdapter(periodUnitAdapter);

		timeIntervalRadioGroup = (RadioGroup) findViewById(R.id.timeIntervalRadioGroup);
		allDay = (RadioButton) findViewById(R.id.allDayRadio);
		partialTime = (RadioButton) findViewById(R.id.partialTimeRadio);
		timeIntervalLayout = (LinearLayout) findViewById(R.id.timeIntervalLayout);
		timeIntervalLayout.setVisibility(View.GONE);
		beginTimePicker = (TimePicker) findViewById(R.id.beginTimePicker);
		beginTimePicker.setIs24HourView(true);
		beginTimePicker.setCurrentHour(0);
		beginTimePicker.setCurrentMinute(0);

		endTimePicker = (TimePicker) findViewById(R.id.endTimePicker);
		endTimePicker.setIs24HourView(true);
		endTimePicker.setCurrentHour(0);
		endTimePicker.setCurrentMinute(0);
		timeIntervalRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				Log.d(TAG, "RadioGroupCheckedChange " + checkedId);
				switch (checkedId) {
					case R.id.allDayRadio:
						timeIntervalLayout.setVisibility(View.GONE);
						break;
					case R.id.partialTimeRadio:
						timeIntervalLayout.setVisibility(View.VISIBLE);
						beginTimePicker.setCurrentHour(23);
						beginTimePicker.setCurrentMinute(0);
						endTimePicker.setCurrentHour(8);
						endTimePicker.setCurrentMinute(0);
						break;
					default:
						break;
				}
			}
		});
		
		final Button ok = (Button) findViewById(R.id.okRuleButton);
		final Button cancel = (Button) findViewById(R.id.cancelRuleButton);
		
		ruleNameInput = (EditText) findViewById(R.id.ruleNameInput);
		
		totalDataInput = (EditText) findViewById(R.id.totalDataInput);
		//totalDataInput.addTextChangedListener(new DataInputWatcher(totalDataInput));
		totalUnitSpinner = (Spinner) findViewById(R.id.totalUnitSpinner);
		
		usedDataInput = (EditText) findViewById(R.id.usedDataInput);
		//usedDataInput.addTextChangedListener(new DataInputWatcher(usedDataInput));
		usedUnitSpinner = (Spinner) findViewById(R.id.usedUnitSpinner);
		
		periodInput = (EditText) findViewById(R.id.periodInput);

		priorityInput = (EditText) findViewById(R.id.priorityInput);

		intent = getIntent();
		if (intent.hasExtra(Constants.Extra.COMBO_ID)) {
			setTitle(getResources().getString(R.string.title_activity_edit_rule));
			bEdit = true;
			readRule(intent.getLongExtra(Constants.Extra.COMBO_ID, -1));
		} else {
			bEdit = false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_rule, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void readRule(long id) {
		if (id == -1) return;
		Log.d(TAG, "readRule ID " + id);
		SQLiteDatabase db = NetworkStatisticsDbHelper.getInstance(this).getReadableDatabase();
		Cursor cursor = db.query(ComboEntry.TABLE_NAME,
				new String[] {ComboEntry.COLUMN_NAME,
						ComboEntry.COLUMN_CONN, ComboEntry.COLUMN_PERIOD,
						ComboEntry.COLUMN_QUANTUM, ComboEntry.COLUMN_USED,
						ComboEntry.COLUMN_TIME_INTERVAL_FROM, ComboEntry.COLUMN_TIME_INTERVAL_TO,
						ComboEntry.COLUMN_PRIORITY
				},
				ComboEntry._ID + " LIKE ?",
				new String[]{String.valueOf(id)}, null, null, null);
		cursor.moveToFirst();

		float[] iAQuantum, iAUsed;
		short[] resolvedPeriod;
		byte[] splitTime;
		DecimalFormat df = new DecimalFormat("0.###");

		ruleNameInput.setText(cursor.getString(cursor.getColumnIndex(ComboEntry.COLUMN_NAME)));

		networkTypeSpinner.setSelection(networkType.inverse().get(cursor.getInt(cursor.getColumnIndex(ComboEntry.COLUMN_CONN))));

		iAQuantum = Util.byteConverter(cursor.getLong(cursor.getColumnIndex(ComboEntry.COLUMN_QUANTUM)));
		iAUsed = Util.byteConverter(cursor.getLong(cursor.getColumnIndex(ComboEntry.COLUMN_USED)));
		totalDataInput.setText(df.format(iAQuantum[0]));
		totalUnitSpinner.setSelection((int) iAQuantum[1] - 2);
		usedDataInput.setText(df.format(iAUsed[0]));
		usedUnitSpinner.setSelection((int) iAUsed[1] - 2);

		resolvedPeriod = Util.resolveComboPeriod(cursor.getString(cursor.getColumnIndex(ComboEntry.COLUMN_PERIOD)));
		periodUnitSpinner.setSelection((int) resolvedPeriod[1]);
		periodInput.setText(String.valueOf(resolvedPeriod[0]));

		priorityInput.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(ComboEntry.COLUMN_PRIORITY))));

		splitTime = Util.splitTime(cursor.getString(cursor.getColumnIndex(ComboEntry.COLUMN_TIME_INTERVAL_FROM)));
		beginTimePicker.setCurrentHour((int) splitTime[0]);
		beginTimePicker.setCurrentMinute((int) splitTime[1]);
		splitTime = Util.splitTime(cursor.getString(cursor.getColumnIndex(ComboEntry.COLUMN_TIME_INTERVAL_TO)));
		endTimePicker.setCurrentHour((int) splitTime[0]);
		endTimePicker.setCurrentMinute((int) splitTime[1]);

		cursor.close();
	}

	/**
	 * 确定按钮的行为方法
	 * 将填入的信息插入数据库
	 * @param v
	 */
	public void addRuleOk(View v) {
		EditText[] requiredEditTexts = new EditText[] {ruleNameInput, totalDataInput, periodInput};
		for (EditText text : requiredEditTexts) {
			if (text.getText().toString().equals("")) {
				Toast.makeText(this, text.getTag().toString() + getString(R.string.notFilledToast), Toast.LENGTH_SHORT).show();
				return;
			}
		}
		if (partialTime.isChecked()) {
			if (beginTimePicker.getCurrentHour().equals(endTimePicker.getCurrentHour())
					&& beginTimePicker.getCurrentMinute().equals(endTimePicker.getCurrentMinute())) {
				Toast.makeText(this, getString(R.string.sameTimeToast), Toast.LENGTH_SHORT).show();
				return;
			}
		}
		SQLiteDatabase db = NetworkStatisticsDbHelper.getInstance(this).getWritableDatabase();

		String usedStr = usedDataInput.getText().toString().equals("") ? "0" : usedDataInput.getText().toString();

		float total = Float.valueOf(totalDataInput.getText().toString()),
				fUsed = Float.valueOf(usedStr);
		
		long quantum, used;
		
		if (totalUnitSpinner.getSelectedItemPosition() == 0)
			quantum = (long)(total * 0x100000); // 2 ^ 20 Bytes == 1 MegaBytes
		else
			quantum = (long)(total * 0x40000000); // 2 ^ 30 Bytes == 1 GigaBytes
		
		if (usedUnitSpinner.getSelectedItemPosition() == 0)
			used = (long)(fUsed * 0x100000);
		else
			used = (long)(fUsed * 0x40000000);
		
		ContentValues values = new ContentValues();
		values.put(ComboEntry.COLUMN_TIMESTAMP, System.currentTimeMillis());
		values.put(ComboEntry.COLUMN_NAME, ruleNameInput.getText().toString()); Log.d(ComboEntry.COLUMN_NAME, ruleNameInput.getText().toString());
		values.put(ComboEntry.COLUMN_CONN, networkType.get(networkTypeSpinner.getSelectedItemPosition())); Log.d(ComboEntry.COLUMN_CONN, String.valueOf(networkTypeSpinner.getSelectedItemPosition()));
		values.put(ComboEntry.COLUMN_QUANTUM, quantum); Log.d(ComboEntry.COLUMN_QUANTUM, String.valueOf(quantum));
		values.put(ComboEntry.COLUMN_USED, used); Log.d(ComboEntry.COLUMN_USED, String.valueOf(used));
		values.put(ComboEntry.COLUMN_PERIOD, periodInput.getText().toString() + timeUnit[periodUnitSpinner.getSelectedItemPosition()]); Log.d(ComboEntry.COLUMN_PERIOD, periodInput.getText().toString() + timeUnit[periodUnitSpinner.getSelectedItemPosition()]);
		values.put(ComboEntry.COLUMN_PERIOD_REMAIN, periodInput.getText().toString() + timeUnit[periodUnitSpinner.getSelectedItemPosition()]);
		values.put(ComboEntry.COLUMN_PRIORITY, priorityInput.getText().toString().equals("") ? String.valueOf(0) : priorityInput.getText().toString());
		values.put(ComboEntry.COLUMN_TIME_INTERVAL_FROM, beginTimePicker.getCurrentHour() + ":" + beginTimePicker.getCurrentMinute());
		values.put(ComboEntry.COLUMN_TIME_INTERVAL_TO, endTimePicker.getCurrentHour() + ":" + endTimePicker.getCurrentMinute());
		long rowID;
		if (!bEdit) {
			if ((rowID = db.insert(ComboEntry.TABLE_NAME, null, values)) != -1) {
				Intent intent = new Intent(Constants.Intent.ACTION_RULE_CHANGED);
				intent.putExtra(Constants.Extra.COMBO_ID, rowID);
				setResult(Activity.RESULT_OK, intent);
				finish();
			} else {
				Log.e(TAG, "Inserting failed.");
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		} else {
			rowID = db.update(ComboEntry.TABLE_NAME, values,
					ComboEntry._ID + " LIKE ?",
					new String[] { String.valueOf(intent.getLongExtra(Constants.Extra.COMBO_ID, -1)) });
			Intent intent = new Intent(Constants.Intent.ACTION_RULE_CHANGED);
			intent.putExtra(Constants.Extra.COMBO_ID, rowID);
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
	}

	/**
	 * 取消按钮的行为方法
	 * @param v
	 */
	public void addRuleCancel(View v) {
		setResult(Activity.RESULT_CANCELED);
		finish();
	}
	
	private class DataInputWatcher implements TextWatcher {
		
		private EditText editText;
		
		public DataInputWatcher(EditText editText) {
			this.editText = editText;
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO 自动生成的方法存根
			
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO 自动生成的方法存根
			String text = editText.toString();
			String str = stringFilter(text);
			if (!text.equals(str))
				editText.setText(str);
		}

		@Override
		public void afterTextChanged(Editable s) {
			// TODO 自动生成的方法存根
			
		}
		
		private String stringFilter(String str) throws PatternSyntaxException{
	        String regEx = "\\d+[bBkKmMtT]";
	        Pattern p = Pattern.compile(regEx);
	        Matcher m = p.matcher(str);
	        return m.replaceAll("");
	    }
	}

}
