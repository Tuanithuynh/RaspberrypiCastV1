package com.example.raspberrypicast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.jcraft.jsch.*;

import android.os.AsyncTask;
import android.util.Log;

public class SSHHandler extends AsyncTask<String, Void, String> {

	public static final String SUCCESS = "Sent";
	
	// Array containing the last output from terminal
	public static ArrayList<String> last_output = new ArrayList<String>();
	
	private static Session session = null;
	
	// Whether or not the user plans to collect output
	// Note: Some commands hang if there is no output end (EOF) so only use this
	// if you actually want output
	private boolean want_reply;
	
	public SSHHandler() {
		super();
		session = null;
		want_reply = false;
	}
	
	public SSHHandler(boolean want_reply) {
		super();
		session = null;
		this.want_reply = want_reply;
	}
	
	@Override
	protected String doInBackground(String... args) {
		last_output.clear();
		
		String url = args[0];
		String user = args[1];
		String pass = args[2];
		String command = args[3];
		for( int i = 4; i < args.length; i++ ){
			command = command.concat("\n" + args[i]);
		}		
		
		Log.v("SSH Thread", "Url: " + url);
		Log.v("SSH Thread", "User: " + user);
		Log.v("SSH Thread", "Pass: " + pass);
		Log.v("SSH Thread", "Command: " + command);
		
		JSch jsch = new JSch();
		
		// StringBuffer result = new StringBuffer();
		
		try {
			// Set up session
			if( session == null ) {
				session = jsch.getSession(user, url);
			}
			if( !session.isConnected() ) {
				Log.v("Console","Wasn't already connected");
				session.setPassword(pass);
				
				// Turn off SSH HostKey check (Easier than dealing with hostkey)
				java.util.Properties config = new java.util.Properties(); 
				config.put("StrictHostKeyChecking", "no");
				session.setConfig(config);
				
				// Connect to server
				session.connect(30000);
			}
			
			// Check if connected TODO Fail on not-connected
			Log.v("Console", session.isConnected()?"True":"False" );
			
			// Create channel for sending commands
			Channel channel = session.openChannel("exec");
			
			// Set command to send
			((ChannelExec)channel).setCommand(command);
			
			InputStream stdout = channel.getInputStream();
			
			// Send command
			channel.connect();
			
			if( want_reply ) {
				if(channel.getExitStatus() != 0) {
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stdout));
					String result = bufferedReader.readLine();
					
					while( result != null ) {
						Log.v("SSHHandler", "Result: " + result);
						last_output.add(result);
						
						result = bufferedReader.readLine();
					}
				} else {
					Log.v("SSHHandler","Exit Status was not 0");
				}
			}
			
			// Disconnect !important
			channel.disconnect();
		} catch (JSchException e) {
			e.printStackTrace();
			// TODO Add more exceptions to print
			if( e.getMessage().toString().startsWith("java.net.UnknownHostException") ) {
				Log.v("Error:", "Host not found");
				return "Host " + url + " not found";
			}
			return e.getMessage();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return SUCCESS;
	}
	
	/**
	 * Disconnects the connection if it is not already
	 * VERY IMPORTANT THAT YOU DO THIS BEFORE CLOSING APP
	 */
	public static void disconnect() {
		if( session != null && session.isConnected() ) {
			session.disconnect();
		}
	}
	
}
