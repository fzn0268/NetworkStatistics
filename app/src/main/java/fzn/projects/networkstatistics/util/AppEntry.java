package fzn.projects.networkstatistics.util;

import java.io.File;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;


/**
 * 应用项类
 * 为加载器{@link AppListLoader}中的每一项创建条目。
 * This class holds the per-item data in our Loader.
 */
public class AppEntry {

	private final AppListLoader mLoader;
    @android.support.annotation.NonNull
    private final ApplicationInfo mInfo;
    @android.support.annotation.NonNull
    private final File mApkFile;
    private String mLabel;
    private Drawable mIcon;
    private boolean mMounted;


    public AppEntry(AppListLoader loader, @android.support.annotation.NonNull ApplicationInfo info) {
        // TODO 自动生成的构造函数存根
        mLoader = loader;
        mInfo = info;
        mApkFile = new File(info.sourceDir);
	}

    @android.support.annotation.NonNull
    public ApplicationInfo getApplicationInfo() {
        return mInfo;
    }

	public String getLabel() {
        return mLabel;
    }

    @android.support.annotation.Nullable
    public Drawable getIcon() {
        if (mIcon == null) {
            if (mApkFile.exists()) {
                mIcon = mInfo.loadIcon(mLoader.mPm);
                return mIcon;
            } else {
                mMounted = false;
            }
        } else if (!mMounted) {
            // If the app wasn't mounted but is now mounted, reload
            // its icon.
            if (mApkFile.exists()) {
                mMounted = true;
                mIcon = mInfo.loadIcon(mLoader.mPm);
                return mIcon;
            }
        } else {
            return mIcon;
        }

        return mLoader.getContext().getResources().getDrawable(android.R.drawable.sym_def_app_icon);
    }

	@Override public String toString() {
        return mLabel;
    }

    void loadLabel(@android.support.annotation.NonNull Context context) {
        if (mLabel == null || !mMounted) {
            if (!mApkFile.exists()) {
                mMounted = false;
                mLabel = mInfo.packageName;
            } else {
                mMounted = true;
                CharSequence label = mInfo.loadLabel(context.getPackageManager());
                mLabel = label != null ? label.toString() : mInfo.packageName;
            }
        }
    }

}
