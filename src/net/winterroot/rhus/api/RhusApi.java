package net.winterroot.rhus.api;

import java.io.InputStream;
import java.util.Map;

import org.calflora.observer.Observer;
import org.calflora.observer.model.Observation;
import org.springframework.http.HttpAuthentication;
import org.springframework.http.HttpBasicAuthentication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import android.net.Uri;

import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

public class RhusApi {

	public static String API_URI = "https://wildflowers.winterroot.net/___";
	public static String USERNAME = "winterroot";
	public static String PASSWORD = "password";
	public static String DATABASE = "wildflowers_of_detroit";
	
	public SpringAndroidSpiceRequest<RhusApiResponse> uploadObservationRequest(
			Observation o) {

		Uri.Builder uriBuilder = Uri.parse( API_URI ).buildUpon();
		uriBuilder.appendQueryParameter( "token", Observer.settings.getString("APIKey", null) );
		final String URI = Uri.decode(uriBuilder.build().toString());
		
		final Map<String, Object> fields = o.getFields();
		if(fields.get("taxon") == null){
			fields.put("taxon", "unknown");
		}
		fields.put("lat", o.latitude);
		fields.put("lng", o.longitude);
		fields.put("date", o.date_added);
		fields.put("timestamp", o.timestamp_added);
		
		class UploadObservationRequest extends SpringAndroidSpiceRequest< RhusApiResponse > {

			public UploadObservationRequest() {
				super( RhusApiResponse.class );
			}

			@Override
			public RhusApiResponse loadDataFromNetwork() throws Exception {
				
				// Set the username and password for creating a Basic Auth request
				HttpAuthentication authHeader = new HttpBasicAuthentication(USERNAME, PASSWORD);
				HttpHeaders requestHeaders = new HttpHeaders();
				requestHeaders.setAuthorization(authHeader);
				HttpEntity<?> requestEntity = new HttpEntity<Object>(fields, requestHeaders);
				
				// Create a new RestTemplate instance
				RestTemplate restTemplate = new RestTemplate();
				
				return restTemplate.postForObject( URI, requestEntity, RhusApiResponse.class  );
			}
		}

		return new UploadObservationRequest();
	}

	public SpringAndroidSpiceRequest<RhusApiResponse> uploadAttachmentRequest( 
			final String documentId, final String revision,
			final String name, final InputStream fileStream) {

		
		class AttachmentUploadRequest extends SpringAndroidSpiceRequest<RhusApiResponse> {

			public AttachmentUploadRequest() {
				super(RhusApiResponse.class );
			}


			@Override
			public RhusApiResponse loadDataFromNetwork() throws Exception {
				
				String URI = API_URI + "/" + DATABASE + "/" + documentId + "/" + name +"?rev=" + revision;
				
				// Set the username and password for creating a Basic Auth request
				HttpAuthentication authHeader = new HttpBasicAuthentication(USERNAME, PASSWORD);
				HttpHeaders requestHeaders = new HttpHeaders();
				requestHeaders.setAuthorization(authHeader);
				//HttpEntity<?> requestEntity = new HttpEntity<Object>(fields, requestHeaders);
				
				// Image setup
				requestHeaders.setContentType(MediaType.IMAGE_JPEG);
				HttpEntity<InputStream> entity = new HttpEntity<InputStream>(fileStream, requestHeaders);
				
				RestTemplate restTemplate = getRestTemplate();
				return restTemplate.postForObject(URI, entity, RhusApiResponse.class);
				
				/*
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
				 *
				 *
				 */
			
			} 
		}
		return new AttachmentUploadRequest();

	}	
}
