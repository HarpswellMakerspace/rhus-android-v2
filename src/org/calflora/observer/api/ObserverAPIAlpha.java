package org.calflora.observer.api;

import org.calflora.observer.Observer;

import android.net.Uri;

import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

public class ObserverAPIAlpha implements ObserverAPICore {

	public static String API_URI = "https://www.calflora.org/mgrapi";
		
	@Override
	public SpringAndroidSpiceRequest<APIResponseSignIn> signInRequest(
			String username, String password) {

		Uri.Builder uriBuilder = Uri.parse( API_URI ).buildUpon();
		uriBuilder.appendQueryParameter( "what", "signin" );
		uriBuilder.appendQueryParameter( "email", username );
		uriBuilder.appendQueryParameter( "passwd", password );
		final String URI = Uri.decode(uriBuilder.build().toString()); //robospice encode for us

		class SignInJsonRequest extends SpringAndroidSpiceRequest< APIResponseSignIn > {

			public SignInJsonRequest() {
				super( APIResponseSignIn.class );
			}

			@Override
			public APIResponseSignIn loadDataFromNetwork() throws Exception {
				return getRestTemplate().getForObject( URI, APIResponseSignIn.class  );
			}
		}

		return new SignInJsonRequest();
	}


	@Override
	public SpringAndroidSpiceRequest<APIResponseOrganizations> getOrganizationsRequest() {
		 Uri.Builder uriBuilder = Uri.parse( API_URI ).buildUpon();
	        uriBuilder.appendQueryParameter( "what", "orgs" );
	        uriBuilder.appendQueryParameter( "token", Observer.settings.getString("APIKey", null) );
	     final String URI = Uri.decode(uriBuilder.build().toString());
	    
		
	     class OrganizationsJsonRequest extends SpringAndroidSpiceRequest< APIResponseOrganizations > {
	    	 
	    	  public OrganizationsJsonRequest() {
			        super( APIResponseOrganizations.class );
			    }

			    @Override
			    public APIResponseOrganizations loadDataFromNetwork() throws Exception {
			        return getRestTemplate().getForObject( URI, APIResponseOrganizations.class  );
			    } 
	     }
	     
		return new OrganizationsJsonRequest();
	}


	@Override
	public SpringAndroidSpiceRequest<APIResponseOrganization> getOrganizationRequest(
			String organizationId) {
		
		 Uri.Builder uriBuilder = Uri.parse( API_URI ).buildUpon();
	        uriBuilder.appendQueryParameter( "what", "orgdata" );
	        uriBuilder.appendQueryParameter( "orgid", organizationId);
	        uriBuilder.appendQueryParameter( "token", Observer.settings.getString("APIKey", null) );
	     final String URI = uriBuilder.build().toString();
		
	     class OrganizationJsonRequest extends SpringAndroidSpiceRequest< APIResponseOrganization > {
	    	 
	    	  public OrganizationJsonRequest() {
			        super( APIResponseOrganization.class );
			    }

			    @Override
			    public APIResponseOrganization loadDataFromNetwork() throws Exception {
			        return getRestTemplate().getForObject( URI, APIResponseOrganization.class  );
			    } 
	     }
	     
		return new OrganizationJsonRequest();
	}


	@Override
	public SpringAndroidSpiceRequest<APIResponseProject> getProjectRequest(
			String projectId) {

		 Uri.Builder uriBuilder = Uri.parse( API_URI ).buildUpon();
	        uriBuilder.appendQueryParameter( "what", "projdata" );
	        uriBuilder.appendQueryParameter( "orgid", projectId);
	        uriBuilder.appendQueryParameter( "token", Observer.settings.getString("APIKey", null) );
	     final String URI = uriBuilder.build().toString();
		
	     class ProjectJsonRequest extends SpringAndroidSpiceRequest< APIResponseProject > {
	    	 
	    	  public ProjectJsonRequest() {
			        super( APIResponseProject.class );
			    }

			    @Override
			    public APIResponseProject loadDataFromNetwork() throws Exception {
			        return getRestTemplate().getForObject( URI, APIResponseProject.class  );
			    } 
	     }
	     
		return new ProjectJsonRequest();
	}
	
	



}
