package net.winterroot.rhus.util;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class RHImage {

	// Utility Functions
	
	public static Bitmap rotateImage(Bitmap bitmap){
		Bitmap bitMapImage = null;
		int orientation = 90;
	    if(orientation > 0){
       	 Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            bitMapImage = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
            		bitmap.getHeight(), matrix, true);	                   
       }
	   return bitMapImage;
       
	}

	public static Bitmap getResizedBitmap(String filePath, int targetWidth, int targetHeight) {

		Options options = new Options();
		options.inJustDecodeBounds = true;
        Bitmap bm = BitmapFactory.decodeFile(filePath, options);
        return getResizedBitmap(bm, targetWidth, targetHeight);
	}
	
	public static Bitmap getResizedBitmap(Bitmap bm, int targetWidth, int targetHeight) {        
        
	    int width = bm.getWidth();
	    int height = bm.getHeight();
	    float scaleWidth = ((float) targetWidth) / width;
	    float scaleHeight = ((float) targetHeight) / height;
	    // CREATE A MATRIX FOR THE MANIPULATION
	    Matrix matrix = new Matrix();
	    // RESIZE THE BIT MAP
	    matrix.postScale(scaleWidth, scaleHeight);

	    // "RECREATE" THE NEW BITMAP
	    Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
	    return resizedBitmap;
	}
	
		
		public static Bitmap resizeBitMapImage(String filePath, int targetWidth,
	            int targetHeight, int orientation) {
			Log.v("RHIMAGE Orientation", String.valueOf(orientation) );

	        Bitmap bitMapImage = null;
	        // First, get the dimensions of the image
	        Options options = new Options();
	        options.inJustDecodeBounds = true;
	        BitmapFactory.decodeFile(filePath, options);
	        double sampleSize = 0;
	        // Only scale if we need to
	        // (16384 buffer for img processing)
	        
	        //Switched inequality to scale by opposite dimension
	        Boolean scaleByHeight = Math.abs(options.outHeight - targetHeight) <= Math
	                .abs(options.outWidth - targetWidth);

	        
	        // Load, scaling to smallest power of 2 that'll get it <= desired
            // dimensions
	        if (options.outHeight * options.outWidth * 2 >= 1638) {
	            sampleSize = scaleByHeight ? options.outHeight / targetHeight
	                    : options.outWidth / targetWidth;
	            sampleSize = (int) Math.pow(2d,
	                    Math.floor(Math.log(sampleSize) / Math.log(2d)));
	        }

	        // Do the actual decoding
	        options.inJustDecodeBounds = false;
	        options.inTempStorage = new byte[128];
	        while (true) {
	            try {
	                options.inSampleSize = (int) sampleSize;
	                bitMapImage = BitmapFactory.decodeFile(filePath, options);
	                
	                if(orientation > 0){
	                	 Matrix matrix = new Matrix();
	                     matrix.postRotate(orientation);

	                     bitMapImage = Bitmap.createBitmap(bitMapImage, 0, 0, bitMapImage.getWidth(),
	                    		 bitMapImage.getHeight(), matrix, true);	                   
	                }
	                

	                break;
	            } catch (Exception ex) {
	                try {
	                    sampleSize = sampleSize * 2;
	                } catch (Exception ex1) {

	                }
	            }
	        }

	        //and crop
            int originx = 0;
            int originy = 0;
            int width = bitMapImage.getWidth() ;
            int height = bitMapImage.getHeight() ; 
            float ratio = (float) targetWidth / (float) targetHeight;
            float reverseRatio = (float) targetHeight / (float) targetWidth;
            //use smaller dimension
            if(width > height){
            	targetHeight = height;
            	targetWidth = (int)  (targetHeight * ratio);
            } else {
              	targetWidth = width;
            	targetHeight = (int) (targetWidth * reverseRatio);
            }
            
           	originx = (bitMapImage.getWidth() - targetWidth) / 2;
           	originy = (bitMapImage.getHeight() - targetHeight) / 2;
           	
           	//TODO: getting cropped to squares even on medium size.
           //	Log.v(TAG, Integer.toString() )
            bitMapImage = Bitmap.createBitmap(bitMapImage, originx, originy, targetWidth, targetHeight);

	        
	        return bitMapImage;
	    }

		public static int getOrientation(Context context, Uri photoUri) {
		    /* it's on the external media. */
		    Cursor cursor = context.getContentResolver().query(photoUri,
		            new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

		    if (cursor.getCount() != 1) {
		        return -1;
		    }

		    cursor.moveToFirst();
		    return cursor.getInt(0);
		}

	
}
