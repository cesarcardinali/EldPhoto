package org.eldorado.eldphoto;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import dalvik.system.DexClassLoader;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.widget.Toast;

public class EffectsFactory {

	public static final int HISTOGRAM_EQUALIZATION = 0;
	public static final int INVERSE = 1;
	private static final int NUMBER_OF_EFFECTS = 2;
	private Context context;
	private Class<?> effectClass;
	private Object effectObj;
	private static ArrayList<String> classes;
	
	/** Returns an Effect object specified by the 'id'.
	 * 
	 * @param id - int - The id specifying the type of effect.
	 * @return Effect - An object of the specified effect or null.
	 */
	public EffectsFactory getEffect(int id)
	throws Exception{
		
		try{
			
			if(isExternalStorageReadable()){
				
				File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "EldPhoto/Effects/");
				if(!folder.isDirectory())
					if(!folder.mkdirs()){
						
						throw new Exception("Couldn't access effects folder");
					}
				
				//String path = folder.getAbsolutePath();
				String path = folder.getAbsolutePath() + "/EffectClasses.apk";
				//File dexOpt = new File(context.getCacheDir(), "dex");
				File dexOpt = context.getCacheDir();
				//ClassLoader EffectClassLoader = new EffectsLoader(path, dexOpt, ClassLoader.getSystemClassLoader());
				ClassLoader EffectClassLoader = new DexClassLoader(path, dexOpt.getAbsolutePath(), null, ClassLoader.getSystemClassLoader());
				
//				switch(id){
//				case HISTOGRAM_EQUALIZATION:
//					effectClass = EffectClassLoader.loadClass("org.eldorado.effectclasses.HistogramEqualizationEffect");
//					effectObj = effectClass.newInstance();
//					//effect = EffectMap.get(clazz.getName()).newInstance();
//					break;
//				case INVERSE:
//					effectClass = EffectClassLoader.loadClass("org.eldorado.effectclasses.InverseEffect");
//					effectObj = effectClass.newInstance();
//					//effect = EffectMap.get(clazz.getName()).newInstance();
//					break;
//				}
				
				effectClass = EffectClassLoader.loadClass(classes.get(id));
				effectObj = effectClass.newInstance();
			}
			else{
				throw new Exception("The storage isn't ok for reading");
			}
			return this;
		}
		catch(Exception ex){
			throw new Exception(ex.getMessage());
		}
	}
	
	public Bitmap applyEffect(Bitmap bitmap)
	throws Exception{
		
		Bitmap picture = null;
		
		if(effectClass != null && effectObj != null){
			
			try{
				
				Method applyEffectMethod = effectClass.getMethod("applyEffect", new Class[]{Bitmap.class});
				picture = (Bitmap) applyEffectMethod.invoke(effectObj, bitmap);
			}
			catch(NoSuchMethodException ex){
				
				ex.printStackTrace();
				throw new Exception("Couldn't find the apply effect method!", ex);
			}
			catch(IllegalAccessException ex){
				
				ex.printStackTrace();
				throw new Exception("Couldn't apply the effect method!", ex);
			}
			catch(IllegalArgumentException ex){
				
				ex.printStackTrace();
				throw new Exception("Couldn't apply such argument to effect method!", ex);
			}
			catch(InvocationTargetException ex){
				
				ex.printStackTrace();
				throw new Exception("Couldn't apply method to such object!", ex);
			}
		}
		
		return picture;
	}
	
	public String getName()
	throws Exception{

		String name = null;

		if(effectClass != null && effectObj != null){

			try{

				Method getNameMethod = effectClass.getMethod("getName", new Class<?>[0]);
				name = (String) getNameMethod.invoke(effectObj, (Object[]) null);
			}
			catch(NoSuchMethodException ex){

				ex.printStackTrace();
				throw new Exception("Couldn't find the apply effect method!", ex);
			}
			catch(IllegalAccessException ex){

				ex.printStackTrace();
				throw new Exception("Couldn't apply the effect method!", ex);
			}
			catch(IllegalArgumentException ex){

				ex.printStackTrace();
				throw new Exception("Couldn't apply such argument to effect method!", ex);
			}
			catch(InvocationTargetException ex){

				ex.printStackTrace();
				throw new Exception("Couldn't apply method to such object!", ex);
			}
		}

		return name;
	}
	
	/** Returns the number of effects available.
	 * 
	 * @return int - The number of effects available.
	 */
	public static int getNumberOfEffectsAvailable(){
		
		return classes.size();
	}
	
	/* Checks if external storage is available to at least read */
	public static boolean isExternalStorageReadable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state) ||
	        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return true;
	    }
	    return false;
	}
	
	public void setContext(Context theContext){
		if(theContext != null)
			context = theContext;
	}
	
	public static void getAvailableEffects()
	throws Exception{

		classes = new ArrayList();

		if(EffectsFactory.isExternalStorageReadable()){

			File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "EldPhoto/Effects/");
			if(!folder.isDirectory())
				if(!folder.mkdirs()){

					throw new Exception("Couldn't access effects folder");
				}
			try{
				File classesFile = new File(folder.getAbsoluteFile() + "/classes.txt");
				BufferedReader reader = new BufferedReader(new FileReader(classesFile));

				String className = reader.readLine(); 

				while(className != null){

					classes.add(className);
					className = reader.readLine();
				}
			}
			catch(FileNotFoundException ex){

				ex.printStackTrace();
				throw new Exception("Couldn't find the effects file!", ex);
			}
			catch(IOException ex){

				ex.printStackTrace();
				throw new Exception("Problem accessing the effects file!", ex);
			}
		}
	}
}
