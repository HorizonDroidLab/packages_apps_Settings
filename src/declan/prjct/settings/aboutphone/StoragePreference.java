package declan.prjct.settings.aboutphone;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

public class StoragePreference extends Preference {
	
	private int mProgressBar = -1;
	
	public StoragePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public final void setProgress(int progressBar) {
		mProgressBar = progressBar;
		notifyChanged();
	}
	
	@Override
	public void onBindViewHolder(PreferenceViewHolder holder) {
		super.onBindViewHolder(holder);
		View progressView = holder.findViewById(android.R.id.progress);
		if (progressView != null) {
			ProgressBar progressBar = (ProgressBar) progressView;
			if (mProgressBar < 0) {
				progressBar.setIndeterminate(true);
				return;
			}
			progressBar.setIndeterminate(false);
			progressBar.setProgress(mProgressBar);
			return;
		}
	}
}