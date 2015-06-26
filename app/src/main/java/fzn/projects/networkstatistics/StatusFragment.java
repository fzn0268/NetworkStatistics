package fzn.projects.networkstatistics;

import android.app.Activity;
import android.app.Fragment;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ValueFormatter;

import java.text.DecimalFormat;
import java.util.ArrayList;

import fzn.projects.networkstatistics.util.Util;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */

/**
 * 状态碎片类
 * 展示设备当前的网络状态（待实现）
 */
public class StatusFragment extends Fragment {
    public static final String TAG = "StatusFragment";

	/**
     * The fragment argument representing the section number for this
     * fragment.
     */
	private static final String ARG_SECTION_NUMBER = "section_number";
    private int sectionNumber;

    private LocalBroadcastManager localBcMgr;
    private BroadcastReceiver speedBcRecvr;
    private TextView speedText;
    private LineChart speedChart;
    private DecimalFormat mDF = new DecimalFormat();
    private ArrayList<Entry> speedList;

    private Binder binder;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (Binder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            binder = null;
        }
    };

    private OnFragmentInteractionListener mListener;
    
	/**
     * Returns a new instance of this fragment for the given section
     * number.
     */
	public static StatusFragment newInstance(int sectionNumber) {
		StatusFragment fragment = new StatusFragment();
		Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
	}
	
	public StatusFragment() {
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
        }
    }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_status, container, false);
        speedText = (TextView) rootView.findViewById(R.id.speedText);
        speedChart = (LineChart) rootView.findViewById(R.id.speedChart);
        setSpeedChart();
        return rootView;
    }

    private void setSpeedChart() {
        XAxis xAxis = speedChart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelsToSkip(4);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.WHITE);
        YAxis yAxis = speedChart.getAxisLeft();
        yAxis.setEnabled(true);
        yAxis.setStartAtZero(true);
        yAxis.setTextColor(Color.WHITE);
        yAxis.setTextSize(10f);
        speedChart.setDescription("");
        speedChart.getAxisRight().setEnabled(false);
        speedChart.setHardwareAccelerationEnabled(true);
        speedChart.getLegend().setEnabled(false);
        speedChart.animateXY(1000, 1000);

        String[] secondList = new String[60];
        for (int i = 0; i < 60; i++) {
            secondList[i] = (i + "");
        }
        speedList = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            speedList.add(new Entry(0f, i));
        }
        LineDataSet speedSet = new LineDataSet(speedList, "Speed");
        speedSet.setDrawCubic(true);
        speedSet.setDrawFilled(true);
        speedSet.setDrawCircles(false);
        speedSet.setLineWidth(2f);
        speedSet.setCubicIntensity(0.2f);
        speedSet.setColor(Color.rgb(104, 241, 175));
        speedSet.setFillColor(ColorTemplate.getHoloBlue());
        LineData speedData = new LineData(secondList, speedSet);
        speedData.setValueTextSize(12f);
        speedData.setDrawValues(false);
        speedChart.setData(speedData);
        speedChart.notifyDataSetChanged();
    }

    /**
     * Called when the fragment is no longer attached to its activity.  This
     * is called after {@link #onDestroy()}.
     */
    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach");
        super.onDetach();
        getActivity().unbindService(conn);
        localBcMgr.unregisterReceiver(speedBcRecvr);
        speedBcRecvr = null;
        mListener = null;
    }

    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach");
        super.onAttach(activity);
        final Intent intent = new Intent(getActivity(), NetworkStatisticsService.class);
        localBcMgr = LocalBroadcastManager.getInstance(getActivity());
        speedBcRecvr = new SpeedBroadcastReceiver();
        localBcMgr.registerReceiver(speedBcRecvr, new IntentFilter(Constants.Intent.ACTION_NET_INFO));
        getActivity().bindService(intent, conn, Service.BIND_AUTO_CREATE);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(int buttonId);
    }

    private class SpeedBroadcastReceiver extends BroadcastReceiver {

        /**
         * This method is called when the BroadcastReceiver is receiving an Intent
         * broadcast.  During this time you can use the other methods on
         * BroadcastReceiver to view/modify the current result values.  This method
         * is always called within the main thread of its process, unless you
         * explicitly asked for it to be scheduled on a different thread using
         * {@link Context#registerReceiver(BroadcastReceiver,
         * IntentFilter, String, Handler)}. When it runs on the main
         * thread you should
         * never perform long-running operations in it (there is a timeout of
         * 10 seconds that the system allows before considering the receiver to
         * be blocked and a candidate to be killed). You cannot launch a popup dialog
         * in your implementation of onReceive().
         * <p/>
         * <p><b>If this BroadcastReceiver was launched through a &lt;receiver&gt; tag,
         * then the object is no longer alive after returning from this
         * function.</b>  This means you should not perform any operations that
         * return a result to you asynchronously -- in particular, for interacting
         * with services, you should use
         * {@link Context#startService(Intent)} instead of
         * {@link Context#bindService(Intent, ServiceConnection, int)}.  If you wish
         * to interact with a service that is already running, you can use
         * {@link #peekService}.
         * <p/>
         * <p>The Intent filters used in {@link Context#registerReceiver}
         * and in application manifests are <em>not</em> guaranteed to be exclusive. They
         * are hints to the operating system about how to find suitable recipients. It is
         * possible for senders to force delivery to specific recipients, bypassing filter
         * resolution.  For this reason, {@link #onReceive(Context, Intent) onReceive()}
         * implementations should respond only to known actions, ignoring any unexpected
         * Intents that they may receive.
         *
         * @param context The Context in which the receiver is running.
         * @param intent  The Intent being received.
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            long speed = intent.getLongExtra(Constants.Extra.SPEED, -1);
            float[] fSpeed = speedConverter(speed);
            float maxSpeedInList = 0;
            speedText.setText(Util.byteConverter(speed, true, "0.##"));

            speedList.remove(0);
            for (int i = 0; i < speedList.size(); i++) {
                speedList.get(i).setXIndex(i);
                maxSpeedInList = maxSpeedInList < speedList.get(i).getVal() ? speedList.get(i).getVal() : maxSpeedInList;
            }
            speedList.add(new Entry(fSpeed[0], 59));
            maxSpeedInList = maxSpeedInList < fSpeed[0] ? fSpeed[0] : maxSpeedInList;
            speedChart.getAxisLeft().setAxisMaxValue(maxSpeedInList + 0.001f);
            speedChart.setDescription(fSpeed[1] == 1 ? "KB/s" : "MB/s");
            speedChart.notifyDataSetChanged();
            speedChart.invalidate();
        }
    }

    private float[] speedConverter(long bytes) {
        if (bytes > 0x40000000) // GB
            return new float[] {((float) bytes / 0x100000), 2}; // MB
        else
            return new float[] {((float) bytes / 0x400), 1}; // KB
    }

}
