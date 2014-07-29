package org.eldorado.eldphoto.support;

import java.util.List;

import org.eldorado.eldphoto.CamActivity;

import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.view.View;

/** This class provides some operations over the camera.
 * It currently opens the camera and returns a reference to it, sets the optimal display and picture orientation and
 * switches the camera, returning the new one.
 * 
 * @author phack
 *
 */
public class CamOps {

	private static View view;
	public static Camera cam;
	public static int cam_id;
	
	/**
	 * Creates, opens and returns an instance of a Camera object.
	 * If an invalid id is given, the main hardware will be used.
	 * @param id - Which camera should be accessed.
	 * @param v - A view on which the camera preview will be displayed.
	 * @return Camera - A ready-for-use Camera object.
	 * @throws RuntimeException - When the camera is busy or it couldn't be opened.
	 */
	public static Camera safelyOpen(int id, View v)
	throws RuntimeException{
		//creates the Camera object
		cam = null;
		
		//checks whether the id is valid
		int n = Camera.getNumberOfCameras();
		
		if(id < 0 || id > n - 1)
			id = 0; //if it is not, turn it into a valid one
		
		//opens the Camera object
	    cam = Camera.open(id);
	    cam_id = id;
	    view = v;
	    
		//returns the new instance to a opened Camera
		return cam;
	}
	
	/** Corrects the camera display orientation.
	 * 
	 * @param camera - Camera - The camera whose display orientation will be corrected
	 */
	public static void setCameraDisplayOrientation(Camera camera) {
			//as the activity orientation is always portrait, the camera display orientation correction is always 90 degrees
			camera.setDisplayOrientation(90);
	 }
	
	/** Corrects the picture orientation. This has to be done because the camera display is always the same.
	 * 
	 * @param activity - Activity - The activity which displays the camera preview.
	 * @param cameraId - int - The ID of the camera (0 to Camera.getNumberOfCameras() - 1)
	 * @param camera - Camera - The camera object.
	 */
	public static void setPictureOrientation(CamActivity activity, int cameraId, Camera camera){
		//retrieves some camera info
		CameraInfo info = new android.hardware.Camera.CameraInfo();
		Camera.getCameraInfo(cameraId, info);
		
		int result = 90;
		
		//gets the orientation of the activity
		int orientation = activity.getRotation();

		//computes the picture angle according to the camera
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation - orientation + 360) % 360;
		}
		else {  // back-facing camera
			result = (info.orientation + orientation) % 360;
		}
		
		Camera.Parameters params = camera.getParameters();
		
		//rounds the angle to 0, 90, 180 or 270 
		if(result >= 315 || result < 45){
			result = 0;
		}
		else if(result >= 45 && result < 135){
			result = 90;
		}
		else if(result >= 135 && result < 225){
			result = 180;
		}
		else if(result >= 225 && result < 315){
			result = 270;
		}
		
		//sets the picture rotation
		params.setRotation(result);
		
		//chooses the best picture size option:
		int width = params.getPreviewSize().width;
		int height = params.getPreviewSize().height;
		//gets a list with the supported picture sizes
		List<Size> picSizes = params.getSupportedPictureSizes();
		
		//computes the size ratio of the preview
		int ratio = 100*width/height;
		
		//if the first picture width is greater than the preview width, ...
		if(picSizes.get(0).width >= width)

			//checks the list from the beginning
			for(int i = 0; i < picSizes.size(); i++){
				
				int picRatio = 100*picSizes.get(i).width/picSizes.get(i).height;

				//if the picture has the same ratio as the preview display, and both dimensions are smaller than 2048, choose this size
				if(ratio == picRatio && picSizes.get(i).width <= 2048 && picSizes.get(i).height <= 2048){
					
					params.setPictureSize(picSizes.get(i).width, picSizes.get(i).height);
					break;
				}
			}
		//if the first picture width is smaller than the preview width, ...
		else
			//checks the list from the end
			for(int i = picSizes.size() - 1; i >= 0; i--){
				
				int picRatio = 100*picSizes.get(i).width/picSizes.get(i).height;
				//if the picture has the same ratio as the preview display, and both dimensions are smaller than 2048, choose this size
				if(ratio == picRatio && picSizes.get(i).width <= 2048 && picSizes.get(i).height <= 2048){
					
					params.setPictureSize(picSizes.get(i).width, picSizes.get(i).height);
					break;
				}
			}

		//sets all parameters
		camera.setParameters(params);
	}
	
	/** Switches the camera from back to front and vice-versa.
	 * It will only switch the camera, if the given camera isn't null and
	 * the device supports at least 2 cameras.
	 * 
	 * @param camera - Camera - The camera object with the current camera.
	 * @param id - int - The ID of the current camera hardware.
	 */
	public static void switchCamera(Camera camera, int id){
		//gets the number of cameras supported by the device
		int numberOfCameras = Camera.getNumberOfCameras();
		
		//if the camera object isn't null and the device has at least 2 cameras, ...
		if(camera != null && numberOfCameras > 1){
			
			try{
				//stops the camera preview
				camera.stopPreview();
				//releases the current camera hardware
				camera.release();
				camera = null;
				
				//gets the next camera ID
				cam_id = (id + 1) % numberOfCameras;
				//opens the next camera with the new ID
				safelyOpen(cam_id, view);
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
}
