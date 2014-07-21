package org.eldorado.eldphoto;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewImageActivity extends Activity {
	// Declare Variable
	TextView text;
	ImageView imageview;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Get the view from view_image.xml
		setContentView(R.layout.activity_view_image);

		// Retrieve data from MainActivity on GridView item click
		Intent i = getIntent();

		// Get the position
		int position = i.getExtras().getInt("position");

		// Get String arrays FilePathStrings
		String[] filepath = i.getStringArrayExtra("filepath");

		// Get String arrays FileNameStrings
		String[] filename = i.getStringArrayExtra("filename");

		// Locate the TextView in view_image.xml
		text = (TextView) findViewById(R.id.imagetext);

		// Load the text into the TextView followed by the position
		text.setText(filename[position]);

		// Locate the ImageView in view_image.xml
		imageview = (ImageView) findViewById(R.id.full_image_view);

		// Decode the filepath with BitmapFactory followed by the position
		Bitmap bmp = BitmapFactory.decodeFile(filepath[position]);

		// Set the decoded bitmap into ImageView
		imageview.setImageBitmap(bmp);
	}
}
