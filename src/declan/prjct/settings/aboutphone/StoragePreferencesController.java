package declan.prjct.settings.aboutphone;

import android.content.Context;
import android.os.storage.StorageManager;
import android.text.BidiFormatter;
import android.text.format.Formatter;
import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.deviceinfo.PrivateStorageInfo;
import com.android.settingslib.deviceinfo.StorageManagerVolumeProvider;
import com.android.settingslib.utils.ThreadUtils;
import declan.prjct.settings.aboutphone.StoragePreference;
import java.util.concurrent.Future;

public class StoragePreferencesController extends BasePreferenceController {
	
	private StoragePreference mPreference;
	private final StorageManager mStorageManager;
	private final StorageManagerVolumeProvider mStorageManagerVolumeProvider;
	
	public StoragePreferencesController(Context context, String key) {
		super(context, key);
		mStorageManager = mContext.getSystemService(StorageManager.class);
		mStorageManagerVolumeProvider = new StorageManagerVolumeProvider(mStorageManager);
	}
	
	@Override
	public int getAvailabilityStatus() {
	    return AVAILABLE;
	}
	
	@Override
	public void displayPreference(PreferenceScreen screen) {
		super.displayPreference(screen);
		mPreference = screen.findPreference(getPreferenceKey());
		updateState(mPreference);
	}
	
	@Override
	public void updateState(Preference preference) {
		if (preference != null) {
			final PrivateStorageInfo info = PrivateStorageInfo.getPrivateStorageInfo(getStorageManagerVolumeProvider());
			final long totalBytes = info.totalBytes;
			
			BidiFormatter instance = BidiFormatter.getInstance();
			String totalSummary = instance.unicodeWrap(Formatter.formatShortFileSize(mContext, totalBytes));
			String usedSummary = Formatter.formatShortFileSize(mContext, info.totalBytes - info.freeBytes);
			mPreference.setProgress((int) ((((double) (totalBytes - info.freeBytes)) / ((double) totalBytes)) * 100.0d));
			mPreference.setSummary(usedSummary + " / " + totalSummary);
		}
	}
	
	@Override
	protected void refreshSummary(Preference preference) {
		if (preference != null) {
			return;
		}
		
		refreshSummaryThread(preference);
	}
	
	@VisibleForTesting
	protected Future refreshSummaryThread(Preference preference) {
		return ThreadUtils.postOnBackgroundThread(() -> {
			ThreadUtils.postOnMainThread(() -> {
				updateState(preference);
			});
		});
	}
	
	@VisibleForTesting
	protected StorageManagerVolumeProvider getStorageManagerVolumeProvider() {
		return mStorageManagerVolumeProvider;
	}
	
}