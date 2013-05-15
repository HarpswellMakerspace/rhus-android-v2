package org.calflora.observer.api;

import java.io.File;
import java.util.Map;

import org.calflora.observer.Observer;
import org.calflora.observer.model.Observation;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;


import android.content.Context;
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
				RestTemplate restTemplate = getRestTemplate();
				restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
				return restTemplate.getForObject( URI, APIResponseSignIn.class  );
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
				RestTemplate restTemplate = getRestTemplate();
				restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
				return restTemplate.getForObject( URI, APIResponseOrganizations.class  );
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
				RestTemplate restTemplate = getRestTemplate();
				restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
				return restTemplate.getForObject( URI, APIResponseOrganization.class  );
			} 
		}

		return new OrganizationJsonRequest();
	}


	@Override
	public SpringAndroidSpiceRequest<APIResponseProject> getProjectRequest(
			String projectId) {

		Uri.Builder uriBuilder = Uri.parse( API_URI ).buildUpon();
		uriBuilder.appendQueryParameter( "what", "projdata" );
		uriBuilder.appendQueryParameter( "project", projectId);
		uriBuilder.appendQueryParameter( "token", Observer.settings.getString("APIKey", null) );
		final String URI = uriBuilder.build().toString();

		class ProjectJsonRequest extends SpringAndroidSpiceRequest< APIResponseProject > {

			public ProjectJsonRequest() {
				super( APIResponseProject.class );
			}

			@Override
			public APIResponseProject loadDataFromNetwork() throws Exception {
				RestTemplate restTemplate = getRestTemplate();
				restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
				return restTemplate.getForObject( URI, APIResponseProject.class  );
			} 
		}

		return new ProjectJsonRequest();
	}



	@Override
	public SpringAndroidSpiceRequest<APIResponseUpload> getUploadRequest(
			final Observation o, final Context context) {

		Uri.Builder uriBuilder = Uri.parse( API_URI ).buildUpon();
		//uriBuilder.appendQueryParameter( "token", Observer.settings.getString("APIKey", null) );
		final String URI = Uri.decode(uriBuilder.build().toString());
		//final HashMap<String, String> record = o.getFields();
		//Only handle the base fields for now..
		//We need to change the way the API is structured to make this easier

		
		final Map<String, Object> fields = o.getFields();
		if(fields.get("taxon") == null){
			fields.put("taxon", "unknown");
		}
		fields.put("lat", o.latitude);
		fields.put("lng", o.longitude);
		fields.put("date", o.date_added);
		fields.put("timestamp", o.timestamp_added);
		
		class UploadJsonRequest extends SpringAndroidSpiceRequest<APIResponseUpload> {

			public UploadJsonRequest(Class<APIResponseUpload> clazz) {
				super(clazz);
			}


			@Override
			public APIResponseUpload loadDataFromNetwork() throws Exception {
				
				MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
				parts.add("token", Observer.instance.getAPIKey());
				String fieldsJson = Observer.mapper.writeValueAsString(fields);
				parts.add("record",fieldsJson);

				for(org.calflora.observer.model.Attachment a : o.attachments){
					if(a.name.equals("thumbnail")){
						continue;
					}
					
					HttpHeaders imageHeaders = new HttpHeaders();
					imageHeaders.setContentType(MediaType.IMAGE_JPEG);
					HttpEntity<FileSystemResource> entity = new HttpEntity<FileSystemResource>(new FileSystemResource(context.getFilesDir() + "/" + a.localPath), imageHeaders);
					parts.add(a.name, entity);
				
				}

				HttpHeaders requestHeaders = new HttpHeaders();
				// Sending multipart/form-data
				requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

				// Populate the MultiValueMap being serialized and headers in an HttpEntity object to use for the request
				HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(
						parts, requestHeaders);
				
				RestTemplate restTemplate = getRestTemplate();
				restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
				restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
				restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

				return restTemplate.postForObject(URI, requestEntity, APIResponseUpload.class);
	
			} 
		}
		return new UploadJsonRequest(null);


	}
}