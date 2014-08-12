package org.eldorado.eldphoto;

import org.eldorado.eldphoto.support.CamOps;
import org.eldorado.eldphoto.support.CamPreview;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.widget.FrameLayout;
import android.widget.Toast;


/** This class is the activity which shows the camera preview.
 * It also shows the 'native' filter options, offers the control to switch the camera
 * and takes the picture.
 * 
 * @author phack
 *
 */
@SuppressLint("NewApi")
public class CamActivity extends Activity{// implements OnHoverListener{ // implements OnClickListener{
	
	public static final String PACKAGE_NAME = "org.eldorado.eldphoto";
	public static final String ACTION_DEAL_WITH_PICTURE = PACKAGE_NAME + ".DEAL_WITH_PICTURE";
	public static final String PICTURE = PACKAGE_NAME + ".PICTURE";
	private CamPreview preview;
	//private ListView listView;
	//private TextView textView;
	private OrientationEventListener orientationListener;
	//private OnItemClickListener clickListener;
	private int rotation;
	private float lastX;
	int width, height;
	Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cam);
		context = this;
		try{
			orientationListener = new OrientationEventListener(this) {
				@Override
				public void onOrientationChanged(int orientation) {
					rotation = orientation;
				}
			};
			
			if(preview == null)
				preview = new CamPreview(this);
			
			CamOps.setPictureSize(preview.getCam(),56);
			final FrameLayout frame = (FrameLayout) findViewById(R.id.camera_preview);
			frame.addView(preview);
			//Fixing screen proportion
			new Handler().postDelayed(new Runnable() {
		        @Override
		        public void run() {
		        	int height = frame.getWidth();
					int width = frame.getHeight();
					Camera.Parameters params = preview.getCam().getParameters();
					int Cwidth = params.getPreviewSize().width;
					int Cheight = params.getPreviewSize().height;
					int newWidth = width*Cheight/Cwidth;
					int pad = height - newWidth;
					frame.setPadding(pad/2, 0, pad/2, 0);
					Log.d("CAMOPS", "W: " + width + " - H: " + height + " ||- cW: " + Cwidth + " - cH: " + Cheight);
					Log.d("CAMOPS", "newH: " + newWidth);
		        }
		    },30);
        }
        catch(RuntimeException ex){
        	ex.printStackTrace();
        }
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final FrameLayout frame = (FrameLayout) findViewById(R.id.camera_preview);
		int width = frame.getWidth();
		int height = frame.getHeight();
		int ratio = width*100/height;
		switch (event.getAction()) {
		// when user first touches the screen to swap
		case MotionEvent.ACTION_DOWN: {
			lastX = event.getX();
			break;
		}
		case MotionEvent.ACTION_UP: {
			float currentX = event.getX();
			// if left to right swipe on screen
			if (lastX < currentX - 50) {
				CamOps.setPictureSize(preview.getCam(), 56);
				new Handler().postDelayed(new Runnable() {
			        @Override
			        public void run() {
			        	int height = frame.getWidth();
						int width = frame.getHeight();
						Camera.Parameters params = preview.getCam().getParameters();
						int Cwidth = params.getPreviewSize().width;
						int Cheight = params.getPreviewSize().height;
						int newWidth = width*Cheight/Cwidth;
						int pad = height - newWidth;
						frame.setPadding(pad/2, 0, pad/2, 0);
						Log.d("CAMOPS", "W: " + width + " - H: " + height + " ||- cW: " + Cwidth + " - cH: " + Cheight);
						Log.d("CAMOPS", "newH: " + newWidth);
			        }
			    },30);
			} else if (lastX > currentX + 50) {
				preview.switchCamera();//cam_id 1 e 0
				CamOps.setPictureSize(preview.getCam(),56);
				//CamOps.setPictureOrientation(this, preview.getCamID(),preview.getCam());
				new Handler().postDelayed(new Runnable() {
			        @Override
			        public void run() {
			        	int height = frame.getWidth();
						int width = frame.getHeight();
						Camera.Parameters params = preview.getCam().getParameters();
						int Cwidth = params.getPreviewSize().width;
						int Cheight = params.getPreviewSize().height;
						int newWidth = width*Cheight/Cwidth;
						int pad = height - newWidth;
						frame.setPadding(pad/2, 0, pad/2, 0);
						Log.d("CAMOPS", "Switch Camera!");
						Log.d("CAMOPS", "W: " + width + " - H: " + height + " ||- cW: " + Cwidth + " - cH: " + Cheight);
						Log.d("CAMOPS", "newW: " + newWidth);
			        }
			    },100);
			}

			else if ((lastX <= currentX + 5) && (lastX >= currentX - 5)) {
				try {
					// removes previous filter images
					DealWithPictureActivity.removeFilterImages();
					DealWithPictureActivity.removeCurrentImage();
					// sets the orientation of the picture
					//CamOps.setPictureOrientation(this, preview.getCamID(),preview.getCam());
					preview.getCam().takePicture(null, null,new PictureCallback() {
						@Override
						public void onPictureTaken(byte[] data,Camera camera) {
							if (data != null) {
								Intent dealWithPictureIntent = new Intent();
								dealWithPictureIntent.setClassName(PACKAGE_NAME,PACKAGE_NAME + ".DealWithPictureActivity");
								dealWithPictureIntent.putExtra("orientation", rotation);
								dealWithPictureIntent.putExtra("w", frame.getWidth());
								EldPhotoApplication.setPicture(data); // sends the picture data to the application class
								startActivity(dealWithPictureIntent);
							}
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					orientationListener.disable();
				}
			}
		}
		}
		return false;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(preview != null)
			this.preview = null; 
	}
	
	@Override
	public void onBackPressed() {
		if(preview != null)
			this.preview = null;
		this.finish();
		System.exit(0);
	}
	
	
	// Getter and Setters
	public OrientationEventListener getOrientationListener() {
		return orientationListener;
	}

	public int getRotation() {
		return rotation;
	}

	public CamPreview getPreview() {
		return preview;
	}

	public void removeOrientationListener() {
		orientationListener = null;
	}
}
