package org.calflora.observer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.calflora.observer.model.Observation;
import org.calflora.observer.model.Plant;
import org.calflora.observer.model.Project;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.services.transport.payload.ByteArrayPayload;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryPrompt;
import org.json.JSONException;
import org.kxml2.io.KXmlParser;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.FormLoaderListener;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.tasks.FormLoaderTask;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.views.ODKView;
import org.odk.collect.android.widgets.QuestionWidget;
import org.xmlpull.v1.XmlPullParserException;

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
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.*;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ObservationActivity extends SherlockActivity implements
ActionBar.TabListener, FormLoaderListener {

	private static final int SELECT_PLANT = 1001;
	private static final int ODK_VIEW_TAG = 2002;
	private static final String ODK_AUTHORITY = "org.calflora.observer.provider.forms";

	private ActionBar mActionBar;
	private ImageView plantThumbnailView;

	private String mFormPath = "";
	private FormController mFormController = null;
	private List<ObservationODKFragment> odkFragments;
	private List<String> tabLabels;

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
		mFormPath = Environment.getExternalStorageDirectory().toString() + "/Calflora/" + Observer.getInstance().odkXmlForProject(); //OAT.xml";
		mFormLoaderTask = new FormLoaderTask(instancePath, null, null);
		mFormLoaderTask.setFormLoaderListener(this);
		mFormLoaderTask.execute(mFormPath);

		odkFragments = new ArrayList<ObservationODKFragment>();
		tabLabels = new ArrayList<String>();

		plantThumbnailView = (ImageView) findViewById(R.id.plant_thumbnail);

		// Set up the action bar.
		mActionBar = getActionBar();
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

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

						for(ObservationODKFragment odkFragment : odkFragments) {
							RelativeLayout layout = (RelativeLayout) odkFragment.getView();
							ODKView odkView = (ODKView) layout.findViewWithTag(ObservationActivity.ODK_VIEW_TAG);
							LinkedHashMap<FormIndex, IAnswerData> answers = odkView.getAnswers();
							/*
							 * This is how you handle the constraints
							 * probably want to save each time they hit a tab item
							 * */
							 //FailedConstraint constraint = mFormController.saveAllScreenAnswers(answers,
							//		evaluateConstraints);

							Iterator<FormIndex> it = answers.keySet().iterator();
							while (it.hasNext()) {
								FormIndex index = it.next();
								// Within a group, you can only save for question events
								//if (getEvent(index) == FormEntryController.EVENT_QUESTION) {
								int saveStatus;
								IAnswerData answer = answers.get(index);
								/*if (evaluateConstraints) {
            	                		saveStatus = answerQuestion(index, answer);
            	                        if (saveStatus != FormEntryController.ANSWER_OK) {
            	                            return new FailedConstraint(index, saveStatus);
            	                        }
            	                    } else {*/
								mFormController.saveAnswer(index, answer);
								//}
								/*} else {
            	                    Log.w(t,
            	                        "Attempted to save an index referencing something other than a question: "
            	                                + index.getReference());
            	                }*/
							}

							/*
            				if (constraint != null) {
            					createConstraintToast(constraint.index, constraint.status);
            					return false;
            				}
							 */
						}


						//mFormController.get
						//This isn't the right map yet, because it doesn't take groups into account
						Map<String, String> values;
						ByteArrayPayload payload = null;
						try {
							payload = mFormController.getFilledInFormXml();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						InputStream is = payload.getPayloadStream();
						values = getOdkCollectFormValues(is);
						is = null;
						payload = null;

						// Add values to the current observation

						for(String key : values.keySet()){
							Observer.currentObservation.setField(key, values.get(key));
						}

						try {
							Observer.currentObservation.storeObservation();
						} catch (JSONException e1) {
							Observer.toast("Failed to store observation", getApplicationContext());
							e1.printStackTrace();
							return;
						}

						Toast.makeText(ObservationActivity.this, "Observation Saved", Toast.LENGTH_LONG).show();

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


	protected Map<String, String> getOdkCollectFormValues(InputStream is) {

		Document xmlDoc = new Document();
		KXmlParser xmlParser = new KXmlParser();
		try {
			xmlParser.setInput(is, "UTF-8");
			xmlDoc.parse(xmlParser);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (XmlPullParserException e) {
			e.printStackTrace();
			return null;
		}
		Element rootEl = xmlDoc.getRootElement();
		Node rootNode = rootEl.getRoot();
		Element dataEl = rootNode.getElement(0);
		Map<String, String> values = new HashMap<String, String>();
		for (int i = 0; i < dataEl.getChildCount(); i++) {
			Element groupEl = dataEl.getElement(i);
			for(int j = 0; j < groupEl.getChildCount(); j++){
				Element child = groupEl.getElement(j);
				String key = child.getName();
				String value = child.getChildCount() > 0 ? child.getText(0) : null;
				values.put(key, value);
			}
		}
		return values;
	}

	private void loadPlant(String taxon) {


		TextView commonName = (TextView)findViewById(R.id.common_name);
		TextView taxonName = (TextView)findViewById(R.id.taxon);


		Plant plant = Project.getPlant(taxon);
		if(plant != null) {
			Observer.currentObservation.setField("taxon", plant.getTaxon());
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
				// Show default image for plant
				plantThumbnailView.setImageResource(R.drawable.calflora_observer_icon);
			}

		} else {
			Observer.currentObservation.setField("taxon", taxon);
			//Placehold for unknown plant icon
			plantThumbnailView.setImageResource(R.drawable.calflora_observer_icon);
			commonName.setText(taxon);
			taxonName.setText("");

		}


	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.observation, menu);
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
		int position = tab.getPosition();
		transaction = fragmentManager.beginTransaction();

		for(ObservationODKFragment fragment : odkFragments){
			transaction.hide(fragment);
		}

		switch (position){
		case 0:
		case 1:
		case 2:
			// Assessment, or ODK configured 2nd tab
			//selectedTab = Tabs.SUMMARY;
			ObservationODKFragment fragment = odkFragments.get(position);
			transaction.show(fragment);
			break;	
		}

		transaction.commit();

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
				c = getContentResolver().query( Uri.parse("content://" + ODK_AUTHORITY + "/forms"), null,
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
		boolean firstGroup = true;
		setup: for(int event=0; !done; event = formController.stepToNextScreenEvent()) {
			switch (event) {
			case FormEntryController.EVENT_BEGINNING_OF_FORM:
				continue setup;

			case FormEntryController.EVENT_END_OF_FORM:
				done = true;
				break;
			case FormEntryController.EVENT_QUESTION:
				break;
			case FormEntryController.EVENT_GROUP:

				// For the pages (groups) of the form

				ODKView odkv = null;
				// should only be a group here if the event_group is a field-list
				try {
					FormEntryPrompt[] prompts = formController.getQuestionPrompts();
					FormEntryCaption[] groups = formController.getGroupsForCurrentIndex();

					FormEntryCaption[] emptyGroups = new FormEntryCaption[0];
					odkv = new ODKView(this, formController.getQuestionPrompts(),
							emptyGroups, false);
					odkv.setTag(ObservationActivity.ODK_VIEW_TAG);
					FormEntryCaption g = groups[0];
					tabLabels.add(g.getLongText());

					
				} catch (RuntimeException e) {
					// this is badness to avoid a crash.
					done = true;
					e.printStackTrace();
					break;
				}

				// Makes a "clear answer" menu pop up on long-click
				for (QuestionWidget qw : odkv.getWidgets()) {
					if (!qw.getPrompt().isReadOnly()) {
						registerForContextMenu(qw);
					}
				}


				ObservationODKFragment fragment;

				if(firstGroup){
					fragment = new ObservationMainFragment(odkv);
					firstGroup = false;
				} else {
					fragment = new ObservationODKFragment(odkv);
				}
				odkFragments.add(fragment);

				break;

			default :
				createErrorDialog("Internal error: step to prompt failed", false);
				//..Log.e(t, "Attempted to create a view that does not exist.");
				// this is badness to avoid a crash.

				break;

			}
		}

		// End ODK code


		//Set up fragments
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction transaction;
		transaction = fragmentManager.beginTransaction();

		for(ObservationODKFragment fragment : odkFragments){
			transaction.add(R.id.observation_fragment_container, fragment);
			transaction.hide(fragment);
		}
		transaction.show(odkFragments.get(0));
		transaction.commit();

		// I think the fragments need to be called in add() before setting up the tabs
		// Didn't fix it, need to create a listener for mapfragment and don't initialize until after
		// it's createView is called, whenever that is.. (onStart() on that MapFragment, not on this fragement)
		for(String tabLabel : tabLabels) {
			mActionBar.addTab(mActionBar.newTab()
					.setText(tabLabel)
					.setTabListener(this)
					);
		}


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
