package com.sky.bloggerapp;

//public class Login extends Activity
//{
//
//	/** Called when the activity is first created. */
//	@Override
//	public void onCreate(Bundle savedInstanceState)
//	{
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.login);
//		// TODO Auto-generated method stub
//	}
//
//	private String username, password;
//	private static GoogleService myService;
//	private boolean connected;
//	private final String APPNAME = "SkyBlogger";
//	private final String SERVICENAME = "blogger";
//
//	public Login(String username, String password)
//	{
//		this.username = username;
//		this.password = password;
//		myService = new GoogleService(SERVICENAME, APPNAME);
//		if (myService != null)
//		{
//			try
//			{
//				myService.setUserCredentials(this.username, this.password);
//				connected = true;
//			}
//			catch (com.google.gdata.util.AuthenticationException e)
//			{
//				// Log.e(TAG, "Authentication failed for user " + this.userid+ ".");
//			}
//		}
//		else
//		{
//			// Log.e(TAG, "Unable to create GoogleService!");
//		}
//	}
//
//}

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message; //import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;

import com.sky.bloggerapp.util.Alert;

public class Login extends Activity
{
	private EditText textUsername, textPassword;
	private ProgressDialog verifyProgress = null;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		
		((EditText) this.findViewById(R.id.login_email)).setOnKeyListener(new OnKeyListener()
		{
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				((Button) Login.this.findViewById(R.id.login_btn)).setEnabled(true);
				return false;
			}
		});
		((EditText) this.findViewById(R.id.login_pwd)).setOnKeyListener(new OnKeyListener()
		{
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				((Button) Login.this.findViewById(R.id.login_btn)).setEnabled(true);
				// mDbHelper = new DBAdapter(Login.this);
				// try
				// {
				// mDbHelper.open();
				// }
				// catch (SQLException e)
				// {
				// // Log.e(TAG, "Database has not opened");
				// }
				// setting = mDbHelper.fetchSettindById(1);
				// startManagingCursor(setting);
				// mDbHelper.close();
				// setting.close();
				return false;
			}
		});

		this.findViewById(R.id.login_btn).setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				verify();
				// save();
				login();

			}

			private void verify()
			{
				// TODO Auto-generated method stub
				String username = null;
				String password = null;
				EditText usernameView = (EditText) Login.this.findViewById(R.id.login_email);
				if (usernameView.getText() == null || usernameView.getText().length() < 1)
				{
					Alert.showAlert(Login.this, "Username needed", "You need to give your Google username in order to continue!");
					return;
				}
				else
				{
					username = usernameView.getText().toString();
				}
				usernameView = null;
				EditText passwordView = (EditText) Login.this.findViewById(R.id.login_pwd);
				if (passwordView.getText() == null || passwordView.getText().length() < 1)
				{
					Alert.showAlert(Login.this, "Password needed", "You need to give your Google password in order to continue!");
					return;
				}
				else
				{
					password = passwordView.getText().toString();
				}
				verifyProgress = ProgressDialog.show(Login.this, "Verifying the data", "Starting to verify blogs...");
				
				verifyProgress.setMessage("Started to verify your blogs...");
			}

			private void save()
			{
				// TODO Auto-generated method stub
				textUsername = (EditText) findViewById(R.id.login_email);
				textPassword = (EditText) findViewById(R.id.login_pwd);
				if (textPassword == null || textPassword.getText() == null)
				{
					// Log.d(TAG,"password editor view is null when trying to read!");
					return;
				}
				if (textUsername == null || textUsername.getText() == null)
				{
					// Log.d(TAG,"Username editor view is null when trying to read!");
					return;
				}
				String usernameStr = textUsername.getText().toString();
				String passwordStr = textPassword.getText().toString();
				if (usernameStr.length() < 1)
				{
					usernameStr = "";
				}
				if (passwordStr.length() < 1)
				{
					passwordStr = "";
				}
				final Dialog dlg;
				
				dlg = new AlertDialog.Builder(Login.this).setIcon(com.sky.bloggerapp.R.drawable.ic_dialog_alert).setTitle("Success").setPositiveButton("OK", null).setMessage("Your profile has been successfully saved.").create();
				
				dlg.show();
				
			}

			private void login()
			{
				// TODO Auto-generated method stub
				SharedPreferences login = getSharedPreferences("com.sky.bloggerapp", MODE_PRIVATE);
				SharedPreferences.Editor editor = login.edit();
				editor.putBoolean("LoggedIn", true);
				editor.commit();
				Intent i = new Intent(Login.this, Editor.class);
				startActivity(i);
				finish();
			}
		});
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			finish();
			return true;
		}
		return false;
	}
}