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
public class CameraMenu extends Activity {

	public static final String TRACE_TAG = "CameraMenu";
	private CameraSurface cameraSurface = null;
	
	private final Handler handler = new Handler();
	public boolean attachedToWindow;
	
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        	Log.d(TRACE_TAG, "service connection");
            if (service instanceof SfGService.SfGBinder) {
                cameraSurface = ((SfGService.SfGBinder) service).getCameraSurface();
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
        bindService(new Intent(this, SfGService.class), connection, 0);
    	Log.d(TRACE_TAG, "after bind");
    }
    
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        attachedToWindow = true;
        openOptionsMenu();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        attachedToWindow = false;
    }
    
    @Override
    public void openOptionsMenu() {
    	Log.d(TRACE_TAG, "openOptionsMenu");
        if (attachedToWindow && (cameraSurface != null)) {
            super.openOptionsMenu();
        	Log.d(TRACE_TAG, "after openOptionsMenu");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	Log.d(TRACE_TAG, "onCreateOptionsMenu");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.cameramenu, menu);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	Log.d(TRACE_TAG, "onPrepareOptionsMenu " + cameraSurface);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
           case R.id.stop:
               handler.post(new Runnable() {

                   @Override
                   public void run() {
                       stopService(new Intent(CameraMenu.this, SfGService.class));
                   }
               });
               return true;
                
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        // Nothing else to do, closing the Activity.
        finish();
    }
}
