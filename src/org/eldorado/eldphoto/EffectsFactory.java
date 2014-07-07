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

/** This class should provide access to the filters/effects "plugins" methods.
 * It loads the classes (Effects implementations) specified in "../DCIM/EldPhoto/Effects/classes.txt" and
 * acts as an interface to its methods 'applyEffect' and 'getName'.
 * 
 * @author phack
 *
 */
public class EffectsFactory {

	private Context context; //the application context, used to get access to the application's cache directory
	private Class<?> effectClass; //the effect class that will be dynamically loaded
	private Object effectObj; //the object that implements the effect class
	private static ArrayList<String> classes; //the names of the classes to be loaded
	
	/** Sets the effect class and object so that their methods can be called next.
	 * It returns a reference of itself, so the desired object's method can be accessed in chain.
	 * Example:<p>
	 *     effectsFactory.getEffect(1).getName(); <p>
	 * This will return the name of the 2nd effect loaded, if no error occurs.
	 * 
	 * @param id - int - The id specifying the type of effect.
	 * @return EffectFactory - a reference to itself (the caller).
	 * @exception Exception - if the folder "../DCIM/EldPhoto/Effects/" cannot be accessed nor created; if 
	 * a problem occurs loading or instantiating the classes.
	 */
	public EffectsFactory getEffect(int id)
	throws Exception{
		
		try{
			
			//checks if the external storage is readable
			if(isExternalStorageReadable()){
				//tries to access the folder "../DCIM/EldPhoto/Effects/", where the classes should be
				File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "EldPhoto/Effects/");

				//if this path is not a directory, ...
				if(!folder.isDirectory())
					//tries to create such directory
					//if it fails, ...
					if(!folder.mkdirs()){
						//throws an exception
						throw new Exception("Couldn't find nor create the needed folder!");
					}
				
				//if the folder was successfully accessed, ...
				//gets the path to the APK file, containing the classes' definitions
				String path = folder.getAbsolutePath() + "/EffectClasses.apk";
				
				//gets the path to a cache directory where the optimized classes version will be created by Android
				File dexOpt = context.getCacheDir();
				
				//constructs the class loader, which will load the classes from the APK file
				ClassLoader EffectClassLoader = new DexClassLoader(path, dexOpt.getAbsolutePath(), null, ClassLoader.getSystemClassLoader());
				
				//loads the (id + 1)th class
				effectClass = EffectClassLoader.loadClass(classes.get(id));
				//constructs an object of this class
				effectObj = effectClass.newInstance();
			}
			//if the external storage can't be read, ...
			else{
				//throws an exception
				throw new Exception("The storage isn't ok for reading");
			}
			//returns a reference to itself (the caller)
			return this;
		}
		catch(Exception ex){
			//if some error occurs, throws an exception with its message and cause
			throw new Exception(ex.getMessage(), ex);
		}
	}
	
	/** Calls the method "applyEffect" from the object 'effectObj' and returns its result.
	 * 
	 * @param bitmap - The input image.
	 * @return Bitmap - The resulting image, after the application of the filter/effect or 
	 * null if the object wasn't correctly loaded.
	 * @throws Exception - if the method couldn't be found; if the arguments were invalid;
	 * if the object was invalid.
	 */
	public Bitmap applyEffect(Bitmap bitmap)
	throws Exception{
		
		//the resulting image
		Bitmap picture = null;
		
		//only proceeds if both class and object are different from null
		if(effectClass != null && effectObj != null){
			
			try{
				//gets the desired method, 'applyEffect'
				Method applyEffectMethod = effectClass.getMethod("applyEffect", new Class[]{Bitmap.class});
				//calls the method from the object 'effectObj' with the input image as argument
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
		
		//returns the result
		return picture;
	}
	
	/** Calls the method 'getName' from the object loaded 'effectObj' and returns it result.
	 * 
	 * @return String - The name of the effect/filter loaded or null, if the effect object
	 * wasn't correctly loaded.
	 * @throws Exception - if the method couldn't be found; if the arguments were invalid;
	 * if the object was invalid.
	 */
	public String getName()
	throws Exception{

		//the resulting effect name
		String name = null;

		//only proceeds if both class and object are different from null
		if(effectClass != null && effectObj != null){

			try{
				//gets the method 'getName' from the class loaded
				Method getNameMethod = effectClass.getMethod("getName", new Class<?>[0]);
				//calls the method from the object 'effectObj' and no argument
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

		//returns the result
		return name;
	}
	
	/** Returns the number of effects available.
	 * 
	 * @return int - The number of effects available.
	 */
	public static int getNumberOfEffectsAvailable(){
		//returns the size of the list containing the names of the classes
		return classes.size();
	}
	
	/** Checks if external storage is available to, at least, read
	 * 
	 * @return TRUE, if the the external storage is available to read;
	 * FALSE, otherwise.
	 */
	public static boolean isExternalStorageReadable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state) ||
	        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return true;
	    }
	    return false;
	}
	
	/** Sets the application context, needed to get access
	 * to the application's cache directory.
	 * If the given context is null, nothing changes.
	 * 
	 * @param theContext - Context - The application context.
	 */
	public void setContext(Context theContext){
		if(theContext != null)
			context = theContext;
	}
	
	/** Checks the availability of effects/filters.
	 * It tries to access the file "../DCIM/EldPhoto/Effects/classes.txt" and read the
	 * classes' names from it. If it succeeds, the classes' names will be stored on the
	 * 'classes' list.
	 * 
	 * @throws Exception - if some error reading the file occurs.
	 */
	public static void getAvailableEffects()
	throws Exception{

		//initializes the classes' names list, if it is null
		if(classes == null)
			classes = new ArrayList();
		else return;

		//if we have access to the external storage, ...
		if(EffectsFactory.isExternalStorageReadable()){

			//tries to access the folder "../DCIM/EldPhoto/Effects/"
			File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "EldPhoto/Effects/");
			//if the folder isn't a directory, ...
			if(!folder.isDirectory())
				//tries to create it
				//if can't be created, ...
				if(!folder.mkdirs()){
					//throws an exception
					throw new Exception("Couldn't access effects folder");
				}
			try{
				//tries to access the file classes.txt from the 'folder'
				File classesFile = new File(folder.getAbsoluteFile() + "/classes.txt");
				BufferedReader reader = new BufferedReader(new FileReader(classesFile));

				//reads its first line
				String className = reader.readLine(); 
				//keeps reading its lines until there's no more
				while(className != null){

					//for each class name, adds it into the list
					classes.add(className);
					//and tries to read the next
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
