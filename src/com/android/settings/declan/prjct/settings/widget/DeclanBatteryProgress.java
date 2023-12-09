package declan.prjct.settings.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import com.android.settings.R;

public class DeclanBatteryProgress extends RelativeLayout {
	
	private ProgressBar batteryProgress;
	private int batteryLevel;
	
	private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
			setupBatteryProgress();
		}
	};
	
	public DeclanBatteryProgress(Context context, AttributeSet attrs) {
		super(context, attrs);
		context.registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		batteryProgress = findViewById(R.id.battery_progressbar);
	}
	
	private void setupBatteryProgress() {
		batteryProgress.setProgress(batteryLevel);
	}
}