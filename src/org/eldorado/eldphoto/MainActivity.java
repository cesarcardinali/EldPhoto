package org.eldorado.eldphoto;

import org.eldorado.eldphoto.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	// Activity request codes
	private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
	public static final int MEDIA_TYPE_IMAGE = 1;

	// directory name to store captured images and videos
	private static final String IMAGE_DIRECTORY_NAME = "Hello Camera";

	private Uri fileUri; // file url to store image/video

	private ImageView imgPreview;
	private Button btnCapturePicture;
	private TextView f1, f2, f3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		imgPreview = (ImageView) findViewById(R.id.imgPreview);
		btnCapturePicture = (Button) findViewById(R.id.btnCapturePicture);
		f1 = (TextView) findViewById(R.id.f1);
		f2 = (TextView) findViewById(R.id.f2);
		f3 = (TextView) findViewById(R.id.f3);

		/**
		 * Capture image button click event
		 */
		btnCapturePicture.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// capture picture
				captureImage();
			}
		});

		f1.setOnClickListener(new TextView.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (f1.getTextSize() == 45.0) {
					f1.setBackgroundColor(0xFF000000);
					f1.setTextColor(0xFFFFFFFF);
					f1.setTextSize((float) 32.0);
					f2.setBackgroundColor(0xFFFFFFFF);
					f2.setTextColor(0xFF000000);
					f2.setTextSize((float) 30.0);
					f3.setBackgroundColor(0xFFFFFFFF);
					f3.setTextColor(0xFF000000);
					f3.setTextSize((float) 30.0);
				} else {
					f1.setBackgroundColor(0xFFFFFFFF);
					f1.setTextColor(0xFF000000);
					f1.setTextSize((float) 30.0);
				}
			}
		});

		f2.setOnClickListener(new TextView.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (f2.getTextSize() == 45.0) {
					f2.setBackgroundColor(0xFF000000);
					f2.setTextColor(0xFFFFFFFF);
					f2.setTextSize((float) 32.0);
					f3.setBackgroundColor(0xFFFFFFFF);
					f3.setTextColor(0xFF000000);
					f3.setTextSize((float) 30.0);
					f1.setBackgroundColor(0xFFFFFFFF);
					f1.setTextColor(0xFF000000);
					f1.setTextSize((float) 30.0);
				} else {
					f2.setBackgroundColor(0xFFFFFFFF);
					f2.setTextColor(0xFF000000);
					f2.setTextSize((float) 30.0);
				}
			}
		});

		f3.setOnClickListener(new TextView.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (f3.getTextSize() == 45.0) {
					f3.setBackgroundColor(0xFF000000);
					f3.setTextColor(0xFFFFFFFF);
					f3.setTextSize((float) 32.0);
					f2.setBackgroundColor(0xFFFFFFFF);
					f2.setTextColor(0xFF000000);
					f2.setTextSize((float) 30.0);
					f1.setBackgroundColor(0xFFFFFFFF);
					f1.setTextColor(0xFF000000);
					f1.setTextSize((float) 30.0);
				} else {
					f3.setBackgroundColor(0xFFFFFFFF);
					f3.setTextColor(0xFF000000);
					f3.setTextSize((float) 30.0);
				}
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
	 * Capturing Camera Image will lauch camera app requrest image capture
	 */
	private void captureImage() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

		intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

		// start the image capture Intent
		startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
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
	 * Receiving activity result method will be called after closing the camera
	 * */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// if the result is capturing Image
		if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				// successfully captured the image
				// display it in image view
				previewCapturedImage();
			} else if (resultCode == RESULT_CANCELED) {
				// user cancelled Image capture
				Toast.makeText(getApplicationContext(),
						"User cancelled image capture", Toast.LENGTH_SHORT)
						.show();
			} else {
				// failed to capture image
				Toast.makeText(getApplicationContext(),
						"Sorry! Failed to capture image", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	/**
	 * Display image from a path to ImageView
	 */
	private void previewCapturedImage() {
		try {
			imgPreview.setVisibility(View.VISIBLE);

			// bimatp factory
			BitmapFactory.Options options = new BitmapFactory.Options();

			// downsizing image as it throws OutOfMemory Exception for larger
			// images
			options.inSampleSize = 2;

			final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
					options);

			imgPreview.setImageBitmap(bitmap);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
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
}
