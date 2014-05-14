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
 
package com.rt.sfg;

import java.util.ArrayList;
import java.util.Collections;

import com.rt.sfg.R;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * Activity showing the options menu.
 */
public class VideoMenu extends Activity {

	public static final String TRACE_TAG = "VideoMenu";
	private ArrayList<String> sources = new ArrayList<String>();
	SfGService.SfGBinder myService;
	private final Handler handler = new Handler();
	
	public boolean attachedToWindow;
	
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        	Log.d(TRACE_TAG, "service connection");
            if (service instanceof SfGService.SfGBinder) {
                sources = ((SfGService.SfGBinder) service).getSources();
                myService = (SfGService.SfGBinder) service;
        		Collections.sort(sources, String.CASE_INSENSITIVE_ORDER);
                openOptionsMenu();
            }
            // No need to keep the service bound.
            unbindService(this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // Nothing to do here.
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.d(TRACE_TAG, "onCreate");
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        attachedToWindow = true;
        bindService(new Intent(this, SfGService.class), connection, 0);
    	Log.d(TRACE_TAG, "after bind");
     }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        attachedToWindow = false;
    }

    @Override
    public void openOptionsMenu() {
    	Log.d(TRACE_TAG, "openOptionsMenu");
        if (attachedToWindow) {
            super.openOptionsMenu();
        	Log.d(TRACE_TAG, "after openOptionsMenu");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	Log.d(TRACE_TAG, "onCreateOptionsMenu");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.videomenu, menu);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	Log.d(TRACE_TAG, "Preparing menu");
    	for (int i = 0; i < sources.size(); i++) {
   			menu.add(0, i, 0, sources.get(i));
     	}
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
    	int id = item.getItemId();
        switch (id) {
     	
            case R.id.stop:
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        stopService(new Intent(VideoMenu.this, SfGService.class));
                    }
                });
                return true;
                
            default:
            	if ((id >= 0) && (id < sources.size())) {
            		myService.newSource(sources.get(id));
            		return true;
            	}
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        // Nothing else to do, closing the Activity.
        finish();
    }
}
