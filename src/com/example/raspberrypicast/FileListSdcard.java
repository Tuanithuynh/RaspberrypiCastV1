package com.example.raspberrypicast;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FileListSdcard extends Activity {
	
	ListView lv;
	String[] items;
	String filepath = null;
	private ProgressDialog pd;
	String url;
	String user;
	String pass;
	String path;
	private FileInputStream fis;
	//private SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);	
		setContentView(R.layout.activity_listsdcard);
		
		Intent intent = getIntent();
		Bundle bundles = intent.getBundleExtra("data");
		url= bundles.getString("url");
		user = bundles.getString("user");
		pass = bundles.getString("password");
		//path = prefs.getString("path", "");
		
		lv = (ListView) findViewById(R.id.listsdcard);
		
		ArrayList<File> myvideo  = findvideo(Environment.getExternalStorageDirectory());
        items = new String[myvideo.size()];
        for(int i = 0; i < myvideo.size();i++)
        {
            items[i] = myvideo.get(i).getName().toString();
            
        }
        ArrayAdapter<String> adp = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,items);
        lv.setAdapter(adp);
        
        lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				filepath = parent.getItemAtPosition(position).toString();
			}
		});
	}
	
	public void upload(View view)
	{
		
		pd = ProgressDialog.show(FileListSdcard.this, "", "Uploading...",true, false);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					uploadvideo();
				} catch (SftpException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				pd.dismiss();
			}
		}).start();
		
		
		
	}
	
	public ArrayList<File> findvideo(File root)
    {
        ArrayList<File> al = new ArrayList<File>();
        File[] files = root.listFiles();
        for(File singlefile:files)
        {
            if(singlefile.isDirectory() && !singlefile.isHidden())
            {
                al.addAll(findvideo(singlefile));
            }
            else
            {
                if(singlefile.getName().endsWith(".MP4")|| singlefile.getName().endsWith(".mp4"))
                {
                    al.add(singlefile);
                }
            }
        }
        return al;
    }
	 public void uploadvideo() throws SftpException, IOException
	 {
		 File uploadFilePath;
		    Session session ;
		     Channel channel = null;
		    ChannelSftp sftp = null;
		    uploadFilePath=new File(Environment.getExternalStorageDirectory()+"/home/pi/"+filepath);
		    byte[] bufr = new byte[(int) uploadFilePath.length()];
		    fis = new FileInputStream(uploadFilePath);
		    fis.read(bufr);
		    JSch ssh = new JSch();
		   try {
		        session =ssh.getSession(user, url);
		        session.setPassword(pass);
		        java.util.Properties config = new java.util.Properties(); 
		        config.put("StrictHostKeyChecking", "no");
		        session.setConfig(config);
		        session.connect();
		        channel = session.openChannel("sftp"); 
		        channel.connect();
		        sftp= (ChannelSftp)channel;
		        sftp.cd("/home/pi"); 
		    }
		    catch(Exception e){
		    }
		   ByteArrayInputStream in = new ByteArrayInputStream(bufr);
		   sftp.put(in, uploadFilePath.getName(), null);
		in.close();
	 }
}


