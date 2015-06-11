package com.example.raspberrypicast;

import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;



public class MainActivity extends Activity {
	
	protected static final int STATIC_INTEGER_VALUE = 1;
	Button playorpause;
	private static final int Setting = Menu.FIRST;
	private SharedPreferences prefs;
	public static Context c;
	static String  path ;
	
	private String url = "192.168.1.115";
	private String user = "pi";
	private String pass = "ras";
	VideoView videoView;
	
	
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);	
		setContentView(R.layout.activity_main);
		
        c = getApplicationContext();
        prefs = getSharedPreferences("omxpi", Context.MODE_PRIVATE);
        videoView = (VideoView)this.findViewById(R.id.vdview);
        playorpause = (Button) findViewById(R.id.btnPlay);
    }
    
    public void Start(View view)
    {
    	playorpause.setText("pause");
    	String location = "http://mikelyons.org/external/cos.mp3";
		Log.d("General", "Start server Clicked!");
		if( !path.toString().matches("") ) {
			location = path.toString();
		}
		
		runCommand("killall omxplayer.bin\nmmkfifo /var/tmp/omx 2> /dev/null\nomxplayer --adev "+ "hdmi" +" " + location + " < /var/tmp/omx &\necho '.' >> /var/tmp/omx");
		videostream(location);
		
    }
    public void Stop(View view)
    {
    	
    	runCommand("echo -n 'q' >> /var/tmp/omx\nkillall omxplayer.bin\nrm -rf /var/tmp/omx 2> /dev/null");
    	videoView.stopPlayback();
    }
    public void Play(View view)
    {
    	
    	
    	if(videoView.isPlaying())
		{
    		playorpause.setText("Play");
			videoView.pause();
			runCommand("echo -n 'p' >> /var/tmp/omx");
			
		}
		else
		{
			playorpause.setText("Pause");
			videoView.start();
			runCommand("echo -n 'p' >> /var/tmp/omx");
		}
    }
    public void setting(View view)
    {
    	Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
    }
    
    public void videostream(String video)
    {
    	
        MediaController mc = new MediaController(this);
        videoView.setMediaController(mc);
        videoView.setVideoURI(Uri.parse(Environment.getExternalStorageDirectory()+video));
        videoView.requestFocus();
        videoView.setBackgroundColor(R.drawable.abc_item_background_holo_light);
        videoView.start();     
    }
    
    public void Sdcard(View view)
    {
    	Intent intent1 = new Intent(this, FileListSdcard.class);
		Bundle bundle = new Bundle();
		bundle.putString("user", user);
		bundle.putString("url", url);
		bundle.putString("password", pass);
		intent1.putExtra("data", bundle);
		startActivity(intent1);
    }
    
    public void Raspberry(View view)
    {
    	Intent intent2 = new Intent(MainActivity.c, FileListActivity.class);
		startActivityForResult(intent2, STATIC_INTEGER_VALUE);
    }
    
    


    @Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		checkSettings();
	}
    
    
    

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode)
		{
		case STATIC_INTEGER_VALUE:
			if (resultCode == Activity.RESULT_OK) { 
				String file = data.getStringExtra(FileListActivity.PUBLIC_STATIC_STRING_IDENTIFIER);
				path = (file);
			} 
			break;
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		SSHHandler.disconnect();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		SSHHandler.disconnect();
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.mymenu, menu);
		menu.add(0, Setting, 0,"Setting").setIcon(R.drawable.abc_item_background_holo_light);
		
		
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
		case Setting:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void checkSettings() {
        if( prefs.contains("url") && prefs.contains("user") && prefs.contains("pass") ) {
        	this.url = prefs.getString("url", "No Url");
        	this.pass = prefs.getString("pass", "No Pass");
        	this.user = prefs.getString("user", "No User");
        } else if( prefs.getString("url", "No Url").equals("No Url") ||
        		   prefs.getString("pass", "No Pass").equals("No Pass") ||
        		   prefs.getString("user", "No User").equals("No User")) {
        	Log.v("Settings", "Needs settings 2nd Reason");
        	Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
        } else {
        	Log.v("Settings", "Needs settings");
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
        }
    }
    
    public void runCommand(String cmd) {
    	String reply = "Failed";    	
    	try {
			reply = new SSHHandler().execute(url, user, pass, cmd).get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	if( !reply.equals(SSHHandler.SUCCESS) ) {
    		CharSequence text = reply;
    		Toast.makeText(c, text, Toast.LENGTH_SHORT).show();
    		
    	}
    }
}
