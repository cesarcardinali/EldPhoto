package org.eldorado.eldphoto;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static final int MEDIA_TYPE_IMAGE = 1;
	//public static final int MEDIA_TYPE_VIDEO = 2;
	private static final int SELECT_IMAGE_REQUEST_CODE = 100;
	public static final String PACKAGE_NAME = "org.eldorado.eldphoto";
	private static final String IMAGE_DIRECTORY_NAME = "Eldphoto";

	static Context context;
	
	private Uri fileUri;
	private Bitmap bitmap;
	
	private Button btnUploadPicture;
	private Button btnCapturePicture;
	private Button btnSelectPicture;
	String[] FilePathStrings;
    String[] FileNameStrings;
    GridView grid;
	File tempFile;
    File[] allFiles;
    ArrayAdapter<String> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		context = getApplicationContext();

		/**
		 * Capture image button click event
		 */
		String storagePatch = Environment.getExternalStorageDirectory().getAbsolutePath();
		tempFile = new File(storagePatch + IMAGE_DIRECTORY_NAME + "/a.jpg");
				
		btnSelectPicture = (Button) findViewById(R.id.btnSelectPicture);
		btnCapturePicture = (Button) findViewById(R.id.btnCapturePicture);
		btnUploadPicture = (Button) findViewById(R.id.btnUploadPicture);
		
		btnCapturePicture.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isDeviceSupportCamera()) {
					Toast.makeText(getApplicationContext(), "Sorry! Your device doesn't support camera", Toast.LENGTH_LONG).show();
				} else{
					Intent camActivityIntent = new Intent(MainActivity.this, CamActivity.class);
			    	startActivity(camActivityIntent);
				}
			}
		});
		
		btnSelectPicture.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this, ShowPicsActivity.class);
				startActivity(i);
			}
		});
		
		btnUploadPicture.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "To do: Photo upload", Toast.LENGTH_SHORT).show();
			}
		});		
	}

	/**
	 * Checking device has camera hardware or not
	 * */
	private boolean isDeviceSupportCamera() {
		if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Here we store the file url as it will be null after returning from camera app
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// save file url in bundle as it will be null on scren orientation changes
		outState.putParcelable("file_uri", fileUri);
	}
	/**
	 * Here we get back the file url as it will be null after returning from camera app
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		// get the file url 
		fileUri = savedInstanceState.getParcelable("file_uri");
	}
	
	/**
	 * Here we get back the file url as it will be null after returning from camera app
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SELECT_IMAGE_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				// successfully captured the image
				// display it in image view
				/*bitmap = BitmapFactory.decodeFile(tempFile.getAbsolutePath());
				EldPhotoApplication.setBitmap(bitmap);
				
				storeImage(bitmap);*/
				
				Intent dealWithPictureIntent = new Intent();
				dealWithPictureIntent.setClassName(PACKAGE_NAME, PACKAGE_NAME + ".DealWithPictureActivity");
				
				//removes previous filter images
        		DealWithPictureActivity.removeFilterImages();
        		DealWithPictureActivity.removeCurrentImage();
				
				startActivity(dealWithPictureIntent);
				
			} else if (resultCode == RESULT_CANCELED) {
				// user cancelled Image capture
				Toast.makeText(getApplicationContext(), "User cancelled image selection", Toast.LENGTH_SHORT).show();
			} else {
				// failed to capture image
				Toast.makeText(getApplicationContext(), "Sorry! Failed to select image", Toast.LENGTH_SHORT).show();
			}

		}
	}
	/**
	 * ------------ Helper Methods ----------------------
	 * */
	//Get a picture file with patch
	private static File getOutputMediaFile(int type) {

		// External sdcard location
		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), IMAGE_DIRECTORY_NAME);

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
						+ IMAGE_DIRECTORY_NAME + " directory");
				Toast.makeText(context, "Oops! Failed create "
						+ IMAGE_DIRECTORY_NAME + " directory", Toast.LENGTH_SHORT).show();
				return null;
			} else {
				Log.d(IMAGE_DIRECTORY_NAME, "Directory created");
				Toast.makeText(context, "Directory created", Toast.LENGTH_SHORT).show();
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
		File mediaFile;
		
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".png");
		} else {
			return null;
		}
		return mediaFile;
	}
	// Saving picture
	private void storeImage(Bitmap image) {
	    File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
	    if (pictureFile == null) {
	        Log.d(IMAGE_DIRECTORY_NAME, "Error creating media file, check storage permissions: ");
	        Toast.makeText(context, "Error creating media file, check storage permissions: ", Toast.LENGTH_SHORT).show();
	        return;
	    } 
	    try {
	        FileOutputStream fos = new FileOutputStream(pictureFile);
	        image.compress(Bitmap.CompressFormat.PNG, 90, fos);
	        fos.close();
	    } catch (FileNotFoundException e) {
	        Log.d(IMAGE_DIRECTORY_NAME, "File not found: " + e.getMessage());
	        Toast.makeText(context, "File not found: " + e.getMessage(), Toast.LENGTH_SHORT).show();
	    } catch (IOException e) {
	        Log.d(IMAGE_DIRECTORY_NAME, "Error accessing file: " + e.getMessage());
	        Toast.makeText(context, "Error accessing file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
	    }  
	}
}
