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

package com.rt.syntronavview;

import java.util.ArrayList;

import com.rt.syntro.lib.SyntroParams;
import com.rt.syntro.lib.SyntroUtils;
import com.rt.syntronavview.R;
import android.net.wifi.WifiManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class NavViewControllerActivity extends Activity {
	
	public static final String TAG = "NavViewControllerActivity";
		
	private GLSurfaceView glDisplay;
	private NavViewRenderer renderer;
    private NavViewClient client;
    private ArrayList<String> sources = new ArrayList<String>();
    private SharedPreferences prefs;
    private String selectedSource = new String();
    private NavViewControllerActivity mainActivity;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = this;
        
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        selectedSource = prefs.getString(getString(R.string.saved_stream), "");
         
        glDisplay = new GLSurfaceView(this);
        glDisplay.setEGLContextClientVersion(2);
        renderer = new NavViewRenderer(this, glDisplay);
        glDisplay.setRenderer(renderer);
        glDisplay.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        setContentView(glDisplay);
        
        SyntroParams params = new SyntroParams();
        SyntroUtils.setName("", params.controlname, 0);
        SyntroUtils.setName("SyntroNavView", params.appType, 0);
        SyntroUtils.setName("Android", params.appName, 0);
        SyntroUtils.setName("NavView", params.compType, 0);
       	WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        client = new NavViewClient(wifi, 2, params, handler);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	if (selectedSource != "")
    		activateSource();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	
    		case R.id.mid_SelectStream:
    			Intent intent = new Intent(this, StreamConfigureActivity.class);
    			intent.putStringArrayListExtra(getString(R.string.stream_list), sources);
    			startActivityForResult(intent, 1);
    	}
    	return true;
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if ((requestCode != resultCode) || (data == null))
    		return;											// handle back arrow

		selectedSource = data.getExtras().getString(getString(R.string.selected_stream));
		Editor edit = prefs.edit();
		edit.putString(getString(R.string.saved_stream), selectedSource);
		edit.apply();
		activateSource();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged");
    }
    
    @SuppressLint("HandlerLeak")
	private final Handler handler = new Handler() {
    	@Override
    	public void handleMessage(Message message) {
    		Toast toast;
    		if (client == null)
    			return;
    		
    		switch (message.what){ 
       		case NavViewClient.NAVVIEWCLIENT_NEWDATA:
    	   		renderer.newData(client.getNavData());
    	   		break;
    	   		
       		case NavViewClient.NAVVIEWCLIENT_NEWDIRECTORY:
    			sources = client.getSources();
    			break;
    			
       		case NavViewClient.NAVVIEWCLIENT_LINKCLOSED:
       			toast = Toast.makeText(mainActivity, "Link to SyntroControl closed", Toast.LENGTH_LONG);
       			toast.show();
       			break;
       			
       		case NavViewClient.NAVVIEWCLIENT_LINKCONNECTED:
       			toast = Toast.makeText(mainActivity, "Link to SyntroControl established", Toast.LENGTH_LONG);
       			toast.show();
       			break;
    	}
   	}
    };
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	client.exitThread();
    	client = null;
    }
    
    private void activateSource() {
    	Message msg = Message.obtain();
		Bundle bundle = new Bundle();
		bundle.putString("", selectedSource);
		msg.setData(bundle);
		client.getHandler().sendMessage(msg);
    }
    
}
