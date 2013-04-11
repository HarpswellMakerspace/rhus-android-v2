package org.calflora.observer.api;

import org.calflora.observer.Observer;
import org.calflora.observer.model.Observation;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;


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
	


	@Override
	public SpringAndroidSpiceRequest<APIResponseUpload> getUploadRequest(
			final Observation o) {
		
	
		
		 Uri.Builder uriBuilder = Uri.parse( API_URI ).buildUpon();
	     //uriBuilder.appendQueryParameter( "token", Observer.settings.getString("APIKey", null) );
	     final String URI = Uri.decode(uriBuilder.build().toString());
	     //final HashMap<String, String> record = o.getFields();
	     //Only handle the base fields for now..
	     //We need to change the way the API is structured to make this easier
	     
	     class BaseFields {
	    	 public String taxon;
	    	 public double lat;
	    	 public double lng;
	    	 public String date;
	     }
	     final BaseFields base = new BaseFields();
	     base.taxon = o.plant.getTaxon();
	     base.lat = o.latitude;
	     base.lng = o.longitude;
	    
		
	     class UploadJsonRequest extends SpringAndroidSpiceRequest<APIResponseUpload> {
	    	 
	    	  public UploadJsonRequest() {
			        super( APIResponseUpload.class );
			    }

			    @Override
			    public APIResponseUpload loadDataFromNetwork() throws Exception {
			    	
					MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
					for(org.calflora.observer.model.Attachment a : o.attachments){

						parts.add(a.name, new FileSystemResource(a.localPath));

					}
					//parts.add("record", base);
					return getRestTemplate().postForObject(URI, parts, APIResponseUpload.class);
			    } 
	     }
	 	return new UploadJsonRequest();


	}
     /*
	public class UploadFileRequest extends SpringAndroidSpiceRequest<String>{
		private static final String TAG = "UploadFileRequest";
		private UploadRequestModel requestModel;
		private String link;
		public UploadFileRequest(UploadRequestModel model, String link) {
		    super(String.class);
		    requestModel = model;
		    this.link = link;
		}

		@Override
		public String loadDataFromNetwork() throws Exception {    

		    MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
		    parts.add("file1", new FileSystemResource(requestModel.getFile1()));
		    parts.add("file2", new FileSystemResource(requestModel.getFile1()));

		    HttpHeaders headers = new HttpHeaders();
		    HttpEntity<MultiValueMap<String, Object>> request = 
		            new HttpEntity<MultiValueMap<String, Object>>(parts, headers);

		    return getRestTemplate().postForObject(link, request, String.class);

		}

		}
*/

}
