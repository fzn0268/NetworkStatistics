package fzn.projects.networkstatistics;

import android.app.Activity;
import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;

/**
 * 主活动
 */
public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        StatusFragment.OnFragmentInteractionListener,
        OverviewFragment.OnFragmentInteractionListener {
    protected static final String TAG = "MainActivity";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private String[] mDrawerTitleItems, mStatusTitleItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!isServiceRunning(NetworkStatisticsService.class))
            startService(new Intent(this, NetworkStatisticsService.class));

        setContentView(R.layout.activity_main);

        mDrawerTitleItems = getResources().getStringArray(R.array.drawerTitle);
        mStatusTitleItems = getResources().getStringArray(R.array.statusTabTitle);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        switch (position) {
        case 0:
        	fragmentManager.beginTransaction()
    			.replace(R.id.container, TabbedFragment.newInstance(position + 1))
    			.commit();
        	break;
        case 1:
        	fragmentManager.beginTransaction()
        		.replace(R.id.container, RulesFragment.newInstance(position + 1))
        		.commit();
        	break;
        case 2:
        	fragmentManager.beginTransaction()
    			.replace(R.id.container, UsageHistoryFragment.newInstance(position + 1))
    			.commit();
        	break;
        default:
        	fragmentManager.beginTransaction()
            	.replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
            	.commit();
        	break;
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = mDrawerTitleItems[0];
                break;
            case 2:
                mTitle = mDrawerTitleItems[1];
                break;
            case 3:
                mTitle = mDrawerTitleItems[2];
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@android.support.annotation.NonNull MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

	@Override
	public void onFragmentInteraction(int buttonId) {
		// TODO 自动生成的方法存根
		switch (buttonId) {
            default:
                break;
        }
	}

    private boolean isServiceRunning(@android.support.annotation.NonNull Class<?> serviceClass) {
        ActivityManager activityManager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo :
                activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(serviceInfo.service.getClassName()))
                return true;
        }
        return false;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @android.support.annotation.NonNull
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(@android.support.annotation.NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(@android.support.annotation.NonNull Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }
}
