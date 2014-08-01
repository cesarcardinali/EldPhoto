package org.eldorado.eldphoto.support;

import java.io.IOException;

import org.eldorado.eldphoto.CamActivity;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/** This class is the preview UI component that will receive the data from the camera and display it in real-time.
 * 
 * @author phack
 *
 */
public class CamPreview extends SurfaceView implements SurfaceHolder.Callback{

	private SurfaceHolder holder;
	private Camera cam = null;
	private int cam_id;

	/**
	 * Creates a new Camera Preview object with the given context, camera
	 * and with a generated Surface Holder.
	 * @param context
	 * @param cam
	 */
	public CamPreview(Context context) {

		super(context); //calls for the super class constructor

		holder = getHolder(); //gets a Surface Holder
		holder.addCallback(this); //assigns this object to answer the call backs
		

		try{
			cam = CamOps.safelyOpen(0, this);
			cam_id = 0;
		}
		catch(RuntimeException e){

			e.printStackTrace();
		}
	}

	/**
	 * This method is called when a surface is created.
	 */
	public void surfaceCreated(SurfaceHolder holder){

		try {
			if(cam == null)
				cam = CamOps.safelyOpen(cam_id, this); //acquires the control of the camera

			//if the camera was successfully acquired
			if(cam != null){
				//adjusts the camera display orientation
				CamActivity activity = (CamActivity) getContext();
				activity.getOrientationListener().enable();
				//CamOps.setCameraDisplayOrientation(cam);
				//CamOps.setPictureSize(cam);

				cam.setPreviewDisplay(holder); //sets the surface displayer
				cam.startPreview(); //starts showing the preview
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method is called when a surface is destroyed.
	 */
	public void surfaceDestroyed(SurfaceHolder holder){

		if(this.cam != null){
			this.cam.stopPreview(); //releases the camera preview
			this.cam.release(); //releases the access to the camera
			this.cam = null;

			CamActivity activity = (CamActivity) getContext();
			activity.getOrientationListener().disable();
			//activity.removeOrientationListener();
		}
	}

	/**
	 * This method is called when a surface suffers a rotation or scaling.
	 */
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h){
/*
		//if the surface doesn't exist, nothing is to be done
		if(holder.getSurface() == null)
			return;
		
		try{
			cam.stopPreview(); //stops the camera preview
		}
		catch(Exception e){}
		
		//handles the changes
		
		//restarts preview with the new settings
		try{
			cam.startPreview();
		}
		catch(Exception e){}*/
	}
	
	public void switchCamera(){
		try{
			CamOps.switchCamera(cam, cam_id);
			cam_id = CamOps.cam_id;
			cam = null;
			cam = CamOps.cam;
			cam.setPreviewDisplay(holder);
			//CamOps.setPictureSize(cam);
			cam.startPreview();
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public Camera getCam(){ return this.cam;}
	public int getCamID(){ return this.cam_id;}
}