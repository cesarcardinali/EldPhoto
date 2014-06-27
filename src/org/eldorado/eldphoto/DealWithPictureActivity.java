package org.eldorado.eldphoto;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ViewFlipper;

/** This class is the activity that shows the options to deal with the taken picture.
 * From here, the user may apply another filter or effect in the image, send it to the server or
 * cancel it and return to the camera preview activity.
 * 
 * @author phack
 *
 */
public class DealWithPictureActivity extends Activity {

	public static final String PICTURE = "org.eldorado.eldphoto.PICTURE";
	private byte[] picture;
	private ViewFlipper viewFlipper;
	private float lastX;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_deal_with_picture);

		//retrieves the picture data from the intent
		picture = EldPhotoApplication.getPicture();
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPurgeable = true; //allows the system to reclaim memory
		options.inInputShareable = true; //keeps a shallow reference to the data
		
		Bitmap image = BitmapFactory.decodeByteArray(picture, 0, picture.length, options);
		ImageView imageView = (ImageView) findViewById(R.id.imageView1);
		imageView.setAdjustViewBounds(true);
		imageView.setImageBitmap(image);
		
		ImageView filter1 = (ImageView) findViewById(R.id.imageFilter1);
		ImageView filter2 = (ImageView) findViewById(R.id.imageFilter2);
		ImageView filter3 = (ImageView) findViewById(R.id.imageFilter3);
		
		filter1.setAdjustViewBounds(true);
		filter2.setAdjustViewBounds(true);
		filter3.setAdjustViewBounds(true);
		
		filter1.setMaxHeight(200);
		filter2.setMaxHeight(200);
		filter3.setMaxHeight(200);
		
		filter1.setMaxWidth(200);
		filter2.setMaxWidth(200);
		filter3.setMaxWidth(200);
		
		filter1.setImageBitmap(image);
		filter2.setImageBitmap(image);
		filter3.setImageBitmap(image);
		
		viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper1);
		viewFlipper.setBackgroundColor(Color.argb(90, 255, 255, 255));
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		picture = null;
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
                    if (lastX < currentX) 
                    {
                         // If no more View/Child to flip
                        if (viewFlipper.getDisplayedChild() == 0)
                            break;
                        
                        // set the required Animation type to ViewFlipper
                        // The Next screen will come in from Left and current Screen will go OUT from Right 
                        viewFlipper.setInAnimation(this, R.anim.in_from_left);
                        viewFlipper.setOutAnimation(this, R.anim.out_to_right);
                        // Show the previous Screen
                        viewFlipper.showPrevious();
                    }
                    
                    // if right to left swipe on screen
                    if (lastX > currentX)
                    {
                        if (viewFlipper.getDisplayedChild() == viewFlipper.getChildCount() - 1)
                            break;
                        // set the required Animation type to ViewFlipper
                        // The Next screen will come in from Right and current Screen will go OUT from Left 
                        viewFlipper.setInAnimation(this, R.anim.in_from_right);
                        viewFlipper.setOutAnimation(this, R.anim.out_to_left);
                        // Show The Next Screen
                        viewFlipper.showNext();
                    }
                    break;
                }
        }
        return false;
	}
	
	/** This method is to be written.
	 * It should check if there is available filters/effects and display them.
	 * 
	 * @param view
	 */
	public void showFilters(View view){}
	
	/** This method is to be written.
	 * It should send the picture to the class that will send it to the remote server.
	 * 
	 * @param view
	 */
	public void sendPicture(View view){}
}
