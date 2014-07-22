package org.eldorado.eldphoto;

import java.io.File;

import org.eldorado.eldphoto.support.GridViewAdapter;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

public class ShowPicsActivity extends Activity {
	
	private String[] FilePathStrings;
    private String[] FileNameStrings;
    private File[] listFile;
    GridView grid;
    File file;
    GridViewAdapter adapter;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_pics);
		
		if (!Environment.getExternalStorageState().equals(
	            Environment.MEDIA_MOUNTED)) {
	        Toast.makeText(this, "Error! No SDCARD Found!", Toast.LENGTH_LONG).show();
	    } else {
	        // Locate the image folder in your SD Card
	        file = new File(Environment.getExternalStorageDirectory() + File.separator + "Eldphoto");
	        // Create a new folder if no folder named SDImageTutorial exist
	        file.mkdirs();
	    }

	    if (file.isDirectory()) {
	        listFile = file.listFiles();
	        // Create a String array for FilePathStrings
	        FilePathStrings = new String[listFile.length];
	        // Create a String array for FileNameStrings
	        FileNameStrings = new String[listFile.length];
	        for (int i = 0; i < listFile.length; i++) {
	        	// Get the name image file
	            FileNameStrings[i] = listFile[i].getName().toString();
	        	// Get the path of the image file
	            FilePathStrings[i] = listFile[i].getAbsolutePath();
	        }
	    }
	    
	    grid = (GridView) findViewById(R.id.gridview);
        // Pass String arrays to LazyAdapter Class
	    adapter = new GridViewAdapter(this, FilePathStrings, FileNameStrings);
        // Set the LazyAdapter to the GridView
        grid.setAdapter(adapter);
 
        // Capture gridview item click
        grid.setOnItemClickListener((new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(ShowPicsActivity.this, ViewImageActivity.class);
                // Pass String arrays FilePathStrings
                i.putExtra("filepath", FilePathStrings);
                // Pass String arrays FileNameStrings
                i.putExtra("filename", FileNameStrings);
                // Pass click position
                i.putExtra("position", position);
                startActivity(i);
            }
        }));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.show_pics, menu);
		return true;
		
		
	}

}
