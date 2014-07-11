package org.eldorado.eldphoto;

import android.app.Application;
import android.graphics.Bitmap;

/** This class acts as a singleton, keeping data as global to all application.
 * Currently it stores the picture as a byte array after the it is taken.
 * 
 * @author phack
 *
 */
public class EldPhotoApplication extends Application {

	private static byte[] picture;
	private static Bitmap bmp;
	
	public EldPhotoApplication() {
		super();
	}

	public static byte[] getPicture() {
		return picture;
	}

	public static void setPicture(byte[] newPicture) {
		picture = null;
		picture = newPicture;
		bmp = null;
	}

	public static Bitmap getBitmap() {
		return bmp;
	}
	
	public static void setBitmap(Bitmap newPicture) {
		bmp = null;
		bmp = newPicture;
	}
	
	public static boolean hasBitmap() {
		if (bmp != null) {
			return true;
		}
		else {
			return false;
		}
				
	}
	
}
