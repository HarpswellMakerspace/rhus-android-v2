package org.calflora.observer;


import net.winterroot.android.wildflowers.R;

import com.octo.android.robospice.JacksonSpringAndroidSpiceService;
import com.octo.android.robospice.SpiceManager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

abstract public class ApiActivity extends BaseActivity {

	protected static final String JSON_CACHE_KEY = "CACHE_KEY";
	protected SpiceManager spiceManager = new SpiceManager( JacksonSpringAndroidSpiceService.class );
	  
	private View mUserView;
	private View mStatusView;
	protected TextView mStatusMessageView;
	  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		

	}

	@Override
	  protected void onStart() {
	      super.onStart();
	      spiceManager.start( this );

	      ViewGroup layout = (ViewGroup) findViewById(android.R.id.content);
	      mUserView = layout.getChildAt(0);

	      mStatusView = getLayoutInflater().inflate(R.layout.api_progress, null);
	      mStatusMessageView = (TextView) mStatusView.findViewById(R.id.progress_status_message);

	      layout.addView(mStatusView);
	  }

	  @Override
	  protected void onStop() {
	      spiceManager.shouldStop();
	      super.onStop();
	  }


	  protected void showProgress(final boolean show) {
			// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
			// for very easy animations. If available, use these APIs to fade-in
			// the progress spinner.
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
				int shortAnimTime = getResources().getInteger(
						android.R.integer.config_shortAnimTime);

				mStatusView.setVisibility(View.VISIBLE);
				mStatusView.animate().setDuration(shortAnimTime)
						.alpha(show ? 1 : 0)
						.setListener(new AnimatorListenerAdapter() {
							@Override
							public void onAnimationEnd(Animator animation) {
								mStatusView.setVisibility(show ? View.VISIBLE
										: View.GONE);
							}
						});

				mUserView.setVisibility(View.VISIBLE);
				mUserView.animate().setDuration(shortAnimTime)
						.alpha(show ? 0 : 1)
						.setListener(new AnimatorListenerAdapter() {
							@Override
							public void onAnimationEnd(Animator animation) {
								mUserView.setVisibility(show ? View.GONE
										: View.VISIBLE);
							}
						});
			} else {
				// The ViewPropertyAnimator APIs are not available, so simply show
				// and hide the relevant UI components.
				mStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
				mUserView.setVisibility(show ? View.GONE : View.VISIBLE);
			}
		}
}
