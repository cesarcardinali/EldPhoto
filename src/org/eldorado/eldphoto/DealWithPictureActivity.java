package org.eldorado.eldphoto;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.eldorado.eldphoto.support.EffectsFactory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
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

	public  static final String PICTURE = "org.eldorado.eldphoto.PICTURE";
	private byte[] picture; //the picture taken as a byte array
	private Bitmap image; //the picture taken as a bitmap
	private Bitmap previewImage;
	private Bitmap storeImage;
	private static Bitmap currentImage = null; //the image displayed (can be the original or a effect-applied one)
	
	private ViewFlipper viewFlipper; //the view which contains the graphical components to show the effects/filters options
	
	private float lastX; //the last X position of the screen touch event
	private static ArrayList<Bitmap> filterImages = new ArrayList<Bitmap>(); //the list with the effects/filters thumbnail images

	private EffectsFactory effectsFactory; //the effects factory object
	private static boolean isThereEffectApplied = false; //whether there is an effect/filter applied to the original image being displayed
	
	private Context context = this;
	
	public DealWithPictureActivity() {
		
		super();
		effectsFactory = new EffectsFactory();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_deal_with_picture);
		
		clearImages();
		
		int orientation = -1, newOr = -1;
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
		    orientation = extras.getInt("orientation");
		    
		    if(orientation >= 315 || orientation < 45){
		    	newOr = 0;
			}
			else if(orientation >= 45 && orientation < 135){
				newOr = 90;
			}
			else if(orientation >= 135 && orientation < 225){
				newOr = 180;
			}
			else if(orientation >= 225 && orientation < 315){
				newOr = 270;
			}
		}

		//retrieves the picture data from the intent
		if (EldPhotoApplication.hasBitmap() == true) {
			Toast.makeText(context, "sfdgfdsg", Toast.LENGTH_LONG).show();
			image = EldPhotoApplication.getBitmap();
			/*Matrix matrix = new Matrix();
			orientation = newOr + 90;
			matrix.postRotate(orientation);
			image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);*/
		}
		else {
			picture = EldPhotoApplication.getPicture();
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPurgeable = true; //allows the system to reclaim memory
			options.inInputShareable = true; //keeps a shallow reference to the data
			//converts the byte array into bitmap
			image = BitmapFactory.decodeByteArray(picture, 0, picture.length, options);
			//EldPhotoApplication.setBitmap(image);
			Matrix matrix = new Matrix();
			orientation = newOr + 90;
			matrix.postRotate(orientation);
			image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
		}
		
		storeImage = image.copy(image.getConfig(), true);
		//storeImage = image;
		
		Button sendButton = (Button) findViewById(R.id.Button2);		
		sendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				storeImage(getApplicationContext(), storeImage);
			}
		});
		
		//displays the current image
		ImageView imageView = (ImageView) findViewById(R.id.imageView1);
		imageView.setAdjustViewBounds(true);

		int W =  extras.getInt("w");
		previewImage = Bitmap.createScaledBitmap(image, W, Math.round((float)(W/(float)image.getWidth())*(float)image.getHeight()), true);
		
		//if this is the first time the activity is created for that picture, ...
		if(currentImage == null)
			//saves the image as the current image
			currentImage = previewImage.copy(previewImage.getConfig(), true);
			//currentImage = previewImage;
		//otherwise, if some effect/filter was applied, ...
		else if(isThereEffectApplied){
			//makes the undo button visible
			Button undoButton = (Button) findViewById(R.id.undoButton);
			undoButton.setVisibility(View.VISIBLE);
		}
		
		imageView.setImageBitmap(currentImage);
				
		//sets the background of the flipper (where will be displayed the effects thumbnails) to be transparent
		viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper1);
		
		//sends the context information to effectsFactory class
		effectsFactory.setContext(context);
		
		//displays the filters/effects options, when available
		showFilters();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		picture = null;
		clearImages();
	}
	
	
	/** Shows the effects/filters implementation available.
	 * It calls the implementation and apply them on thumbnails.
	 * If this is the first time it is being called with the same picture,
	 * the thumbnails are saved into a list so the next times, they will only
	 * be displayed.
	 */
	@SuppressLint("NewApi")
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
					//creates the graphical components to display the thumbnails the layout
					LinearLayout layout = new LinearLayout(this);
					layout.setOrientation(LinearLayout.VERTICAL);
					layout.setGravity(Gravity.CENTER);
					
					//the text view with the filter/effect's name
					TextView textView = new TextView(context);
					textView.setText(effectsFactory.getEffect(id).getName());
					textView.setGravity(Gravity.CENTER);
					
					//the image view with the thumbnail with the filter/effect applied
					ImageView filterView = new ImageView(context);
					filterView.setAdjustViewBounds(false);
					filterView.setMaxHeight(80);
					filterView.setMaxWidth(80);

					//gets the dimensions of the original image
					int width = image.getWidth();
					int height = image.getHeight();

					//computes the dimensions of the thumbnail according to the dimensions of the original image
					if(width > height){
						//sets the height proportionally
						height = Math.round(height * 100.0f/width);
						width = 100;
					}
					//else, the height should be 200px
					else{
						//sets the width proportionally
						width = Math.round(width * 100.0f/height);
						height = 100;
					}
					
					//adds into the list 'filterImages' the thumbnail with the filter/effect applied and with he computed dimensions
					filterImages.add(effectsFactory.getEffect(id).applyEffect(Bitmap.createScaledBitmap(previewImage, width, height, false)));

					//sets the image view's image bitmap to the thumbnail
					filterView.setImageBitmap(filterImages.get(id));

					layout.addView(textView); //puts the text view inside the layout
					layout.addView(filterView); //puts the image view inside the layout
					
					//sets the method that will be called when the image view with the thumbnail is clicked
					layout.setOnTouchListener(new View.OnTouchListener() {					
						@Override
						public boolean onTouch(View v, MotionEvent event) {
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
				                        viewFlipper.setInAnimation(context, R.anim.in_from_left);
				                        viewFlipper.setOutAnimation(context, R.anim.out_to_right);
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
				                        viewFlipper.setInAnimation(context, R.anim.in_from_right);
				                        viewFlipper.setOutAnimation(context, R.anim.out_to_left);
				                        // Show The Next Screen
				                        viewFlipper.showNext();
				                    }
				                    if(currentX < lastX+5 && currentX > lastX-5){
										try{
											/*
											//gets the image view with the current image
											ImageView imageView = (ImageView) findViewById(R.id.imageView1);
											//gets the name of the clicked filter/effect thumbnail
											LinearLayout layout = (LinearLayout) v;
											TextView text = (TextView) layout.getChildAt(0);
											String str = (String) text.getText();
											int id = 0;
	
											//gets the id corresponding to such filter/effect
											for(int i = 0; i < EffectsFactory.getNumberOfEffectsAvailable(); i++)
											{
												if(str.compareTo(effectsFactory.getEffect(i).getName()) == 0){
													id = i;
													break;
												}
											}
											//applies the filter/effect to the original image and makes it the current one
											currentImage = effectsFactory.getEffect(id).applyEffect(previewImage);
											//storeImage = effectsFactory.getEffect(id).applyEffect(image);
											if(storeImage.getHeight() > 2048){
												storeImage = effectsFactory.getEffect(id).applyEffect(Bitmap.createScaledBitmap(image, (image.getWidth()*2048)/image.getHeight(), 2048, false));
											}			
											else if(storeImage.getWidth() > 2048){
												storeImage = effectsFactory.getEffect(id).applyEffect(Bitmap.createScaledBitmap(image, 2048, (image.getHeight()*2048/image.getWidth()), false));
											} else
												storeImage = effectsFactory.getEffect(id).applyEffect(image);
	
											//displays the new image with the filter/effect applied
											imageView.setImageBitmap(currentImage);
	
											//makes the undo button visible
											Button undoButton = (Button) findViewById(R.id.undoButton);
											undoButton.setVisibility(View.VISIBLE);
											
											//signalizes that an effect/filter was applied
											isThereEffectApplied = true;*/
											applyFilter(v);
										}
										catch(Exception ex){
	
											//if something goes wrong, displays the error message
											Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
										}
									}
				                    break;
				                }
					        }
							return true;
						}
					});
					viewFlipper.addView(layout); //puts the layout inside the flipper view
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
					filterView.setAdjustViewBounds(false);
					filterView.setMaxHeight(80);
					filterView.setMaxWidth(80);

					//sets the image view's image bitmap to the thumbnail
					filterView.setImageBitmap(filterImages.get(id));

					layout.addView(textView); //puts the text view inside the layout
					layout.addView(filterView); //puts the image view inside the layout
					//sets the method that will be called when the image view with the thumbnail is clicked
					layout.setOnTouchListener(new View.OnTouchListener() {					
						@Override
						public boolean onTouch(View v, MotionEvent event) {
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
				                        viewFlipper.setInAnimation(context, R.anim.in_from_left);
				                        viewFlipper.setOutAnimation(context, R.anim.out_to_right);
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
				                        viewFlipper.setInAnimation(context, R.anim.in_from_right);
				                        viewFlipper.setOutAnimation(context, R.anim.out_to_left);
				                        // Show The Next Screen
				                        viewFlipper.showNext();
				                    }
				                    if(currentX < lastX+5 && currentX > lastX-5){
										try{
											applyFilter(v);
										}
										catch(Exception ex){
											//if something goes wrong, displays the error message
											Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
										}
									}
				                    break;
				                }
					        }
							return true;
						}
					});
					viewFlipper.addView(layout); //puts the layout inside the flipper view
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
		currentImage = previewImage.copy(previewImage.getConfig(), true);
		//storeImage = image.copy(image.getConfig(), true);
		
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
		clearImages();
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
	
	private void clearImages(){
		image = null;
		currentImage = null;
		previewImage = null;
		storeImage = null;
	}
	
	//Get a picture file with patch
	private static File getOutputMediaFile(Context context, int Type) {
		String IMAGE_DIRECTORY_NAME = "Eldphoto";
		String THUMBNAIL_DIRECTORY_NAME = "Eldphoto/Thumbnails";
		File mediaStorageDir = null;
		// External sdcard location (Fails, it detects the internal memory)
		if (Type == 1)
			mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + IMAGE_DIRECTORY_NAME);
		if (Type == 2) 
			mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + THUMBNAIL_DIRECTORY_NAME);
		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				//Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create " + IMAGE_DIRECTORY_NAME + " directory");
				Toast.makeText(context, "Oops! Failed create " + IMAGE_DIRECTORY_NAME + " directory", Toast.LENGTH_SHORT).show();
				return null;
			} else {
				//Log.d(IMAGE_DIRECTORY_NAME, "Directory created");
				Toast.makeText(context, "Directory created", Toast.LENGTH_SHORT).show();
			}
		}
		// Create a media file name
		String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss", Locale.getDefault()).format(new Date());
		File mediaFile;
		mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".png");
		return mediaFile;
	}
	
	// Saving picture
	private void storeImage(Context context, Bitmap image) {
		File pictureFile = getOutputMediaFile(this.context, 1);
		File thumbnailFile = getOutputMediaFile(this.context, 2);
	    if (pictureFile == null) {
	        //Log.d(IMAGE_DIRECTORY_NAME, "Error creating media file, check storage permissions: ");
	        Toast.makeText(context, "Error creating media file, check storage permissions: ", Toast.LENGTH_SHORT).show();
	        return;
	    }
	    try {
	    	FileOutputStream fos1 = new FileOutputStream(pictureFile);
	    	FileOutputStream fos2 = new FileOutputStream(thumbnailFile);
	        image.compress(Bitmap.CompressFormat.PNG, 0, fos1);
	        Bitmap aux = ThumbnailUtils.extractThumbnail(image, 80, 80);
	        fos1.close();
	        aux.compress(Bitmap.CompressFormat.PNG, 0, fos2);
	        fos2.close();
	        Toast.makeText(context, "Media stored and Thumbnail created", Toast.LENGTH_SHORT).show();
	    } catch (FileNotFoundException e) {
	        //Log.d(IMAGE_DIRECTORY_NAME, "File not found: " + e.getMessage());
	        Toast.makeText(context, "File not found: " + e.getMessage(), Toast.LENGTH_SHORT).show();
	    } catch (IOException e) {
	        //Log.d(IMAGE_DIRECTORY_NAME, "Error accessing file: " + e.getMessage());
	        Toast.makeText(context, "Error accessing file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
	    } finally {
	    	
	    }
	}
	
	private void applyFilter(View v) throws Exception{
		//gets the image view with the current image
		ImageView imageView = (ImageView) findViewById(R.id.imageView1);
		//gets the name of the clicked filter/effect thumbnail
		LinearLayout layout = (LinearLayout) v;
		TextView text = (TextView) layout.getChildAt(0);
		String str = (String) text.getText();
		int id = 0;

		//gets the id corresponding to such filter/effect
		for(int i = 0; i < EffectsFactory.getNumberOfEffectsAvailable(); i++)
		{
			if(str.compareTo(effectsFactory.getEffect(i).getName()) == 0){
				id = i;
				break;
			}
		}
		//applies the filter/effect to the original image and makes it the current one	
		currentImage = effectsFactory.getEffect(id).applyEffect(previewImage);

		if(image.getHeight() > 2048){
			storeImage = Bitmap.createScaledBitmap(image, (image.getWidth()*2048)/image.getHeight(), 2048, false);
		}			
		else if(image.getWidth() > 2048){
			storeImage = Bitmap.createScaledBitmap(image, 2048, (image.getHeight()*2048/image.getWidth()), false);
		}
		
		storeImage = effectsFactory.getEffect(id).applyEffect(image);
		

		//displays the new image with the filter/effect applied
		imageView.setImageBitmap(currentImage);

		//makes the undo button visible
		Button undoButton = (Button) findViewById(R.id.undoButton);
		undoButton.setVisibility(View.VISIBLE);
		
		//signalizes that an effect/filter was applied
		isThereEffectApplied = true;
	}
}
