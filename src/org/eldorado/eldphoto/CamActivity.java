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
			
			final FrameLayout frame = (FrameLayout) findViewById(R.id.camera_preview);
			//CamOps.setPictureOrientation(this, preview.getCamID(),preview.getCam());
			frame.addView(preview);
			//Fixing screen proportion
			new Handler().postDelayed(new Runnable() {
		        @Override
		        public void run() {
		        	int width = frame.getWidth();
					int height = frame.getHeight();
					Camera.Parameters params = preview.getCam().getParameters();
					//Toast.makeText(context, "1" + height + "" + width, Toast.LENGTH_SHORT).show();
					int newHeight = width * params.getPreviewSize().width/params.getPreviewSize().height;
					int pad = height - newHeight;
					//Toast.makeText(context, "2" + height + "" + width, Toast.LENGTH_SHORT).show();
					frame.setPadding(0, pad/2, 0, pad/2);
		        }
		    },30);
			

			/*listView = (ListView) findViewById(R.id.listView);
			clickListener = new OnItemClickListener() {
			    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			    	Camera.Parameters params = preview.getCam().getParameters();
					List<String> items = params.getSupportedColorEffects();
					List<Size> size = params.getSupportedPreviewSizes();
		            params.setPreviewSize(size.get(0).width, size.get(0).height);
					params.setColorEffect(items.get(position));
			    	preview.getCam().setParameters(params);
			    	
			    	//listView.setVisibility(View.INVISIBLE);
			    	textView.setVisibility(View.INVISIBLE);
			    }
			};
			
			//listView.setOnItemClickListener(clickListener);
			textView = (TextView) findViewById(R.id.text_choosing_filters);
			textView.setBackgroundColor(Color.WHITE);*/
        }
        catch(RuntimeException ex){
        	ex.printStackTrace();
        }
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
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
				Toast.makeText(context, "Orientation Prev: " + rotation, Toast.LENGTH_LONG).show();
				CamOps.setPictureSize(preview.getCam());
			} else if (lastX > currentX + 50) {
				preview.switchCamera();
				final FrameLayout frame = (FrameLayout) findViewById(R.id.camera_preview);
				//CamOps.setPictureOrientation(this, preview.getCamID(),preview.getCam());
				new Handler().postDelayed(new Runnable() {
			        @Override
			        public void run() {
			        	int width = frame.getWidth();
						int height = frame.getHeight();
						Camera.Parameters params = preview.getCam().getParameters();
						//Toast.makeText(context, "1" + height + "" + width, Toast.LENGTH_SHORT).show();
						int newHeight = width * params.getPreviewSize().width/params.getPreviewSize().height;
						int pad = height - newHeight;
						//Toast.makeText(context, "2" + height + "" + width, Toast.LENGTH_SHORT).show();
						frame.setPadding(0, pad/2, 0, pad/2);
			        }
			    },30);
			}

			else if ((lastX <= currentX + 5) && (lastX >= currentX - 5)) {
				try {
					// removes previous filter images
					DealWithPictureActivity.removeFilterImages();
					DealWithPictureActivity.removeCurrentImage();
					final FrameLayout frame = (FrameLayout) findViewById(R.id.camera_preview);
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
		this.preview = null; 
	}
	
	@Override
	public void onBackPressed() {
		finish();
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
