package org.calflora.observer;

import net.winterroot.android.wildflowers.R;
import android.os.Bundle;
import com.actionbarsherlock.view.*;

public class DashboardActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dashboard);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.dashboard, (Menu) menu);
		return true;
	}

}
