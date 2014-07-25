package org.eldorado.eldphoto;

import android.app.Application;

/**
 * This class acts as a singleton, keeping data as global to all application.
 * Currently it stores the picture as a byte array after the it is taken.
 * 
 * @author phack
 * 
 */
public class EldPhotoApplication extends Application {

	private static byte[] picture;

	public EldPhotoApplication() {
		super();
	}

	public static byte[] getPicture() {
		return picture;
	}

	public static void setPicture(byte[] newPicture) {
		picture = null;
		picture = newPicture;
	}
}
