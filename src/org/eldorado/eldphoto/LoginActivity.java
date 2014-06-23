package org.eldorado.eldphoto;

import org.eldorado.eldphoto.R;
import java.util.ArrayList;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.EditText;

public class LoginActivity extends Activity {
	EditText un,pw;
	Button login;
	Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		context = this.getApplicationContext();
		un=(EditText)findViewById(R.id.user);
		pw=(EditText)findViewById(R.id.pass);		
		login = (Button) findViewById(R.id.login);



		login.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				//				Intent nextIntent = new Intent(LoginActivity.this, MainActivity.class);
				//				//nextIntent.putExtra("nome", "user");
				//				LoginActivity.this.startActivity(nextIntent);

				String uname = un.getText().toString();
				String pwd = pw.getText().toString();

				validateUserTask task = new validateUserTask();
				task.execute(new String[]{uname, pwd});				
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}


	private class validateUserTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
			postParameters.add(new BasicNameValuePair("username", params[0] ));
			postParameters.add(new BasicNameValuePair("password", params[1] ));				
			String res = null;
			try {
				String response = CustomHttpClient.executeHttpPost("http://www.decom.fee.unicamp.br/~rhiga/check.php", postParameters);
				//String response = CustomHttpClient.executeHttpPost("http://drive.google.com", postParameters);
				res=response.toString();
				res= res.replaceAll("\\s+","");

			} catch (Exception e) {
				Toast.makeText(getApplicationContext(),e.toString(),
						Toast.LENGTH_SHORT).show();
			}
			return res;
		}//close doInBackground

		@Override
		protected void onPostExecute(String result) {
			if(result.equals("1")){
				//navigate to Main Menu
				Intent intent = new Intent(LoginActivity.this, MainActivity.class);
				startActivity(intent);
			}
			else{
				Toast.makeText(getApplicationContext(), "Sorry!! Incorrect Username or Password",
						Toast.LENGTH_SHORT).show();
			}   
		}//close onPostExecute
	}// close validateUserTask 		

}
