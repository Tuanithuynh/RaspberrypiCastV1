package com.example.raspberrypicast;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends Activity {

	Button done;
	
	EditText url;
	EditText user;
	EditText pass;
	EditText path;
	
	private SharedPreferences prefs;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.settingip);
	    Toast.makeText(MainActivity.c, "Please Enter Server Information", Toast.LENGTH_SHORT).show();
	    // TODO Auto-generated method stub
	    
	    done = (Button) findViewById(R.id.settingsDone);
	    
	    url = (EditText) findViewById(R.id.settingsUrl);
	    user = (EditText) findViewById(R.id.settingsUser);
	    pass = (EditText) findViewById(R.id.settingsPass);
	    path = (EditText) findViewById(R.id.settingsPath); 
	    prefs = getSharedPreferences("omxpi", Context.MODE_PRIVATE);
	    
	    url.setText(prefs.getString("url", ""));
	    user.setText(prefs.getString("user", ""));
	    pass.setText(prefs.getString("pass", ""));
	    path.setText(prefs.getString("path", ""));
	    addListeners();
	}
	
	private void saveSettings() {
		Log.v("Settings", "Settings Saving");
		SharedPreferences.Editor editor = prefs.edit();
		if( !user.getText().toString().equals("") ) {
			editor.putString("user", user.getText().toString());
		}
		if( !url.getText().toString().equals("") ) {
			editor.putString("url", url.getText().toString());
		}
		if( !pass.getText().toString().equals("") ) {
			editor.putString("pass", pass.getText().toString());
		}
		if( !path.getText().toString().equals("") ) {
			editor.putString("path", path.getText().toString());
		}
		editor.commit();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Toast.makeText(MainActivity.c, "Please Enter Server Information", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		saveSettings();
	}

	public void addListeners() {
		done.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v("Settings", "Done Clicked!");
				saveSettings();
				finish();
			}
		});
	}
	
}
