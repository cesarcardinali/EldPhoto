package org.eldorado.eldphoto;

import org.eldorado.eldphoto.R;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import android.widget.EditText;

public class LoginActivity extends Activity {
	private static final String SPF_NAME = "eldlogin"; // <--- Add this
	private static final String USERNAME = "username"; // <--- To save username
	private static final String PASSWORD = "password"; // <--- To save password
	private static final String KEY = "string!keyword!elds"; // <--- To encrypt the password

	EditText un, pw;
	Button login;
	Context context;
	CheckBox chkRememberMe;
	SimpleAES aes;
	ProgressDialog progress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		context = this.getApplicationContext();
		un = (EditText) findViewById(R.id.user);
		pw = (EditText) findViewById(R.id.pass);
		login = (Button) findViewById(R.id.login);
		chkRememberMe = (CheckBox) findViewById(R.id.savepass);

		SharedPreferences loginPreferences = getSharedPreferences(SPF_NAME,Context.MODE_PRIVATE);

		if (loginPreferences.contains("PASSWORD") && loginPreferences.contains("USERNAME")) {
			try {
				aes = new SimpleAES(KEY);
			} catch (Exception e1) {
				Toast.makeText(getApplicationContext(),
						"Failed to generate AES " + e1.toString(),
						Toast.LENGTH_LONG).show();
				;
			}

			try {
				un.setText(aes.decrypt(loginPreferences.getString(USERNAME, "")));
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(),
						"Failed to save username" + e.toString(),
						Toast.LENGTH_LONG).show();
			}
			try {
				pw.setText(aes.decrypt(loginPreferences.getString(PASSWORD, "")));
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(),
						"Failed to save password" + e.toString(),
						Toast.LENGTH_LONG).show();
			}
			chkRememberMe.setChecked(true);
		}
		progress = new ProgressDialog(this);

		login.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				String uname = un.getText().toString();
				String pwd = pw.getText().toString();
				progress.setMessage("Please wait ...");
				progress.setTitle("Verifying");
				progress.setCancelable(false);
				validateUserTask task = new validateUserTask(progress);
				task.execute(new String[] { uname, pwd });
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
		ProgressDialog pleaseWait;
		
		public validateUserTask(ProgressDialog pleaseWait) {
		    this.pleaseWait = pleaseWait;
		  }
		
		public void onPreExecute() {
			pleaseWait.show();
			
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
			postParameters.add(new BasicNameValuePair("username", params[0]));
			postParameters.add(new BasicNameValuePair("password", params[1]));
			String res = null;
			try {
				String response = 
						CustomHttpClient.executeHttpPost("http://serv196.corp.eldorado.org.br/siscorpservices/exchange.asmx/UserSession",postParameters);
				res = response.toString();
				res = res.replaceAll("\\s+", "");
				message = res;
				publishProgress(0);
			} catch (Exception e) {
				// sets the error message
				message = e.toString();
				// shows the message
				publishProgress(0);
			}
			return res;
		}
		
		@Override
		protected void onPostExecute(String result) {

			if (result != null && result.contains("successful")) {
				String strUserName = "";
				try {
					strUserName = aes.encrypt(un.getText().toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
				String strPassword = "";
				try {
					strPassword = aes.encrypt(pw.getText().toString());
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(), "savepass" + e.toString(), Toast.LENGTH_SHORT).show();
				}

				if (chkRememberMe.isChecked() && (un.getText().length() > 2 && pw.getText().length() > 2)) {
					SharedPreferences loginPreferences = getSharedPreferences(SPF_NAME, Context.MODE_PRIVATE);
					loginPreferences.edit().putString(USERNAME, strUserName).putString(PASSWORD, strPassword).commit();
					Toast.makeText(getApplicationContext(), "Password Saved",Toast.LENGTH_SHORT).show();
				} else {
					SharedPreferences loginPreferences = getSharedPreferences(SPF_NAME, Context.MODE_PRIVATE);
					loginPreferences.edit().clear().commit();
				}
				Intent nextIntent = new Intent(LoginActivity.this,MainActivity.class);
				LoginActivity.this.startActivity(nextIntent);
			} else {
				// sets the error message
				message = "Sorry!! Incorrect Username or Password";
				// shows it
				Toast.makeText(context, message, Toast.LENGTH_LONG).show();
			}
			if(pleaseWait.isShowing()){
				pleaseWait.dismiss();
			}
		}

		protected void onProgressUpdate(Integer... progress) {
			Toast.makeText(context, message, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onBackPressed() {
		finish();
		System.exit(0);
	}

}
