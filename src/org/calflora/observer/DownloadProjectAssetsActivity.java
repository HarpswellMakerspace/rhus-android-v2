package org.calflora.observer;

import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class DownloadProjectAssetsActivity extends BaseActivity {

	private Handler mHandler = new Handler(); 
	private ProgressBar tileCacheProgressBar;
	private ProgressBar plantListProgressBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download_project_assets);
		
		tileCacheProgressBar = (ProgressBar) findViewById(R.id.tile_cache_download);
		plantListProgressBar = (ProgressBar) findViewById(R.id.plants_list_download);

		if(Observer.instance.getOrganization() != null){
			ImageView imageView = (ImageView) findViewById(R.id.org_image_view);
			imageView.setImageResource(R.drawable.yosemite_conservancy_splash );

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.download_project_assets, menu);
		return true;
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		mHandler.postDelayed(new Runnable() { 
			public void run() { 
				int total = 10;
				int currentPosition = 0;
				while (currentPosition<total) {

					try {
						Thread.sleep(300);
						currentPosition++;
					} catch (InterruptedException e) {
						//return;
					} catch (Exception e) {
						//return;
					}         
					final int progress = (currentPosition * 100 / total );
			
					runOnUiThread(new Runnable() {
					    @Override
					    public void run() {
							tileCacheProgressBar.setProgress( progress );
							plantListProgressBar.setProgress( progress );
					    }
					});
			
					
				}    
				Intent intent = new Intent("org.calflora.observer.action.WORKSPACE");
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
	        } 
	    },2000);
	}

	
	
	
}
