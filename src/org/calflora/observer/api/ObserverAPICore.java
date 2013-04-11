package org.calflora.observer.api;

import org.calflora.observer.model.Observation;

import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

public interface ObserverAPICore {
	
	static String API_URI = "";
	
	public static final int API_SIGN_IN = 1;
	public static final int API_REGISTER = 2;
	public static final int API_ORGANIZATIONS = 3;
	public static final int API_ORGANIZATION_DETAIL = 4;
	public static final int API_PROJECTS = 5;
	public static final int API_PROJECT_DETAIL = 6;
	
	//public SpringAndroidSpiceRequest<APIResponseSignIn> signInRequest(String username, String password, ObserverAPIListener delegate);
	public SpringAndroidSpiceRequest<APIResponseSignIn> signInRequest(String username, String password);
	//public SpringAndroidSpiceRequest<APIResponseRegister> registerRequest(String email, String username, String password);
	public SpringAndroidSpiceRequest<APIResponseOrganizations> getOrganizationsRequest();
	public SpringAndroidSpiceRequest<APIResponseOrganization> getOrganizationRequest(String organizationId);
	public SpringAndroidSpiceRequest<APIResponseProject> getProjectRequest(String projectId);
	public SpringAndroidSpiceRequest<APIResponseUpload> getUploadRequest(Observation o);

	

}
