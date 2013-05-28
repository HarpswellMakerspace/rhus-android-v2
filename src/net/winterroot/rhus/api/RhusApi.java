package net.winterroot.rhus.api;

import java.io.InputStream;
import java.util.Map;

import net.winterroot.rhus.settings.RhusSettings;

import org.apache.commons.io.IOUtils;
import org.calflora.observer.Observer;
import org.calflora.observer.model.Observation;
import org.springframework.http.HttpAuthentication;
import org.springframework.http.HttpBasicAuthentication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.net.Uri;

import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

public class RhusApi {

	
	
	public static SpringAndroidSpiceRequest<RhusApiResponse> uploadObservationRequest(
			Observation o) {

		Uri.Builder uriBuilder = Uri.parse( RhusSettings.API_URI + "/" + RhusSettings.DATABASE ).buildUpon();
		final String URI = Uri.decode(uriBuilder.build().toString());
		
		final Map<String, Object> fields = o.getFields();
		if(fields.get("taxon") == null){
			fields.put("taxon", "unknown");
		}
		fields.put("latitude", o.latitude);
		fields.put("longitude", o.longitude);
		fields.put("created_at", o.date_added);
		fields.put("timestamp", o.timestamp_added);
		Point point = new Point();
		point.coordinates[0] = (float) o.latitude;
		point.coordinates[1] = (float) o.longitude;
		fields.put("geometry", point);
		
		class UploadObservationRequest extends SpringAndroidSpiceRequest< RhusApiResponse > {

			public UploadObservationRequest() {
				super( RhusApiResponse.class );
			}

			@Override
			public RhusApiResponse loadDataFromNetwork() throws Exception {
				
				// Set the username and password for creating a Basic Auth request
				HttpAuthentication authHeader = new HttpBasicAuthentication(RhusSettings.USERNAME, RhusSettings.PASSWORD);
				HttpHeaders requestHeaders = new HttpHeaders();
				requestHeaders.setAuthorization(authHeader);
				HttpEntity<?> requestEntity = new HttpEntity<Object>(fields, requestHeaders);
				
				// Create a new RestTemplate instance
				RestTemplate restTemplate = new RestTemplate();
				
				// Add the String message converter
				restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
				
				return restTemplate.postForObject( URI, requestEntity, RhusApiResponse.class  );
			}
		}

		return new UploadObservationRequest();
	}

	public static SpringAndroidSpiceRequest<RhusApiResponse> uploadAttachmentRequest( 
			final String documentId, final String revision,
			final String name, final InputStream fileStream) {

		
		class AttachmentUploadRequest extends SpringAndroidSpiceRequest<RhusApiResponse> {

			public AttachmentUploadRequest() {
				super( RhusApiResponse.class);
			}


			@Override
			public RhusApiResponse loadDataFromNetwork() throws Exception {
				
				String URI = RhusSettings.API_URI + RhusSettings.DATABASE + "/" + documentId + "/" + name +"?rev=" + revision;
				
				// Set the username and password for creating a Basic Auth request
				HttpAuthentication authHeader = new HttpBasicAuthentication(RhusSettings.USERNAME, RhusSettings.PASSWORD);
				HttpHeaders requestHeaders = new HttpHeaders();
				requestHeaders.setAuthorization(authHeader);
				//HttpEntity<?> requestEntity = new HttpEntity<Object>(fields, requestHeaders);
				
				// Image setup
				requestHeaders.setContentType(MediaType.IMAGE_JPEG);
				byte[] bytes = IOUtils.toByteArray(fileStream);
				HttpEntity<byte[]> entity = new HttpEntity<byte[]>(bytes, requestHeaders);
				
				RestTemplate restTemplate = getRestTemplate();
				restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());

				//return restTemplate.put(URI, entity, RhusApiResponse.class);
				ResponseEntity<RhusApiResponse> response = restTemplate.exchange(URI, HttpMethod.PUT, entity, RhusApiResponse.class);
				return response.getBody();
				
			} 
		}
		return new AttachmentUploadRequest();

	}	
}
