package declan.prjct.settings.widget;

import android.content.Context;
import android.os.storage.StorageManager;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.android.settingslib.deviceinfo.PrivateStorageInfo;
import com.android.settingslib.deviceinfo.StorageManagerVolumeProvider;

import com.android.settings.R;

public class DeclanStorageProgress extends RelativeLayout {
	
	private ProgressBar mStorageProgress;
	
	public DeclanStorageProgress(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mStorageProgress = findViewById(R.id.storage_progress);
		setupStorageProgress();
	}
	
	private void setupStorageProgress() {
		PrivateStorageInfo storageInfo = PrivateStorageInfo.getPrivateStorageInfo(new StorageManagerVolumeProvider((StorageManager) getContext().getSystemService(StorageManager.class)));
		long totalBytes = storageInfo.totalBytes;
		mStorageProgress.setProgress((int) ((((double) (totalBytes - storageInfo.freeBytes)) / ((double) totalBytes)) * 100.0d));
	}
}