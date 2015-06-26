package fzn.projects.networkstatistics;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import fzn.projects.networkstatistics.db.NetworkStatisticsContract.ComboEntry;
import fzn.projects.networkstatistics.db.NetworkStatisticsDbHelper;
import fzn.projects.networkstatistics.util.Util;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */

/**
 * 规则碎片类
 * 从数据库中查询并显示已添加的规则，列出规则中的总流量、已用流量、可用流量
 */
public class RulesFragment extends Fragment implements
		ListView.OnItemClickListener, ListView.OnItemLongClickListener {
	private static final String TAG = RulesFragment.class.getSimpleName();

	/**
     * The fragment argument representing the section number for this
     * fragment.
     */
	private static final String ARG_SECTION_NUMBER = "section_number";

	private ListView mListView;
	private SQLiteOpenHelper dbHelper;

	// This is the Adapter being used to display the list's data.
    private SimpleCursorAdapter mAdapter;

	private long longClickId;

	private ActionMode.Callback mActionModeCallback = new RulesActionModeCallback();
	private ActionMode mActionMode;
	private ArrayList<View> lastItems = new ArrayList<>();

	private LocalBroadcastManager mLocalBcMgr;

	/**
     * Returns a new instance of this fragment for the given section
     * number.
     */
	public static RulesFragment newInstance(int sectionNumber) {
		RulesFragment fragment = new RulesFragment();
		Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
	}
	
	public RulesFragment() {
	}

	/**
	 * Receive the result from a previous call to
	 * {@link #startActivityForResult(Intent, int)}.  This follows the
	 * related Activity API as described there in
	 * {@link Activity#onActivityResult(int, int, Intent)}.
	 *
	 * @param requestCode The integer request code originally supplied to
	 *                    startActivityForResult(), allowing you to identify who this
	 *                    result came from.
	 * @param resultCode  The integer result code returned by the child activity
	 *                    through its setResult().
	 * @param data        An Intent, which can return result data to the caller
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult");
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			mLocalBcMgr.sendBroadcast(data);
			new RefreshRulesTask().execute(dbHelper);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(TAG, "onActivityCreated");
		// We have a menu item to show in action bar.
		setHasOptionsMenu(true);
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		mLocalBcMgr = LocalBroadcastManager.getInstance(getActivity());
		Intent intent = new Intent(Constants.Intent.ACTION_RULE_UPDATE);
		mLocalBcMgr.sendBroadcastSync(intent);
		super.onCreate(savedInstanceState);
    }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
        final View view = inflater.inflate(R.layout.fragment_rules, container, false);
		mListView = (ListView) view.findViewById(R.id.rule_list);
		mListView.setEmptyView(view.findViewById(R.id.rule_empty));
		setEmptyText(getResources().getText(R.string.noRule));
        // Set OnItemClickListener so we can be notified on item clicks
		if (mListView != null) {
			mListView.setOnItemClickListener(this);
			mListView.setOnItemLongClickListener(this);
		}
		dbHelper = NetworkStatisticsDbHelper.getInstance(getActivity());
        new LoadRulesTask().execute(dbHelper);

        return view;
    }
	
	@Override
    public void onAttach(Activity activity) {
		Log.d(TAG, "onAttach");
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
				getArguments().getInt(ARG_SECTION_NUMBER));
    }
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    // Inflate the menu items for use in the action bar

	    menu.setGroupVisible(R.id.main_action_group, false);
	    inflater.inflate(R.menu.fragment_rules_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.new_rule:
				startActivityForResult(new Intent(getActivity(), RuleOperationActivity.class),
						Constants.RULE_ADD_REQUEST);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	/**
	 * Callback method to be invoked when an item in this AdapterView has
	 * been clicked.
	 * <p/>
	 * Implementers can call getItemAtPosition(position) if they need
	 * to access the data associated with the selected item.
	 *
	 * @param parent   The AdapterView where the click happened.
	 * @param view     The view within the AdapterView that was clicked (this
	 *                 will be a view provided by the adapter)
	 * @param position The position of the view in the adapter.
	 * @param id       The row id of the item that was clicked.
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

	}

	/**
	 * The default content for this Fragment has a TextView that is shown when
	 * the list is empty. If you would like to change the text, call this method
	 * to supply the text it should use.
	 */
	public void setEmptyText(CharSequence emptyText) {
		View emptyView = mListView.getEmptyView();

		if (emptyText instanceof TextView) {
			((TextView) emptyView).setText(emptyText);
		}
	}

	/**
	 * Callback method to be invoked when an item in this view has been
	 * clicked and held.
	 * <p/>
	 * Implementers can call getItemAtPosition(position) if they need to access
	 * the data associated with the selected item.
	 *
	 * @param parent   The AbsListView where the click happened
	 * @param view     The view within the AbsListView that was clicked
	 * @param position The position of the view in the list
	 * @param id       The row id of the item that was clicked
	 * @return true if the callback consumed the long click, false otherwise
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		Log.d(TAG, "onItemLongClick");
		longClickId = position;
		TextView idText = (TextView) view.findViewById(R.id.ruleId);
		longClickId = Long.valueOf(idText.getText().toString());
		if (mActionMode != null) {
			return false;
		}

		mActionMode = getActivity().startActionMode(mActionModeCallback);
		view.setSelected(true);
		view.setBackgroundColor(Color.rgb(0, 0xCC, 0xCC));
		lastItems.add(view);
		return true;
	}

	/**
	 * 加载规则任务类
	 * 继承AsyncTask异步任务类，当切换至本碎片时将生成一个该类的实例，在后台加载
	 * 数据库中存储的规则并显示，保证过程中不会因响应时间过长而阻塞应用。
	 */
    private class LoadRulesTask extends AsyncTask<SQLiteOpenHelper, Integer, Cursor> {

		@Override
		protected Cursor doInBackground(SQLiteOpenHelper... params) {
			// TODO 自动生成的方法存根
			SQLiteDatabase db = params[0].getReadableDatabase();

	    	String[] projection = {
					ComboEntry._ID,
					ComboEntry.COLUMN_NAME,
					ComboEntry.COLUMN_QUANTUM,
					ComboEntry.COLUMN_USED,
					ComboEntry.COLUMN_REMAIN_DERIVED,
					ComboEntry.COLUMN_PRIORITY
	    	};

			return db.query(ComboEntry.TABLE_NAME, projection, null, null, null, null, null);
		}
    	
		@Override
		protected void onPostExecute(Cursor result) {
			longClickId = 0;
			// Create an empty adapter we will use to display the loaded data.
			mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.rule_item, result,
					new String[] { ComboEntry._ID, ComboEntry.COLUMN_NAME, ComboEntry.COLUMN_QUANTUM,
							ComboEntry.COLUMN_USED, ComboEntry.COLUMN_REMAIN, ComboEntry.COLUMN_PRIORITY },
					new int[] { R.id.ruleId, R.id.ruleName, R.id.totalData,
							R.id.usedData, R.id.remainData, R.id.rulePriority }, 0);

			mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
				@Override
				public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
					if (view instanceof TextView) {
						if ((columnIndex == cursor.getColumnIndex(ComboEntry.COLUMN_QUANTUM)
								|| columnIndex == cursor.getColumnIndex(ComboEntry.COLUMN_USED)
								|| columnIndex == cursor.getColumnIndex(ComboEntry.COLUMN_REMAIN))) {
							((TextView) view).setText(Util.byteConverter(cursor.getLong(columnIndex), false, "0.##"));
						}
						if (columnIndex == cursor.getColumnIndex(ComboEntry._ID)) {
							((TextView) view).setText(String.valueOf(cursor.getLong(columnIndex)));
						}
						if (columnIndex == cursor.getColumnIndex(ComboEntry.COLUMN_NAME))
							((TextView) view).setText(cursor.getString(cursor.getColumnIndex(ComboEntry.COLUMN_NAME)));
						if (columnIndex == cursor.getColumnIndex(ComboEntry.COLUMN_PRIORITY)) {
							((TextView) view).setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(ComboEntry.COLUMN_PRIORITY))));
						}
						return true;
					}
					return false;
				}
			});
			mListView.setAdapter(mAdapter);
		}
    }

	/**
	 * 刷新规则任务类
	 * 继承AsyncTask异步任务类，当规则被修改后，在后台重新加载
	 * 数据库中存储的规则并显示，保证过程中不会因响应时间过长而阻塞应用。
	 */
	private class RefreshRulesTask extends LoadRulesTask {

		@Override
		protected void onPostExecute(Cursor result) {
			longClickId = 0;
			mAdapter.changeCursor(result);
			mAdapter.notifyDataSetChanged();
		}
	}

	private class RulesActionModeCallback implements ActionMode.Callback {

		/**
		 * Called when action mode is first created. The menu supplied will be used to
		 * generate action buttons for the action mode.
		 *
		 * @param mode ActionMode being created
		 * @param menu Menu used to populate action buttons
		 * @return true if the action mode should be created, false if entering this
		 * mode should be aborted.
		 */
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.fragment_rules_actionmode_menu, menu);
			return true;
		}

		/**
		 * Called to refresh an action mode's action menu whenever it is invalidated.
		 *
		 * @param mode ActionMode being prepared
		 * @param menu Menu used to populate action buttons
		 * @return true if the menu or action mode was updated, false otherwise.
		 */
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		/**
		 * Called to report a user click on an action button.
		 *
		 * @param mode The current ActionMode
		 * @param item The item that was clicked
		 * @return true if this callback handled the event, false if the standard MenuItem
		 * invocation should continue.
		 */
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
				case R.id.rule_edit: {
					Intent intent = new Intent(getActivity(), RuleOperationActivity.class);
					intent.putExtra(Constants.Extra.COMBO_ID, longClickId);
					Log.d(TAG, "IDofPos " + longClickId);
					startActivityForResult(intent, Constants.RULE_EDIT_REQUEST);
					mActionMode.finish();
					break;
				}
				case R.id.rule_delete: {
					//Toast.makeText(getActivity(), "Delete button pressed.", Toast.LENGTH_SHORT).show();
					dbHelper.getWritableDatabase().delete(ComboEntry.TABLE_NAME,
							ComboEntry._ID + " LIKE ?",
							new String[]{String.valueOf(longClickId)});
					Intent intent = new Intent(Constants.Intent.ACTION_RULE_DELETED);
					intent.putExtra(Constants.Extra.COMBO_ID, longClickId);
					mLocalBcMgr.sendBroadcast(intent);
					new RefreshRulesTask().execute(dbHelper);
					mActionMode.finish();
					break;
				}
				default:
					break;
			}
			return false;
		}

		/**
		 * Called when an action mode is about to be exited and destroyed.
		 *
		 * @param mode The current ActionMode being destroyed
		 */
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			for (View v : lastItems) {
				v.setSelected(false);
				v.setBackgroundColor(Color.TRANSPARENT);
			}
			lastItems.clear();
			mActionMode = null;
		}
	}
}
