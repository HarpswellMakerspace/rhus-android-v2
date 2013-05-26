package org.calflora.observer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.calflora.observer.api.APIResponseOrganization;
import org.calflora.observer.api.APIResponseOrganizations;
import org.calflora.observer.api.IdNameItem;
import org.calflora.observer.model.Organization;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import com.actionbarsherlock.view.*;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.octo.android.robospice.exception.NoNetworkException;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.simple.SmallBinaryRequest;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;


public class OrganizationsActivity extends ApiActivity {
	
	//static final String mockJson ="[{\"id\":\"42\", \"name\":\"Yosemite NP Invasive Plant Management\"},{\"id\":\"20\", \"name\":\"Presidio Trust Natural Resources Team\"},{\"id\":\"0\", \"name\":\"Independent\"}]";
	//static final String mockOrganizationJSON = "{\"id\":\"1\", \"name\":\"Yosemite NP Invasive Plant Management\", \"splashGraphic\":\"http://www.nps.gov/yose/naturescience/images/torch-web_1.jpg\",\"logoGraphic\":\"http://www.yosemiteconservancy.org/sites/all/themes/yosemite/images/logo.gif\",\"orgURL\":\"http://www.yosemiteconservancy.org/\",\"projects\":[{\"id\":\"pr1\", \"name\":\"Weed Inventory 2012\"},{\"id\":\"pr2\", \"name\":\"Weed Inventory 2013\"}]}";
	
