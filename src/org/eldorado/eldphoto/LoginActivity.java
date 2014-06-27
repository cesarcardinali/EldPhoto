package org.eldorado.eldphoto;

import org.eldorado.eldphoto.R;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import android.widget.EditText;

public class LoginActivity extends Activity {
	private static final String SPF_NAME = "eldlogin"; //  <--- Add this
	private static final String USERNAME = "username";  //  <--- To save username
	private static final String PASSWORD = "password";  //  <--- To save password
	private static final String KEY = "security@eldorado";  //  <--- To encrypt the password
	
	EditText un,pw;
	Button login;
	Context context;
	CheckBox chkRememberMe;
	SimpleAES aes;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		context = this.getApplicationContext();
		un=(EditText)findViewById(R.id.user);
		pw=(EditText)findViewById(R.id.pass);		
		login = (Button) findViewById(R.id.login);
		chkRememberMe = (CheckBox) findViewById(R.id.savepass);
		

		SharedPreferences loginPreferences = getSharedPreferences(SPF_NAME,
	            Context.MODE_PRIVATE);		
		
		try {
			aes= new SimpleAES(KEY);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			Toast.makeText(getApplicationContext(),"aes "+e1.toString(),Toast.LENGTH_LONG).show();;
		}
		
		try {
			un.setText(aes.decrypt(loginPreferences.getString(USERNAME,"")));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast.makeText(getApplicationContext(),"un" + e.toString(),
					Toast.LENGTH_LONG).show();
		}
		try {
			pw.setText(aes.decrypt(loginPreferences.getString(PASSWORD,"")));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast.makeText(getApplicationContext(),e.toString(),
					Toast.LENGTH_LONG).show();
		}
		//un.setText(loginPreferences.getString(USERNAME,""));
		
		
		
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

	
	private class validateUserTask extends AsyncTask<String, Integer, String> {
		
		private String message;
		
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
				//sets the error message
				message = e.toString();
				//shows the message
				publishProgress(0);
			}

			return res;
		}//close doInBackground

		@Override
		protected void onPostExecute(String result) {
			
			if(result != null && result.equals("1")){
				String strUserName="false";
				try {
					strUserName = aes.encrypt(un.getText().toString());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String strPassword="false";
				try {
					strPassword = aes.encrypt(pw.getText().toString());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Toast.makeText(getApplicationContext(), "savepass" + e.toString(),
							Toast.LENGTH_SHORT).show();
				}
				
				if (chkRememberMe.isChecked()){
					SharedPreferences loginPreferences = getSharedPreferences(SPF_NAME, Context.MODE_PRIVATE);
					loginPreferences.edit().putString(USERNAME, strUserName).putString(PASSWORD, strPassword).commit();
					Toast.makeText(getApplicationContext(), "Password Saved",Toast.LENGTH_SHORT).show();
				}
					//navigate to Main Menu					
				Intent intent = new Intent(LoginActivity.this, MainActivity.class);
				startActivity(intent);
			}
			else{
				//sets the error message
				message = "Sorry!! Incorrect Username or Password";
				//shows it
				publishProgress(0);
			}   
		}
		//close onPostExecute
		
	
		protected void onProgressUpdate(Integer... progress) {
			Toast.makeText(context, message, Toast.LENGTH_LONG).show();
		}
		
	}// close validateUserTask 		

}
