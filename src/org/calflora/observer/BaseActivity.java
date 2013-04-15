package org.calflora.observer;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;

public class BaseActivity extends Activity {

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.menu_help:
	            showHelp();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private void showHelp(){

		Intent intent = new Intent("org.calflora.observer.action.HELP");
		startActivity(intent);	

	}
}
