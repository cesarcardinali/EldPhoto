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
	private byte[] picture; //the picture taken as a byte array
	private Bitmap image; //the picture taken as a bitmap
	private static Bitmap currentImage = null; //the image displayed (can be the original or a effect-applied one)
	private ViewFlipper viewFlipper; //the view which contains the graphical components to show the effects/filters options
	private float lastX; //the last X position of the screen touch event
	private static ArrayList<Bitmap> filterImages = new ArrayList(); //the list with the effects/filters thumbnail images
	private Context context = this;
	private EffectsFactory effectsFactory; //the effects factory object
	private static boolean isThereEffectApplied = false; //whether there is an effect/filter applied to the original image being displayed
	
	public DealWithPictureActivity() {
		
		super();
		effectsFactory = new EffectsFactory();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_deal_with_picture);

		//retrieves the picture data from the intent
		if (EldPhotoApplication.hasBitmap() == true) {
			image = EldPhotoApplication.getBitmap();
		}
		else {
			picture = EldPhotoApplication.getPicture();
			
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPurgeable = true; //allows the system to reclaim memory
			options.inInputShareable = true; //keeps a shallow reference to the data

			//converts the byte array into bitmap
			image = BitmapFactory.decodeByteArray(picture, 0, picture.length, options);
		}
		
		//if this is the first time the activity is created for that picture, ...
		if(currentImage == null)
			//saves the image as the current image
			currentImage = image.copy(image.getConfig(), true);
		
		//otherwise, if some effect/filter was applied, ...
		else if(isThereEffectApplied){
			
			//makes the undo button visible
			Button undoButton = (Button) findViewById(R.id.undoButton);
			undoButton.setVisibility(View.VISIBLE);
		}
		
		//displays the current image
		ImageView imageView = (ImageView) findViewById(R.id.imageView1);
		imageView.setAdjustViewBounds(true);
		imageView.setImageBitmap(currentImage);
		
		//sets the background of the flipper (where will be displayed the effects thumbnails) to be transparent
		viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper1);
		viewFlipper.setBackgroundColor(Color.argb(90, 255, 255, 255));
		
		//sends the context information to effectsFactory class
		effectsFactory.setContext(this);
		
		//displays the filters/effects options, when available
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
	
	/** Shows the effects/filters implementation available.
	 * It calls the implementation and apply them on thumbnails.
	 * If this is the first time it is being called with the same picture,
	 * the thumbnails are saved into a list so the next times, they will only
	 * be displayed.
	 */
	public void showFilters(){
		
		try{
			//checks for the available filters/effects (this must be called before 'getNumberOfEffectsAvailable()' method)
			EffectsFactory.getAvailableEffects();
		}
		catch(Exception ex){
			
			//if there was a problem, display its message
			Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
			//prints its stack trace for debugging
			ex.printStackTrace();
			//gets out of this method
			return;
		}
		
		//if this is the first time we show the filters thumbnails, they shall be processed
		if(filterImages.size() == 0){
			//for each of the available effects/filters...
			for(int id = 0; id < EffectsFactory.getNumberOfEffectsAvailable(); id++){

				try{
					//creates the graphical components to display the thumbnails 
					//the layout
					LinearLayout layout = new LinearLayout(this);
					layout.setOrientation(LinearLayout.VERTICAL);
					layout.setGravity(Gravity.CENTER);
					
					//the text view with the filter/effect's name
					TextView textView = new TextView(this);
					textView.setText(effectsFactory.getEffect(id).getName());
					textView.setGravity(Gravity.CENTER);
					
					//the image view with the thumbnail with the filter/effect applied
					ImageView filterView = new ImageView(this);
					filterView.setAdjustViewBounds(true);
					filterView.setMaxHeight(200);
					filterView.setMaxWidth(200);

					//gets the dimensions of the original image
					int width = image.getWidth();
					int height = image.getHeight();

					//computes the dimensions of the thumbnail according to the dimensions of the original image
					//such that the greatest dimension should equal 200 px
					//if the width is bigger than height, then width should be 200px
					if(width > height){
						
						//sets the height proportionally
						height = Math.round(height * 200.0f/width);
						width = 200;
					}
					//else, the height should be 200px
					else{
						//sets the width proportionally
						width = Math.round(width * 200.0f/height);
						height = 200;
					}
					
					//adds into the list 'filterImages' the thumbnail with the filter/effect applied and with he computed dimensions
					filterImages.add(effectsFactory.getEffect(id).applyEffect(Bitmap.createScaledBitmap(image, width, height, false)));

					//sets the image view's image bitmap to the thumbnail
					filterView.setImageBitmap(filterImages.get(id));

					layout.addView(textView); //puts the text view inside the layout
					layout.addView(filterView); //puts the image view inside the layout
					viewFlipper.addView(layout); //puts the layout inside the flipper view

					//sets the method that will be called when the image view with the thumbnail is clicked
					layout.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {

							try{
								//gets the image view with the current image
								ImageView imageView = (ImageView) findViewById(R.id.imageView1);

								//gets the name of the clicked filter/effect thumbnail
								LinearLayout layout = (LinearLayout) view;
								TextView text = (TextView) layout.getChildAt(0);
								String str = (String) text.getText();
								int id = 0;

								//gets the id corresponding to such filter/effect
								for(int i = 0; i < EffectsFactory.getNumberOfEffectsAvailable(); i++)
									if(str.compareTo(effectsFactory.getEffect(i).getName()) == 0){
										id = i;
										break;
									}

								//applies the filter/effect to the original image and makes it the current one
								currentImage = effectsFactory.getEffect(id).applyEffect(
										Bitmap.createScaledBitmap(image, imageView.getWidth(), imageView.getHeight(), false));

								//displays the new image with the filter/effect applied
								imageView.setImageBitmap(currentImage);

								//makes the undo button visible
								Button undoButton = (Button) findViewById(R.id.undoButton);
								undoButton.setVisibility(View.VISIBLE);
								
								//signalizes that an effect/filter was applied
								isThereEffectApplied = true;
							}
							catch(Exception ex){

								//if something goes wrong, displays the error message
								Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
							}
						}
					});
				}
				catch(Exception ex){
					//if something goes wrong, displays the error message
					Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		}
		//else, if this isn't the first time we compute the thumbnails for the filters/effects for that picture...
		else{
			
			//for each of the available filters/effects, ...
			for(int id = 0; id < EffectsFactory.getNumberOfEffectsAvailable(); id++){

				try{
					//creates the graphical components to display the thumbnails 
					//the layout
					LinearLayout layout = new LinearLayout(this);
					layout.setOrientation(LinearLayout.VERTICAL);
					layout.setGravity(Gravity.CENTER);

					//the text view with the filter/effect's name
					TextView textView = new TextView(this);
					textView.setText(effectsFactory.getEffect(id).getName());
					textView.setGravity(Gravity.CENTER);

					//the image view with the thumbnail with the filter/effect applied
					ImageView filterView = new ImageView(this);
					filterView.setAdjustViewBounds(true);
					filterView.setMaxHeight(200);
					filterView.setMaxWidth(200);

					//sets the image view's image bitmap to the thumbnail
					filterView.setImageBitmap(filterImages.get(id));

					layout.addView(textView); //puts the text view inside the layout
					layout.addView(filterView); //puts the image view inside the layout
					viewFlipper.addView(layout); //puts the layout inside the flipper view

					//sets the method that will be called when the image view with the thumbnail is clicked
					layout.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {

							try{
								//gets the image view with the current image
								ImageView imageView = (ImageView) findViewById(R.id.imageView1);

								//gets the name of the clicked filter/effect thumbnail
								LinearLayout layout = (LinearLayout) view;
								TextView text = (TextView) layout.getChildAt(0);
								String str = (String) text.getText();
								int id = 0;

								//gets the id corresponding to such filter/effect
								for(int i = 0; i < EffectsFactory.getNumberOfEffectsAvailable(); i++)
									if(str.compareTo(effectsFactory.getEffect(i).getName()) == 0){
										id = i;
										break;
									}

								//applies the filter/effect to the original image and makes it the current one
								currentImage = effectsFactory.getEffect(id).applyEffect(
										Bitmap.createScaledBitmap(image, imageView.getWidth(), imageView.getHeight(), false));

								//displays the new image with the filter/effect applied
								imageView.setImageBitmap(currentImage);

								//makes the undo button visible
								Button undoButton = (Button) findViewById(R.id.undoButton);
								undoButton.setVisibility(View.VISIBLE);
								
								//signalizes that an effect/filter was applied
								isThereEffectApplied = true;
							}
							catch(Exception ex){

								//if something goes wrong, displays the error message
								Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
							}
						}
					});
				}
				catch(Exception ex){
					//if something goes wrong, displays the error message
					Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		}
	}
	
	/** Removes the filter/effect from the preview picture.
	 * It sets the image view's bitmap to 'image' (the original picture taken).
	 * 
	 * @param view
	 */
	public void undoFilter(View view){
		
		//sets the 'currentImage' to the original one ('image')
		currentImage = image;
		//sets the image view's image picture to the original one
		ImageView imageView = (ImageView) findViewById(R.id.imageView1);
		imageView.setImageBitmap(currentImage);
		
		//makes the undo button invisible
		Button undoButton = (Button) findViewById(R.id.undoButton);
		undoButton.setVisibility(View.INVISIBLE);
		
		//signalizes that an effect/filter wasn't applied
		isThereEffectApplied = false;
	}
	
	/** Called when cancel button is clicked.
	 * It finalizes the activity.
	 * @param view
	 */
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
	
	/** Removes the current image by setting it to null.
	 * 
	 */
	public static void removeCurrentImage(){
		currentImage = null;
	}
}
