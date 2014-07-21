package org.eldorado.eldphoto;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

public class MainActivity extends Activity {

	// Activity request codes
	public static final int MEDIA_TYPE_IMAGE = 1;
	private static final int SELECT_IMAGE_REQUEST_CODE = 100;
	
	public static final String PACKAGE_NAME = "org.eldorado.eldphoto";

	// directory name to store captured images and videos
	private static final String IMAGE_DIRECTORY_NAME = "Hello Camera";

	private Uri fileUri; // file url to store image/video
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

		/**
		 * Capture image button click event
		 */
		String storagePatch = Environment.getExternalStorageDirectory().getAbsolutePath();
		tempFile = new File(storagePatch + "/Eldphoto/a.jpg");
				
		btnSelectPicture = (Button) findViewById(R.id.btnSelectPicture);
		btnCapturePicture = (Button) findViewById(R.id.btnCapturePicture);
		
		btnCapturePicture.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// capture picture
				captureImage();
			}
		});
		
		btnSelectPicture.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// selectImage();

				Intent i = new Intent(MainActivity.this, ShowPicsActivity.class);
				startActivity(i);
				
				/*File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Eldphoto");
				allFiles = folder.listFiles();
				FilePathStrings = new String[allFiles.length];
	            FileNameStrings = new String[allFiles.length];
				for (int i = 0; i < allFiles.length; i++) {
	                // Get the path of the image file
	                FilePathStrings[i] = allFiles[i].getAbsolutePath();
	                // Get the name image file
	                FileNameStrings[i] = allFiles[i].getName();
	            }
				adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, FileNameStrings);*/
				
				
				/*Toast.makeText(getApplicationContext(),"" + folder.list().length + "\n" + folder.getAbsolutePath()+ "\n" + allFiles.length,Toast.LENGTH_LONG).show();
				for (int i = 0; i < allFiles.length; i++){
					if(allFiles[i].isFile()){
						Toast.makeText(getApplicationContext(),"Name: " + allFiles[i].getName().toString(),Toast.LENGTH_LONG).show();
					}
				}*/
			}
		});
	

		// Checking camera availability
		if (!isDeviceSupportCamera()) {
			Toast.makeText(getApplicationContext(),
					"Sorry! Your device doesn't support camera",
					Toast.LENGTH_LONG).show();
			// will close the app if the device does't have camera
			finish();
		}
		
		btnUploadPicture = (Button) findViewById(R.id.btnUploadPicture);
	}

	/**
	 * Checking device has camera hardware or not
	 * */
	private boolean isDeviceSupportCamera() {
		if (getApplicationContext().getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			// this device has a camera
			return true;
		} else {
			// no camera on this device
			return false;
		}
	}

	/**
	 * Capturing Camera Image will start CamActivity, which will display the camera preview
	 */
	private void captureImage() {
		
		Intent camActivityIntent = new Intent(this, CamActivity.class);
    	startActivity(camActivityIntent);
	}

	/**
	 * Here we store the file url as it will be null after returning from camera
	 * app
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// save file url in bundle as it will be null on scren orientation
		// changes
		outState.putParcelable("file_uri", fileUri);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		// get the file url 
		fileUri = savedInstanceState.getParcelable("file_uri");
	}

	/**
	 * ------------ Helper Methods ----------------------
	 * */

	/**
	 * Creating file uri to store image/video
	 */
	public Uri getOutputMediaFileUri(int type) {
		return Uri.fromFile(getOutputMediaFile(type));
	}

	/**
	 * returning image / video
	 */
	private static File getOutputMediaFile(int type) {

		// External sdcard location
		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				IMAGE_DIRECTORY_NAME);

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
						+ IMAGE_DIRECTORY_NAME + " directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
				Locale.getDefault()).format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".jpg");
		} else {
			return null;
		}

		return mediaFile;
	}
	
	private void selectImage() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI,
				"image/*");
		intent.setType("image/*");
		intent.putExtra("output", Uri.fromFile(tempFile));
		intent.putExtra("crop", "true");
		startActivityForResult(intent, SELECT_IMAGE_REQUEST_CODE);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == SELECT_IMAGE_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				// successfully captured the image
				// display it in image view
				
				bitmap = BitmapFactory.decodeFile(tempFile.getAbsolutePath());
				EldPhotoApplication.setBitmap(bitmap);
				
				Intent dealWithPictureIntent = new Intent();
				dealWithPictureIntent.setClassName(PACKAGE_NAME, PACKAGE_NAME + ".DealWithPictureActivity");
				
				//removes previous filter images
        		DealWithPictureActivity.removeFilterImages();
        		DealWithPictureActivity.removeCurrentImage();
				
				startActivity(dealWithPictureIntent);
				
			} else if (resultCode == RESULT_CANCELED) {
				// user cancelled Image capture
				Toast.makeText(getApplicationContext(),
						"User cancelled image selection", Toast.LENGTH_SHORT)
						.show();
			} else {
				// failed to capture image
				Toast.makeText(getApplicationContext(),
						"Sorry! Failed to select image", Toast.LENGTH_SHORT)
						.show();
			}

		}
	}
}
