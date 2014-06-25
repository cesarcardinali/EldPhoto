package org.eldorado.eldphoto;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnHoverListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;


/** This class is the activity which shows the camera preview.
 * It also shows the 'native' filter options, offers the control to switch the camera
 * and takes the picture.
 * 
 * @author phack
 *
 */
public class CamActivity extends Activity{// implements OnHoverListener{ // implements OnClickListener{
	
	public static final String PACKAGE_NAME = "org.eldorado.eldphoto";
	public static final String ACTION_DEAL_WITH_PICTURE = PACKAGE_NAME + ".DEAL_WITH_PICTURE";
	public static final String PICTURE = PACKAGE_NAME + ".PICTURE";
	private CamPreview preview;
	private ListView listView;
	private TextView textView;
	private OrientationEventListener orientationListener;
	private OnItemClickListener clickListener;
	private int rotation;
	private float lastX;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cam);
		
		try{			
			orientationListener = new OrientationEventListener(this) {
				
				@Override
				public void onOrientationChanged(int orientation) {
					
					rotation = orientation;
				}
			};
			
			if(preview == null)
				preview = new CamPreview(this);
			
			FrameLayout frame = (FrameLayout) findViewById(R.id.camera_preview);
			//preview.setOnClickListener(this);
			frame.addView(preview);
			listView = (ListView) findViewById(R.id.listView);
			clickListener = new OnItemClickListener() {
			    public void onItemClick(AdapterView parent, View v, int position, long id) {
			        
			    	Camera.Parameters params = preview.getCam().getParameters();
					List<String> items = params.getSupportedColorEffects();
					params.setColorEffect(items.get(position));
			    	preview.getCam().setParameters(params);
			    	listView.setVisibility(View.INVISIBLE);
			    	textView.setVisibility(View.INVISIBLE);
			    }
			};
			
			listView.setOnItemClickListener(clickListener);
			
			textView = (TextView) findViewById(R.id.text_choosing_filters);
			textView.setBackgroundColor(Color.WHITE);
			//setList();
        }
        catch(RuntimeException ex){
        	
        	ex.printStackTrace();
        }
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		switch (event.getAction())
        {
               // when user first touches the screen to swap
                case MotionEvent.ACTION_DOWN: 
                {
                    lastX = event.getX();
                    break;
               }
                case MotionEvent.ACTION_UP: 
                {
                    float currentX = event.getX();
                    
                    // if left to right swipe on screen
                    if (lastX < currentX - 50) 
                    {
                        setList();
                    	listView.setVisibility(View.VISIBLE);
                        textView.setVisibility(View.VISIBLE);
                    }
                    else if(lastX > currentX + 50){
                    	
                    	preview.switchCamera();
                    }
                    
                    else if (lastX == currentX){
                    	
                    	try{
                			
                			//CamOps.setCameraDisplayOrientation(this, 0, preview.getCam());
                			CamOps.setPictureOrientation(this, preview.getCamID(), preview.getCam());
                			preview.getCam().takePicture(null, null, new PictureCallback() {
                				
                				@Override
                				public void onPictureTaken(byte[] data, Camera camera) {
                					// TODO Auto-generated method stub
                					if(data != null){
                						Intent dealWithPictureIntent = new Intent();
                						dealWithPictureIntent.setClassName(PACKAGE_NAME, PACKAGE_NAME + ".DealWithPictureActivity");
                						EldPhotoApplication.setPicture(data); //sends the picture data to the application class
                						startActivity(dealWithPictureIntent);
                					}
                				}
                			});
                		}
                		catch(Exception e){
                			
                			e.printStackTrace();
                		}
                		finally{
                			//orientationListener.disable();
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
	
	public OrientationEventListener getOrientationListener(){ return orientationListener;}
	public int getRotation(){ return rotation;}
	public CamPreview getPreview(){ return preview;}
	public void removeOrientationListener(){ orientationListener = null;}
	
	private void setList(){
		
		Camera.Parameters params = preview.getCam().getParameters();
		List<String> items = params.getSupportedColorEffects();
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
		listView.setAdapter(null);
		listView.setAdapter(adapter);
		listView.setBackgroundColor(Color.argb(80, 255, 255, 255));
	}
}
