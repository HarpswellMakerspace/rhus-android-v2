package org.calflora.observer.service;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.SpiceService;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

// TODO Unused so far.

public class UploaderService extends Service {

	// TODO will handle queueing upload requests in batches.

	private SpiceManager spiceManager = new SpiceManager(SpiceService.class);

	@Override
	public void onCreate() {
		super.onCreate();
		spiceManager.start(this);
	}

	@Override
	public void onDestroy() {
		spiceManager.shouldStop();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
