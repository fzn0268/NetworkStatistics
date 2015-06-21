package fzn.projects.networkstatistics;

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
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

/**
 * 添加规则活动类
 * 生成添加规则的对话框
 */
public class RuleOperationActivity extends Activity {
	protected static final String TAG = "RuleOperationActivity";
	
	private Spinner networkConnSpinner, timeUnitSpinner, totalUnitSpinner, usedUnitSpinner;
	private TimePicker startTimePicker, endTimePicker;
	private EditText ruleNameInput, totalDataInput, usedDataInput, periodInput;
	private Resources res;
	private Intent intent;
	private boolean bEdit;

	final char[] timeUnit = Constants.TIME_UNIT;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rule_operation);
		
		networkConnSpinner = (Spinner) findViewById(R.id.networkSpinner);

		timeUnitSpinner = (Spinner) findViewById(R.id.timeUnitSpinner);
		final ArrayAdapter<CharSequence> timeUnitAdapter = ArrayAdapter.createFromResource(this, 
				R.array.timeUnit, android.R.layout.simple_spinner_item);
		timeUnitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		timeUnitSpinner.setAdapter(timeUnitAdapter);
		
		startTimePicker = (TimePicker) findViewById(R.id.startTimePicker);
		startTimePicker.setCurrentHour(0);
		startTimePicker.setCurrentMinute(0);
		startTimePicker.setIs24HourView(true);
		
		endTimePicker = (TimePicker) findViewById(R.id.endTimePicker);
		endTimePicker.setCurrentHour(23);
		endTimePicker.setCurrentMinute(59);
		endTimePicker.setIs24HourView(true);
		
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
		
		res = getResources();
		intent = getIntent();
		if (intent.hasExtra(Constants.Extra.COMBOID)) {
			bEdit = true;
			readRule(intent.getIntExtra(Constants.Extra.COMBOID, -1));
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

	public void readRule(Integer id) {
		if (id == -1) return;
		Log.d(TAG, "readRule ID " + id);
		SQLiteDatabase db = new NetworkStatisticsDbHelper(this).getReadableDatabase();
		Cursor cursor = db.query(ComboEntry.TABLE_NAME, new String[]{ComboEntry.COLUMN_COMBO_NAME,
						ComboEntry.COLUMN_COMBO_CONN, ComboEntry.COLUMN_PERIOD, ComboEntry.COLUMN_QUANTUM,
						ComboEntry.COLUMN_USED, ComboEntry.COLUMN_TIME_RANGE_FROM,
						ComboEntry.COLUMN_TIME_RANGE_TO}, ComboEntry._ID + " LIKE ?",
				new String[] {String.valueOf(id)}, null, null, null);
		cursor.moveToFirst();
		int[] iAQuantum, iAUsed;
		String period;
		String[] timeFrom, timeTo;
		ruleNameInput.setText(cursor.getString(cursor.getColumnIndex(ComboEntry.COLUMN_COMBO_NAME)));
		networkConnSpinner.setSelection(cursor.getInt(cursor.getColumnIndex(ComboEntry.COLUMN_COMBO_CONN)));
		iAQuantum = Util.byteConverter(cursor.getLong(cursor.getColumnIndex(ComboEntry.COLUMN_QUANTUM)));
		iAUsed = Util.byteConverter(cursor.getLong(cursor.getColumnIndex(ComboEntry.COLUMN_USED)));
		totalDataInput.setText(String.valueOf(iAQuantum[0]));
		totalUnitSpinner.setSelection(iAQuantum[1] - 2);
		usedDataInput.setText(String.valueOf(iAUsed[0]));
		usedUnitSpinner.setSelection(iAUsed[1] - 2);
		period = cursor.getString(cursor.getColumnIndex(ComboEntry.COLUMN_PERIOD));
		switch (period.charAt(period.length() - 1)) {
			case 'm':
				timeUnitSpinner.setSelection(0);
				break;
			case 'd':
				timeUnitSpinner.setSelection(1);
				break;
			case 'w':
				timeUnitSpinner.setSelection(2);
				break;
			case 'y':
				timeUnitSpinner.setSelection(3);
				break;
			default:
				break;
		}
		periodInput.setText(period.substring(0, period.length() - 1));
		timeFrom = cursor.getString(cursor.getColumnIndex(ComboEntry.COLUMN_TIME_RANGE_FROM)).split(":");
		startTimePicker.setCurrentHour(Integer.valueOf(timeFrom[0]));
		startTimePicker.setCurrentMinute(Integer.valueOf(timeFrom[1]));
		timeTo = cursor.getString(cursor.getColumnIndex(ComboEntry.COLUMN_TIME_RANGE_TO)).split(":");
		endTimePicker.setCurrentHour(Integer.valueOf(timeTo[0]));
		endTimePicker.setCurrentMinute(Integer.valueOf(timeTo[1]));
	}

	/**
	 * 确定按钮的行为方法
	 * 将填入的信息插入数据库
	 * @param v
	 */
	public void addRuleOk(View v) {
		EditText[] editTexts = new EditText[] {ruleNameInput, totalDataInput, usedDataInput, periodInput};
		for (EditText text : editTexts) {
			if (text.getText().toString().equals("")) {
				Toast.makeText(this, text.getTag().toString() + getResources().getString(R.string.notFilled), Toast.LENGTH_SHORT).show();
				return;
			}
		}
		SQLiteDatabase db = new NetworkStatisticsDbHelper(this).getWritableDatabase();

		String usedStr = usedDataInput.getText().toString();

		float total = Float.valueOf(totalDataInput.getText().toString()),
				fUsed = Float.valueOf(usedStr);
		
		long quantum = Long.valueOf(totalDataInput.getText().toString()),
				used = Long.valueOf(usedStr.equals("") ? "0" : usedStr);
		
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
		values.put(ComboEntry.COLUMN_COMBO_NAME, ruleNameInput.getText().toString()); Log.d(ComboEntry.COLUMN_COMBO_NAME, ruleNameInput.getText().toString());
		values.put(ComboEntry.COLUMN_COMBO_CONN, networkConnSpinner.getSelectedItemPosition()); Log.d(ComboEntry.COLUMN_COMBO_CONN, String.valueOf(networkConnSpinner.getSelectedItemPosition()));
		values.put(ComboEntry.COLUMN_QUANTUM, quantum); Log.d(ComboEntry.COLUMN_QUANTUM, String.valueOf(quantum));
		values.put(ComboEntry.COLUMN_USED, used); Log.d(ComboEntry.COLUMN_USED, String.valueOf(used));
		values.put(ComboEntry.COLUMN_PERIOD, periodInput.getText().toString() + timeUnit[timeUnitSpinner.getSelectedItemPosition()]); Log.d(ComboEntry.COLUMN_PERIOD, periodInput.getText().toString() + timeUnit[timeUnitSpinner.getSelectedItemPosition()]);
		values.put(ComboEntry.COLUMN_PERIOD_REMAIN, periodInput.getText().toString() + timeUnit[timeUnitSpinner.getSelectedItemPosition()]);
		values.put(ComboEntry.COLUMN_TIME_RANGE_FROM, startTimePicker.getCurrentHour() + ":" + startTimePicker.getCurrentMinute());
		values.put(ComboEntry.COLUMN_TIME_RANGE_TO, endTimePicker.getCurrentHour() + ":" + endTimePicker.getCurrentMinute());
		if (!bEdit) {
			if (db.insert(ComboEntry.TABLE_NAME, null, values) != -1) {
				setResult(Activity.RESULT_OK);
				finish();
			} else {
				Log.e(TAG, "Inserting failed.");
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		} else {
			db.update(ComboEntry.TABLE_NAME, values,
					ComboEntry._ID + " LIKE ?",
					new String[] {String.valueOf(intent.getIntExtra(Constants.Extra.COMBOID, -1))});
			setResult(Activity.RESULT_OK);
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