	ArrayList<IdNameItem> organizations;
	List<Map<String,Object>> organizationsListData; 

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_organizations);    
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.organizations, (Menu) menu);
		return true;
	}


	public boolean organizationsRequestSent = false;

	@Override
	protected void onStart() {
		super.onStart();
		

		if ( ! organizationsRequestSent ){
			
			organizationsRequestSent = true;
			
			mStatusMessageView.setText("Getting Organizations");

			class OrganizationsRequestListener implements RequestListener< APIResponseOrganizations > {
				@Override
				public void onRequestFailure( SpiceException e ) {

					if(e instanceof NoNetworkException){
						Observer.toast("Network connection is unavailable.  Please connect with wifi to choose a different organization.",  OrganizationsActivity.this);
						spiceManager.cancelAllRequests();
					} else {
						Observer.unhandledErrorToast("Error during request: " + e.getMessage(), OrganizationsActivity.this);
						e.printStackTrace();
					}
					
				}

				@Override
				public void onRequestSuccess( APIResponseOrganizations response ) {

					if(response.status.equals("ERROR") ){
						Toast.makeText( OrganizationsActivity.this, "Error during request: " + response.message, Toast.LENGTH_LONG ).show();
						showProgress(false);
						return;

		        	}
					
					showProgress(false);
					organizations = response.data;
					// TODO And we may want to cache this here
					populateList();


				}
			}

			

			SpringAndroidSpiceRequest<APIResponseOrganizations> request;
			showProgress(true);
			request = Observer.observerAPI.getOrganizationsRequest();
			spiceManager.execute(request, JSON_CACHE_KEY, DurationInMillis.NEVER, new OrganizationsRequestListener() );
			

		}
	}
	
	
	
	@Override
	protected void onRestart() {
		super.onRestart();
		showProgress(false);

	}




	protected void populateList(){
		
		ListView lv = (ListView)findViewById(R.id.organizationsListView);

		//TODO: Any of the above exceptions should be handled gracefully
		//Though the more salient error would be upon loading JSON remotely into the 
		

		class ListDataItem {
			String name;
			int image;
		}
		
		ArrayList<ListDataItem> listData = new ArrayList<ListDataItem>();
		ListDataItem listDataItem = null;
		
		
		for(IdNameItem o: organizations){
				listDataItem = new ListDataItem();
				listDataItem.name = o.name;
				if(o.name.contains("Yosemite")){
					listDataItem.image = R.drawable.logo;
				}
				listData.add(listDataItem);
		}
		
		class MyCustomAdaptor extends ArrayAdapter<ListDataItem>
		{
		    Context context;
		    int layoutResourceId;   
		    
		    ListDataItem currentItem;
		    ArrayList<ListDataItem> data;
		    /** Called when the activity is first created. */
		    // TODO Auto-generated constructor stub
		    public MyCustomAdaptor(Context context, int layoutResourceId, ArrayList<ListDataItem> data) 
		    {
		        super(context,layoutResourceId,data);
		        this.layoutResourceId = layoutResourceId;
		        this.context=context;
		        this.data = data;
		    }
		    @Override
		    public View getView(int position, View convertView, ViewGroup parent)
		    {
		        //View row = convertView;
		    	View row = convertView;
		        MyStringReaderHolder holder;
		        
		        if(row==null)
		        {
		            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
		            row = inflater.inflate(layoutResourceId, parent,false);
		            
		            holder= new MyStringReaderHolder();
		            
		            holder.nameView =(TextView)row.findViewById(R.id.col1);
		            holder.imageView=(ImageView) row.findViewById(R.id.list_item_image_view);
		            
		            row.setTag(holder);
		        }
		        else
		        {
		            holder=(MyStringReaderHolder) row.getTag();
		        }
		        
		        currentItem = (ListDataItem) data.get(position);
		        System.out.println("Position="+position);
		      
		        holder.nameView.setText(currentItem.name);
		        holder.imageView.setImageResource(currentItem.image);
		        return row;
		    }
		    
		    class MyStringReaderHolder
		    {
		        TextView nameView;
		        ImageView imageView;
		    }
		}
		
		MyCustomAdaptor adapter = new MyCustomAdaptor(this, R.layout.list_item_single_image, listData);
        lv.setAdapter(adapter);
        
        lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long rowid) {
				
				IdNameItem organizationIdData = organizations.get(arg2);
				
				requestOrganizationDetail(organizationIdData.id);
				
			}


        });
	}

	public void requestOrganizationDetail(final String id){
	
		class OrganizationRequestListener implements RequestListener< APIResponseOrganization > {
	        @Override
	        public void onRequestFailure( SpiceException e ) {
	        	
	        	// TODO remove bypass
				showProgress(false);
				
				Organization cachedOrganization = Observer.instance.getCachedOrganizationData(id);
				if(cachedOrganization != null){
					Toast.makeText( OrganizationsActivity.this, "Error during request: using cached organization data", Toast.LENGTH_LONG ).show();
					Observer.instance.setOrganization(cachedOrganization);	
					downloadLogo();
					
				} else {
					Toast.makeText( OrganizationsActivity.this, "Error during request and no cache available.  Please find an internet connection to select an organization", Toast.LENGTH_LONG ).show();
				}
	        	
	        }

	        @Override
	        public void onRequestSuccess( APIResponseOrganization response ) {

	        	if(response.status.equals("ERROR") ){
					Toast.makeText( OrganizationsActivity.this, "Error during request: " + response.message, Toast.LENGTH_LONG ).show();
					showProgress(false);
					return;

	        	}
	        	
				// TODO This needs to be cached
				Observer.instance.setOrganization(response.data);
				
				//And get the graphics..
				
				downloadLogo();
				
				
	        }
	    }
		
		mStatusMessageView.setText("Getting Organization Details");
		showProgress(true);
	
		spiceManager.execute( Observer.observerAPI.getOrganizationRequest(id), JSON_CACHE_KEY, DurationInMillis.NEVER, new OrganizationRequestListener() );
	
		
	}
	
	public void downloadLogo(){
		
		class LogoDownloadListener implements RequestListener< InputStream > {
			 @Override
		        public void onRequestFailure( SpiceException e ) {
		        	
		        	// TODO remove bypass
					showProgress(false);
		        	
		            Toast.makeText( OrganizationsActivity.this, "Error during image download: " + e.getMessage(), Toast.LENGTH_LONG ).show();
					e.printStackTrace();
		        }

				@Override
				public void onRequestSuccess(InputStream inputStream) {
					
					String logoFileName = Observer.instance.getOrganization().getLogoGraphicPath();
												
					// TODO where does the file get stored?
					FileOutputStream outputStream = null;
					try {
						outputStream = getBaseContext().openFileOutput(logoFileName, Context.MODE_PRIVATE);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					int bytes = -1;
					byte[] buffer = new byte[1024];
					try {
						while((bytes = inputStream.read(buffer)) != -1) {
							outputStream.write(buffer);
						}

						outputStream.close();
						
					} catch (IOException e) {
						e.printStackTrace();
						Observer.toast("Logo failed to download", getBaseContext());
					}
					
					// OK, now download the splash screen
					downloadSplash();
					
				}
		}
		
		Organization o = Observer.instance.getOrganization();
		if(o.logoGraphic != null){
			SmallBinaryRequest request = new SmallBinaryRequest(Observer.instance.getOrganization().logoGraphic);

			mStatusMessageView.setText("Getting Organization Graphics");
			showProgress(true);
			spiceManager.execute(request, JSON_CACHE_KEY, DurationInMillis.NEVER, new LogoDownloadListener());
		} else {
			downloadSplash();
		}
		

	}
	
	public void downloadSplash(){
		
		class SplashDownloadListener implements RequestListener< InputStream > {
			 @Override
		        public void onRequestFailure( SpiceException e ) {
		        	
		        	// TODO remove bypass
					showProgress(false);
		        	
		            Toast.makeText( OrganizationsActivity.this, "Error during image download: " + e.getMessage(), Toast.LENGTH_LONG ).show();
					e.printStackTrace();
		        }

				@Override
				public void onRequestSuccess(InputStream inputStream) {
					
					String splashFileName =  Observer.instance.getOrganization().getSplashGraphicPath();
					
					// TODO where does the file get stored?
					// This logic should go into the model
					FileOutputStream outputStream = null;
					try {
						outputStream = getBaseContext().openFileOutput(splashFileName, Context.MODE_PRIVATE);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					int bytes = -1;
					byte[] buffer = new byte[1024];
					try {
						while((bytes = inputStream.read(buffer)) != -1) {
							outputStream.write(buffer);
						}

						outputStream.close();
						
					} catch (IOException e) {
						e.printStackTrace();
						Observer.toast("Splash failed to download", getBaseContext());
					}
					
					// OK, now we can move onto projects
					ImageView home = (ImageView)findViewById(android.R.id.home);
					Drawable logoImage = null;
					
					try {
						String logoFileName = Observer.instance.getOrganization().getLogoGraphicPath();
						logoImage = Drawable.createFromPath(logoFileName); 

					} catch (Exception e) {
						// File not found
					}
					
					if(logoImage != null){
						home.setImageDrawable(logoImage);
					}
					
					showSplash();
				}
		}
		
		if(Observer.instance.getOrganization().splashGraphic != null){
			SmallBinaryRequest request = new SmallBinaryRequest(Observer.instance.getOrganization().splashGraphic);

			mStatusMessageView.setText("Getting Organization Graphics");
			showProgress(true);
			spiceManager.execute(request, JSON_CACHE_KEY, DurationInMillis.NEVER, new SplashDownloadListener());
		} else {
			showSplash();
		}


	}
	
	public void showSplash(){
		Intent intent = new Intent("org.calflora.observer.action.SPLASH_THEN_PROJECTS");
		startActivity(intent);
		showProgress(false);	
		finish(); // TODO We should handle organization and project as fragments in a flow

	}
		
}
	