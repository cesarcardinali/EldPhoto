package org.eldorado.eldphoto;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
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
	private Bitmap image;
	private static Bitmap currentImage = null;
	private ViewFlipper viewFlipper;
	private float lastX;
	private static ArrayList<Bitmap> filterImages = new ArrayList();
	private Context context = this;
	private EffectsFactory effectsFactory;
	
	public DealWithPictureActivity() {
		
		super();
		effectsFactory = new EffectsFactory();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_deal_with_picture);

		//retrieves the picture data from the intent
		picture = EldPhotoApplication.getPicture();
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPurgeable = true; //allows the system to reclaim memory
		options.inInputShareable = true; //keeps a shallow reference to the data
		
		image = BitmapFactory.decodeByteArray(picture, 0, picture.length, options);
		if(currentImage == null)
			currentImage = image.copy(image.getConfig(), true);
		else{
			
			Button undoButton = (Button) findViewById(R.id.undoButton);
			undoButton.setVisibility(View.VISIBLE);
		}
		
		ImageView imageView = (ImageView) findViewById(R.id.imageView1);
		imageView.setAdjustViewBounds(true);
		imageView.setImageBitmap(currentImage);
		
		viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper1);
		viewFlipper.setBackgroundColor(Color.argb(90, 255, 255, 255));
		
		effectsFactory.setContext(this);
		
		showFilters();
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
	 */
	public void showFilters(){
		
		//for each effects available, ...
		try{
			
			EffectsFactory.getAvailableEffects();
		}
		catch(Exception ex){
			
			Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
			ex.printStackTrace();
			return;
		}
		
		if(filterImages.size() == 0){
			
			for(int id = 0; id < EffectsFactory.getNumberOfEffectsAvailable(); id++){

				try{
					LinearLayout layout = new LinearLayout(this);
					layout.setOrientation(LinearLayout.VERTICAL);
					layout.setGravity(Gravity.CENTER);

					TextView textView = new TextView(this);
					textView.setText(effectsFactory.getEffect(id).getName());
					textView.setGravity(Gravity.CENTER);

					ImageView filterView = new ImageView(this);
					filterView.setAdjustViewBounds(true);
					filterView.setMaxHeight(200);
					filterView.setMaxWidth(200);

					int width = image.getWidth();
					int height = image.getHeight();

					if(width > height){

						height = Math.round(height * 200.0f/width);
						width = 200;
					}
					else{
						width = Math.round(width * 200.0f/height);
						height = 200;
					}

					filterImages.add(effectsFactory.getEffect(id).applyEffect(Bitmap.createScaledBitmap(image, width, height, false)));

					filterView.setImageBitmap(filterImages.get(id));

					layout.addView(textView);
					layout.addView(filterView);
					viewFlipper.addView(layout);

					layout.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {

							try{
								ImageView imageView = (ImageView) findViewById(R.id.imageView1);
								LinearLayout layout = (LinearLayout) view;
								TextView text = (TextView) layout.getChildAt(0);
								String str = (String) text.getText();
								int id = 0;

								for(int i = 0; i < EffectsFactory.getNumberOfEffectsAvailable(); i++)
									if(str.compareTo(effectsFactory.getEffect(i).getName()) == 0){
										id = i;
										break;
									}

								currentImage = effectsFactory.getEffect(id).applyEffect(
										Bitmap.createScaledBitmap(image, imageView.getWidth(), imageView.getHeight(), false));

								imageView.setImageBitmap(currentImage);

								Button undoButton = (Button) findViewById(R.id.undoButton);
								undoButton.setVisibility(View.VISIBLE);
							}
							catch(Exception ex){

								Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
							}
						}
					});
				}
				catch(Exception ex){
					Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		}
		else{
			
			for(int id = 0; id < EffectsFactory.getNumberOfEffectsAvailable(); id++){

				try{
					LinearLayout layout = new LinearLayout(this);
					layout.setOrientation(LinearLayout.VERTICAL);
					layout.setGravity(Gravity.CENTER);

					TextView textView = new TextView(this);
					textView.setText(effectsFactory.getEffect(id).getName());
					textView.setGravity(Gravity.CENTER);

					ImageView filterView = new ImageView(this);
					filterView.setAdjustViewBounds(true);
					filterView.setMaxHeight(200);
					filterView.setMaxWidth(200);

					filterView.setImageBitmap(filterImages.get(id));

					layout.addView(textView);
					layout.addView(filterView);
					viewFlipper.addView(layout);

					layout.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {

							try{
								ImageView imageView = (ImageView) findViewById(R.id.imageView1);
								LinearLayout layout = (LinearLayout) view;
								TextView text = (TextView) layout.getChildAt(0);
								String str = (String) text.getText();
								int id = 0;

								for(int i = 0; i < EffectsFactory.getNumberOfEffectsAvailable(); i++)
									if(str.compareTo(effectsFactory.getEffect(i).getName()) == 0){
										id = i;
										break;
									}

								currentImage = effectsFactory.getEffect(id).applyEffect(
										Bitmap.createScaledBitmap(image, imageView.getWidth(), imageView.getHeight(), false));

								imageView.setImageBitmap(currentImage);

								Button undoButton = (Button) findViewById(R.id.undoButton);
								undoButton.setVisibility(View.VISIBLE);
							}
							catch(Exception ex){
								Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
							}
						}
					});
				}
				catch(Exception ex){
					Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		}
	}
	
	public void undoFilter(View view){
		
		currentImage = image;
		ImageView imageView = (ImageView) findViewById(R.id.imageView1);
		imageView.setImageBitmap(currentImage);
		
		Button undoButton = (Button) findViewById(R.id.undoButton);
		undoButton.setVisibility(View.INVISIBLE);
	}
	
	public void cancelPicture(View view){
		
		this.finish();
	}
	
	/** This method is to be written.
	 * It should send the picture to the class that will send it to the remote server.
	 * 
	 * @param view
	 */
	public void sendPicture(View view){}
	
	/** Removes all filter images previously computed.
	 * 
	 */
	public static void removeFilterImages(){
		
		filterImages.clear();
	}
	
	public static void removeCurrentImage(){
		currentImage = null;
	}
}
