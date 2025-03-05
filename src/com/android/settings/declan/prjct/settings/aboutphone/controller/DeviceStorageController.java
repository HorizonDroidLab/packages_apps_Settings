package declan.prjct.settings.aboutphone.controller;

import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.text.BidiFormatter;
import android.text.format.Formatter;
import android.util.Log;
import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.deviceinfo.storage.StorageEntry;
import com.android.settingslib.utils.ThreadUtils;
import com.android.settings.R;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;

public class DeviceStorageController extends BasePreferenceController {
	
	private static final String TAG = "DeviceStorageController";
	
	private final StorageStatsManager mStorageStatsManager;
	private StorageEntry mStorageEntry;
	long mUsedBytes, mTotalBytes;
	
	public DeviceStorageController(Context context, String key) {
		super(context, key);
		mStorageStatsManager = (StorageStatsManager) context.getSystemService(StorageStatsManager.class);
		mStorageEntry = StorageEntry.getDefaultInternalStorageEntry(mContext);
	}
	
	@Override
	public int getAvailabilityStatus() {
		return AVAILABLE;
	}
	
	@Override
	protected void refreshSummary(Preference preference) {
		if (preference == null) {
			return;
		}
		
		refreshSummaryThread(preference);
	}
	
	@VisibleForTesting
	protected Future refreshSummaryThread(Preference preference) {
		return ThreadUtils.postOnBackgroundThread(() -> {
			try {
				if (mStorageEntry == null || !mStorageEntry.isMounted()) {
					throw new IOException();
				}
				
				if (mStorageEntry.isPrivate()) {
					mTotalBytes = mStorageStatsManager.getTotalBytes(mStorageEntry.getFsUuid());
					mUsedBytes = mTotalBytes - mStorageStatsManager.getFreeBytes(mStorageEntry.getFsUuid());
					} else {
					final File rootFile = mStorageEntry.getPath();
					if (rootFile == null) {
						Log.d(TAG, "Mounted public storage has null root path: " + mStorageEntry);
						throw new IOException();
					}
					mTotalBytes = rootFile.getTotalSpace();
					mUsedBytes = mTotalBytes - rootFile.getFreeSpace();
				}
				} catch (IOException e) {
				mTotalBytes = 0;
				mUsedBytes = 0;
			}
			
			ThreadUtils.postOnMainThread(() -> {
				String usedSummary = getStorageSummary(R.string.declan_storage_usage_summary, mUsedBytes);
				String totalSummary = getStorageSummary(R.string.declan_storage_total_summary, mTotalBytes);
				preference.setSummary(String.format("%s / %s", new Object[]{usedSummary, totalSummary}));
			});
		});
	}
	
	private String getStorageSummary(int resId, long bytes) {
		final Formatter.BytesResult result = Formatter.formatBytes(mContext.getResources(), bytes, Formatter.FLAG_SHORTER);
		return mContext.getString(resId, result.value, result.units);
	}
	
	private final String bidiFormatter(long bytes) {
		return BidiFormatter.getInstance().unicodeWrap(Formatter.formatShortFileSize(mContext, bytes));
	}
	
}