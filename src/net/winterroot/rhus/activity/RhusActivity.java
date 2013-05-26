package net.winterroot.rhus.activity;

import org.calflora.observer.Observer;
import org.calflora.observer.model.Organization;
import org.calflora.observer.model.Project;

import android.content.Intent;

import com.actionbarsherlock.app.SherlockActivity;


public class RhusActivity extends SherlockActivity {

	@Override
	protected void onResume() {
		super.onResume();
		
		Organization organization = new Organization();
		organization.name = "Wildflowers of Detroit";

		Project project = new Project();
		project.name = "Wildflowers of Detroit";
		
		Observer.instance.setProject(project);
		Observer.instance.setOrganization(organization);
		
		Intent intent = new Intent("org.calflora.observer.action.WORKSPACE");
		startActivity(intent);
	}

}
