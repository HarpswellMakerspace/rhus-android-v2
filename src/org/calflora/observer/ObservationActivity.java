package org.calflora.observer;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.calflora.observer.model.Observation;
import org.calflora.observer.model.Plant;
import org.calflora.observer.model.Project;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryPrompt;
import org.json.JSONException;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.activities.FormHierarchyActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.FormLoaderListener;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.tasks.FormLoaderTask;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.views.ODKView;
import org.odk.collect.android.widgets.QuestionWidget;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ObservationActivity extends Activity implements
		ActionBar.TabListener, FormLoaderListener {
	
	private static final int SELECT_PLANT = 1001;
	
	private ActionBar mActionBar;
	private ObservationSummaryFragment observationSummaryFragment;
	private ObservationAssessmentFragment observationAssessmentFragment;
	private ObservationTreatmentFragment observationTreatmentFragment;

	private ImageView plantThumbnailView;

	private String mFormPath = "";
	private FormController mFormController = null;
	
	protected void done(){
		finish();

		/*
		Intent intent = new Intent("org.calflora.observer.action.WORKSPACE");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		 */
	}
	

	private FormLoaderTask mFormLoaderTask = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_observation);
		
		String instancePath = null;
		mFormPath = Environment.getExternalStorageDirectory().toString() + "/Forms/OAT.xml";
        mFormLoaderTask = new FormLoaderTask(instancePath, null, null);
        mFormLoaderTask.setFormLoaderListener(this);
        mFormLoaderTask.execute(mFormPath);
		
		observationSummaryFragment = new ObservationSummaryFragment();
		observationAssessmentFragment = new ObservationAssessmentFragment();
		observationTreatmentFragment = new ObservationTreatmentFragment();
		
		plantThumbnailView = (ImageView) findViewById(R.id.plant_thumbnail);
	
		// Set up the action bar.
		mActionBar = getActionBar();
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		/*
		actionBar.addTab(actionBar.newTab()
				.setText("Observation")
				.setTabListener(this)
				);
		actionBar.addTab(actionBar.newTab()
				.setText("Assessment")
				.setTabListener(this)
				);
		actionBar.addTab(actionBar.newTab()
				.setText("Treatment")
				.setTabListener(this)
				);
		*/
		
		//actionBar.setHomeButtonEnabled(true);
		
		View homeIcon = findViewById(android.R.id.home);
		((View) homeIcon.getParent()).setVisibility(View.GONE);
		
		TextView titleView = (TextView) findViewById(R.id.actionSheetTitleView);
		titleView.setText("New Observation");
		
		ImageButton cancelButton = (ImageButton) findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(
        		new OnClickListener(){
        			public void onClick(View v){
        				onCancel();
        			}
        		}
        		);
		

		
		ImageButton doneButton = (ImageButton) findViewById(R.id.doneButton);
		doneButton.setOnClickListener(
        		new OnClickListener(){
        			
        			Boolean storingObservation = false;
        		    Object lock = new Object();

        			public void onClick(View v){
	
        				synchronized (lock) {
        		
        					if(storingObservation){
            					return;
            				}
            				storingObservation = true;
            				          
        				}
        				
        				//Read data from fragments and store.
        				/*
        				 "locdesc":"between the big rock and the oak tree",
        				  "Habitat":"riparian",
        				  "Notes":"seems to be spreading downhill",
        				*/
        				
        				
        				
        				try {
        					Observer.currentObservation.storeObservation();
        				} catch (JSONException e1) {
        					Observer.toast("JSON Failed", getApplicationContext());
        					e1.printStackTrace();
        					return;
        				}

        				done();

        			}
        		}
        		);

		
		// TODO: combine into constructor..
		Observer.currentObservation = new Observation();
		Location lastLocation = Observer.getInstance().getLastLocation();
		Observer.currentObservation.latitude = lastLocation.getLatitude();
		Observer.currentObservation.longitude = lastLocation.getLongitude();	
		
		Button changePlantButton = (Button) findViewById(R.id.plant_change_button);
		changePlantButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent("org.calflora.observer.action.PLANTSELECTOR");
				startActivityForResult(intent, SELECT_PLANT);				
			}
			
		
		});
	}



	private void loadPlant(String taxon) {
		Plant plant = Project.getPlant(taxon);
		Observer.currentObservation.plant = plant;

		TextView commonName = (TextView)findViewById(R.id.common_name);
		TextView taxonName = (TextView)findViewById(R.id.taxon);
		
		if(! taxon.equals("unknown")){
			commonName.setText(plant.getCommon() );
			taxonName.setText(plant.getTaxon() );
			AssetManager assets = getBaseContext().getResources().getAssets();
			AssetFileDescriptor asset = null;
			try {
				String imagePath = "plant_images/" + plant.getPhotoid().replace("'","")+".jpeg";
				asset = assets.openFd(imagePath);
				Drawable plantThumbnail = Drawable.createFromStream(asset.createInputStream(), "");
				plantThumbnailView.setImageDrawable(plantThumbnail);
			} catch (IOException e) {
				// TODO Show default image for plant
			}
		} else {
			//Placehold for unknown plant icon
			plantThumbnailView.setImageResource(R.drawable.calflora_observer_icon);
			commonName.setText("Unknown Taxon");
			taxonName.setText("");

		}
		
		
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.observation, menu);
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction transaction;
		
		// Replace whatever is in the fragment_container view with this fragment,
		// and add the transaction to the back stack
		switch (tab.getPosition()){
		case 0:
		    transaction = fragmentManager.beginTransaction();
			//selectedTab = Tabs.SUMMARY;
			transaction.replace(R.id.observation_fragment_container,observationSummaryFragment );
			transaction.commit();
			break;
			
		case 1:
			// Assessment, or ODK configured 2nd tab
		    transaction = fragmentManager.beginTransaction();
				//selectedTab = Tabs.SUMMARY;
				transaction.replace(R.id.observation_fragment_container,observationAssessmentFragment );
				transaction.commit();
			break;
			
		case 2:
			// Treatment, or ODK configured 3rd tab
		    transaction = fragmentManager.beginTransaction();
				//selectedTab = Tabs.SUMMARY;
				transaction.replace(R.id.observation_fragment_container,observationTreatmentFragment );
				transaction.commit();
			break;
			
	
		}
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		if(resultCode == Activity.RESULT_CANCELED){
			return;
			
		} else if(!(resultCode == Activity.RESULT_OK)){
			Observer.toast("Error getting plant: result code is not OK", this);
			return;
		}
		
		
		try
		{
			switch (requestCode) {
			case SELECT_PLANT: //SELECT_PLANT
				if (resultCode == Activity.RESULT_OK)
				{
					Bundle data =  intent.getExtras();
					if(data == null){
						Observer.toast("Error getting plant:", this);
						return;
					}
					
					String taxon = data.getString(Observer.NEW_PLANT_TAXON);
					loadPlant(taxon);
			
					
				}
				break;
			}
		}
		catch (Throwable ex)
		{
			ex.printStackTrace();
			Observer.toast("trouble choosing plant " + ex, this);
		}
	}
	
	@Override
	public void onBackPressed() {
		onCancel();
		return;
	}
	
	protected void onCancel(){
		@SuppressWarnings("unused")
		AlertDialog ad = new AlertDialog.Builder(this).setMessage(
				"Clicking OK will discard all your data for this entry.").setTitle(
						"Are you sure?").setCancelable(false)
						.setPositiveButton(android.R.string.ok,
								new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// User selects OK, cancel this observation
								done();
							}
						}).setNeutralButton(android.R.string.cancel,
								new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// User selects Cancel, do nothing
							}
						}).show();	    
		return;
	}
	
	
    /**
     * loadingComplete() is called by FormLoaderTask once it has finished loading a form.
     */
    public void loadingComplete(FormLoaderTask task) {
        //dismissDialog(PROGRESS_DIALOG);

        FormController formController = task.getFormController();
        boolean pendingActivityResult = task.hasPendingActivityResult();
        boolean hasUsedSavepoint = task.hasUsedSavepoint();
        int requestCode = task.getRequestCode(); // these are bogus if
                                                 // pendingActivityResult is
                                                 // false
        int resultCode = task.getResultCode();
        Intent intent = task.getIntent();

        mFormLoaderTask.setFormLoaderListener(null);
        FormLoaderTask t = mFormLoaderTask;
        mFormLoaderTask = null;
        t.cancel(true);
        t.destroy();
        //Collect.getInstance().setFormController(formController);
        mFormController = formController;
        
        // Set the language if one has already been set in the past
        String[] languageTest = formController.getLanguages();
        if (languageTest != null) {
            String defaultLanguage = formController.getLanguage();
            String newLanguage = "";
            String selection = FormsColumns.FORM_FILE_PATH + "=?";
            String selectArgs[] = {
                    mFormPath
            };
            Cursor c = null;
            try {
                c = getContentResolver().query(FormsColumns.CONTENT_URI, null,
                        selection, selectArgs, null);
                if (c.getCount() == 1) {
                    c.moveToFirst();
                    newLanguage = c.getString(c
                            .getColumnIndex(FormsColumns.LANGUAGE));
                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }

            // if somehow we end up with a bad language, set it to the default
            try {
                formController.setLanguage(newLanguage);
            } catch (Exception e) {
                formController.setLanguage(defaultLanguage);
            }
        }

        /*
        if (pendingActivityResult) {
            // set the current view to whatever group we were at...
            refreshCurrentView();
            // process the pending activity request...
            onActivityResult(requestCode, resultCode, intent);
            return;
        }
        */

        // it can be a normal flow for a pending activity result to restore from a savepoint
        // (the call flow handled by the above if statement). For all other use cases, the
        // user should be notified, as it means they wandered off doing other things then
        // returned to ODK Collect and chose Edit Saved Form, but that the savepoint for that
        // form is newer than the last saved version of their form data.
        if (hasUsedSavepoint) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ObservationActivity.this,
                            getString(R.string.savepoint_used),
                            Toast.LENGTH_LONG).show();
                }
            });
        }

        // Set saved answer path
        if (formController.getInstancePath() == null) {

            // Create new answer folder.
            String time = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH).format(Calendar.getInstance()
                    .getTime());
            String file = mFormPath.substring(mFormPath.lastIndexOf('/') + 1,
                    mFormPath.lastIndexOf('.'));
            String path = Collect.INSTANCES_PATH + File.separator + file + "_" + time;
            if (FileUtils.createFolder(path)) {
                formController.setInstancePath(new File(path + File.separator + file + "_" + time
                        + ".xml"));
            }
        } else {
        	/*
        	 * This would possibly be where to handle loading an existing item for editing.
            Intent reqIntent = getIntent();
            boolean showFirst = reqIntent.getBooleanExtra("start", false);

            if (!showFirst) {
                // we've just loaded a saved form, so start in the hierarchy
                // view
                Intent i = new Intent(this, FormHierarchyActivity.class);
                startActivity(i);
                return; // so we don't show the intro screen before jumping to
                        // the hierarchy
            }
            */
        }

        /*
         * 
         *  setupView();
         *  Code below is for this function
         */
        
        boolean done = false;
        setup: for(int event=0; !done; event = formController.stepToNextScreenEvent()) {
            switch (event) {
            case FormEntryController.EVENT_BEGINNING_OF_FORM:
            	continue setup;

            case FormEntryController.EVENT_END_OF_FORM:
            	done = true;
            	break;
            case FormEntryController.EVENT_QUESTION:
            	int asdf = 0;
            	break;
            case FormEntryController.EVENT_GROUP:
  
            	// For the pages (groups) of the form

            	// return createView(event, advancingPage);  ODK Code
            	ODKView odkv = null;
            	// should only be a group here if the event_group is a field-list
            	try {
            		FormEntryPrompt[] prompts = formController.getQuestionPrompts();
            		FormEntryCaption[] groups = formController.getGroupsForCurrentIndex();
            		odkv = new ODKView(this, formController.getQuestionPrompts(),
            				groups, false);
            		/*
            Log.i(t,
                    "created view for group "
                            + (groups.length > 0 ? groups[groups.length - 1]
                                    .getLongText() : "[top]")
                            + " "
                            + (prompts.length > 0 ? prompts[0]
                                    .getQuestionText() : "[no question]"));
            		 */
            	} catch (RuntimeException e) {
            		// this is badness to avoid a crash.

            		done = true;

            		//            		createErrorDialog(e.getMessage(), false);
            		e.printStackTrace();
            		break;
            		// event = formController.stepToNextScreenEvent();
            		// return createView(event, advancingPage);
            	}

            	// Makes a "clear answer" menu pop up on long-click
            	for (QuestionWidget qw : odkv.getWidgets()) {
            		if (!qw.getPrompt().isReadOnly()) {
            			registerForContextMenu(qw);
            		}
            	}

            	//return odkv;

            	mActionBar.addTab(mActionBar.newTab()
            			.setText("Observation")
            			.setTabListener(this)
            			);
            	break;

            default :
            	createErrorDialog("Internal error: step to prompt failed", false);
            	//..Log.e(t, "Attempted to create a view that does not exist.");
            	// this is badness to avoid a crash.

            	break;

            }
        }

        // End ODK code

    }

    /**
     * called by the FormLoaderTask if something goes wrong.
     */
    @Override
    public void loadingError(String errorMsg) {
       // dismissDialog(PROGRESS_DIALOG);
        if (errorMsg != null) {
			Toast.makeText( ObservationActivity.this, errorMsg, Toast.LENGTH_LONG ).show();
            //createErrorDialog(errorMsg, EXIT);
        } else {
			Toast.makeText( ObservationActivity.this, R.string.parse_error, Toast.LENGTH_LONG ).show();
            //createErrorDialog(getString(R.string.parse_error), EXIT);
        }
    }
    
    
    /**
     * Creates and displays dialog with the given errorMsg.
     */
    private void createErrorDialog(String errorMsg, final boolean shouldExit) {
        Collect.getInstance()
                .getActivityLogger()
                .logInstanceAction(this, "createErrorDialog",
                        "show." + Boolean.toString(shouldExit));
        String mErrorMessage = errorMsg;
        AlertDialog mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
        mAlertDialog.setTitle(getString(R.string.error_occured));
        mAlertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON1:
                        Collect.getInstance().getActivityLogger()
                                .logInstanceAction(this, "createErrorDialog", "OK");
                        if (shouldExit) {
                            finish();
                        }
                        break;
                }
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.ok), errorListener);
        mAlertDialog.show();
    }

}
