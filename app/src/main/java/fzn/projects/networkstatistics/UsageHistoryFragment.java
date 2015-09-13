package fzn.projects.networkstatistics;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import fzn.projects.networkstatistics.util.AppContent;

/**
 * 应用数据用量碎片
 * 列出各应用自开机时接收和发送的流量
 *
 * A fragment representing a list of Items.
 * <p />
 * Large screen devices (such as tablets) are supported by replacing the
 * ListView with a GridView.
 * <p />
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 *
 * TODO: List all applications' Tx and Rx in a month.
 */
public class UsageHistoryFragment extends Fragment implements
		AbsListView.OnItemClickListener {
	protected static final String TAG = "UsageHistoryFragment";

	/**
     * The fragment argument representing the section number for this
     * fragment.
     */
	private static final String ARG_SECTION_NUMBER = "section_number";

	/**
	 * The fragment's ListView/GridView.
	 */
	private AbsListView mListView;

	// TODO: Rename and change types of parameters
	@android.support.annotation.NonNull
	public static UsageHistoryFragment newInstance(int sectionNumber) {
		UsageHistoryFragment fragment = new UsageHistoryFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public UsageHistoryFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO: Change Adapter to display your content
	}

	@Override
	public View onCreateView(@android.support.annotation.NonNull LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_appdatausage, container,
				false);
		mListView = (AbsListView) view.findViewById(R.id.history_list);
		new LoadAppsTask().execute(this);
		// Set OnItemClickListener so we can be notified on item clicks
		if (mListView != null) {
			mListView.setEmptyView(getActivity().findViewById(R.id.history_empty));
			setEmptyText(getResources().getText(R.string.empty_list));
			mListView.setOnItemClickListener(this);
		}
		return view;
	}

	@Override
	public void onAttach(@android.support.annotation.NonNull Activity activity) {
		super.onAttach(activity);
		((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
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
	 * 异步加载应用
	 * 在后台加载各应用的图标、名称、已接收和发送的流量
	 */
	private class LoadAppsTask extends AsyncTask<Object, Integer, List<Map<String, Object>>> {

		@android.support.annotation.NonNull
		@Override
		protected List<Map<String, Object>> doInBackground(Object... params) {
			AppContent appInfos = new AppContent(getActivity());
			return appInfos.loadItem();
		}
		
		@Override
		protected void onPostExecute(List<Map<String, Object>> result) {
			
			// Set the adapter
			/*
	  The Adapter which will be used to populate the ListView/GridView with
	  Views.
	 */
			SimpleAdapter mAdapter = new SimpleAdapter(getActivity(), result, R.layout.app_item,
					new String[]{"icon", "label", "rx", "tx"},
					new int[]{R.id.app_icon, R.id.app_name, R.id.app_rx, R.id.app_tx});
			mAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {

				@Override
				public boolean setViewValue(View view, Object data,
											String textRepresentation) {
					if (view instanceof ImageView && data instanceof Drawable) {
						((ImageView) view).setImageDrawable((Drawable) data);
						return true;
					}
					return false;
				}

			});
			mListView.setAdapter(mAdapter);
		}
	}
}
