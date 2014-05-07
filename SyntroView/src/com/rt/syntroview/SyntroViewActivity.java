//
//  Copyright (c) 2014 richards-tech.
//
//  This file is part of SyntroNet
//
//  SyntroNet is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  SyntroNet is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with SyntroNet.  If not, see <http://www.gnu.org/licenses/>.
//

package com.rt.syntroview;

import java.util.ArrayList;

import com.rt.syntro.lib.*;
import com.rt.syntroview.StreamListFragment.ListSelectionListener;
import com.pansenti.syntroview.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Configuration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

public class SyntroViewActivity extends Activity implements ListSelectionListener{
	static private final String TAG = "SyntroViewActivity";

    private ViewClient client;
    private FragmentManager fragmentManager = null;
	private final StreamListFragment streamListFragment = new StreamListFragment();
	private final VideoViewFragment videoViewFragment = new VideoViewFragment();
	private boolean viewMode = false;
	private String streamName;
	private Activity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = this;

		setContentView(R.layout.portrait_view);
		TextView tv = (TextView)findViewById(R.id.header);
		tv.setText(R.string.stream_list_header);
		fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		fragmentTransaction.add(R.id.fragment_container, streamListFragment);
		fragmentTransaction.commit();
        
        SyntroParams params = new SyntroParams();
        SyntroUtils.setName("", params.controlname, 0);
        SyntroUtils.setName("SyntroView", params.appType, 0);
        SyntroUtils.setName("Android", params.appName, 0);
        SyntroUtils.setName("View", params.compType, 0);
       	WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        client = new ViewClient(wifi, 2, params, imageHandler);
     }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	client.exitThread();
    	client = null;
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged");

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.landscape_view);

        } else {
        	setContentView(R.layout.portrait_view);
        }
        
        TextView tv = (TextView)findViewById(R.id.header);
        if (viewMode) {
            tv.setText(streamName);        	
    	    tv.setGravity(Gravity.CENTER_HORIZONTAL);
      } else {
    		tv.setText(R.string.stream_list_header);       	
    	    tv.setGravity(Gravity.LEFT);
       }
        Fragment fragment;
        
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        fragment = fm.findFragmentById(R.id.fragment_container);
        transaction.detach(fragment);
        transaction.attach(fragment);
        transaction.commit();
    }
    
    @Override
    public void onBackPressed() {
    	if (!viewMode) {
    		super.onBackPressed();
    		return;
    	}
    	viewMode = false;
	    Message msg = Message.obtain();
		Bundle bundle = new Bundle();
		bundle.putString("", "");
		msg.setData(bundle);
		client.getHandler().sendMessage(msg);
		
		if (videoViewFragment.isAdded()) {
			FragmentTransaction fragmentTransaction = fragmentManager
					.beginTransaction();
			fragmentTransaction.remove(videoViewFragment);
			fragmentTransaction.add(R.id.fragment_container, streamListFragment);
			fragmentTransaction.commit();
			fragmentManager.executePendingTransactions();
			
		}
        TextView tv = (TextView)findViewById(R.id.header);
   		tv.setText(R.string.stream_list_header);       	
	    tv.setGravity(Gravity.LEFT);
  	}
    
	@Override
	public void onListSelection(String streamName) {
		this.streamName = streamName;
		if (!videoViewFragment.isAdded()) {
			FragmentTransaction fragmentTransaction = fragmentManager
					.beginTransaction();
			fragmentTransaction.remove(streamListFragment);
			fragmentTransaction.add(R.id.fragment_container, videoViewFragment);
			fragmentTransaction.commit();
			fragmentManager.executePendingTransactions();
			
		}
	    TextView tv = (TextView)findViewById(R.id.header);
	    tv.setGravity(Gravity.CENTER_HORIZONTAL);
	    tv.setText(streamName);
	    
	    Message msg = Message.obtain();
		Bundle bundle = new Bundle();
		bundle.putString("", streamName);
		msg.setData(bundle);
		client.getHandler().sendMessage(msg);
		viewMode = true;
	}

    @SuppressLint("HandlerLeak")
	private Handler imageHandler = new Handler() {
    	
    	@Override
    	public void handleMessage(Message message) {
    		Toast toast;
    		if (client == null)
    			return;
    		
    		switch (message.what) { 
       		case ViewClient.VIEWCLIENT_NEWFRAME:
       			if (viewMode) 
    				videoViewFragment.videoView.setImageBitmap(client.getLatestFrame());
    	   		break;
    	   		
       		case ViewClient.VIEWCLIENT_NEWDIRECTORY:
    			ArrayList<String> sources = client.getSources();
    			streamListFragment.setSources(sources);
    			break;
    			
       		case ViewClient.VIEWCLIENT_LINKCLOSED:
       			toast = Toast.makeText(mainActivity, "Link to SyntroControl closed", Toast.LENGTH_LONG);
       			toast.show();
       			break;
       			
       		case ViewClient.VIEWCLIENT_LINKCONNECTED:
       			toast = Toast.makeText(mainActivity, "Link to SyntroControl established", Toast.LENGTH_LONG);
       			toast.show();
       			break;
    		}

    	}
    };
}
