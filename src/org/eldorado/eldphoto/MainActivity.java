package org.eldorado.eldphoto;

import java.io.File;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
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
}
