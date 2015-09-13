package fzn.projects.networkstatistics.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.TrafficStats;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * 
 * 应用内容类 AppContent
 * 获取系统内已安装应用并建立列表，内容为应用项类对象及其在表内位置。
 * 
 * 应用项内部类 AppItem
 * 记录单个应用的图标、名称、UID、上传流量、下载流量。
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class AppContent {

	/**
	 * An array of sample (dummy) items.
	 */
	@android.support.annotation.NonNull
	public List<Map<String, Object>> appsList = new ArrayList<>();

	/**
	 * A map of sample (dummy) items, by ID.
	 */
	//public SparseArray<AppItem> itemMap = new SparseArray<AppItem>();
	
	public final PackageManager pm;

	public AppContent(@android.support.annotation.NonNull Context context) {
		pm = context.getPackageManager();
	}

	private void addItem(int order, @android.support.annotation.NonNull AppItem item) {
		//itemMap.put(order, item);
		Map<String, Object> itemElem = new HashMap<>();
		itemElem.put("uid", item.uid);
		itemElem.put("label", item.label);
		itemElem.put("icon", item.icon);
		itemElem.put("tx", item.strTx);
		itemElem.put("rx", item.strRx);
		appsList.add(itemElem);
	}

	/**
	 * A dummy item representing a piece of content.
	 */
	public class AppItem {
		public final int uid;
		@android.support.annotation.NonNull
		public final String label;
		public final Drawable icon;
		@android.support.annotation.NonNull
		public final String strTx, strRx;

		public AppItem(@android.support.annotation.NonNull ApplicationInfo appInfo) {
			this.uid = appInfo.uid;
			this.label = appInfo.loadLabel(pm).toString();
			this.icon = appInfo.loadIcon(pm);
			if (TrafficStats.getUidTxBytes(uid) > 1048576) strTx = TrafficStats.getUidTxBytes(uid) / 1048576 + "MB";
	        else if (TrafficStats.getUidTxBytes(uid) > 1024) strTx = TrafficStats.getUidTxBytes(uid) / 1024 + "KB";
	        else strTx = TrafficStats.getUidTxBytes(uid) + "B";
			if (TrafficStats.getUidRxBytes(uid) > 1048576) strRx = TrafficStats.getUidRxBytes(uid) / 1048576 + "MB";
	        else if (TrafficStats.getUidRxBytes(uid) > 1024) strRx = TrafficStats.getUidRxBytes(uid) / 1024 + "KB";
	        else strRx = TrafficStats.getUidRxBytes(uid) + "B";
		}

		@android.support.annotation.NonNull
		@Override
		public String toString() {
			return label;
		}
	}
	
	/**
	 * 加载应用列表，便于异步任务实现。
	 * @return 应用列表
	 */
	@android.support.annotation.NonNull
	public List<Map<String, Object>> loadItem() {
		int count = 0;
		for (ApplicationInfo appInfo : pm.getInstalledApplications(0)) {
			addItem(count++, new AppItem(appInfo));
		}
		return appsList;
	}
}
