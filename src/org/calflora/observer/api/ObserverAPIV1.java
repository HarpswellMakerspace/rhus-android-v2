package org.calflora.observer.api;

import org.calflora.observer.model.Observation;

import android.content.Context;

import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

public class ObserverAPIV1 implements ObserverAPICore {

	@Override
	public SpringAndroidSpiceRequest<APIResponseSignIn> signInRequest(
			String username, String password) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SpringAndroidSpiceRequest<APIResponseOrganizations> getOrganizationsRequest() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SpringAndroidSpiceRequest<APIResponseOrganization> getOrganizationRequest(
			String organizationId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SpringAndroidSpiceRequest<APIResponseProject> getProjectRequest(
			String projectId) {
		// TODO Auto-generated method stub
		return null;
	}

	

	@Override
	public SpringAndroidSpiceRequest<APIResponseUpload> getUploadRequest(
			Observation o, Context context) {
		// TODO Auto-generated method stub
		return null;
	}


}
