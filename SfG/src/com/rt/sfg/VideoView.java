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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

@SuppressLint("HandlerLeak")
public class VideoView extends FrameLayout {

	public static final String TRACE_TAG = "VideoView";
	
    public interface ChangeListener {
        public void onChange();
    }

    private final ImageView imageView;
 
    private boolean running = false;
    
    private SfGClient client;
  
    private ChangeListener changeListener;
    
    private boolean videoViewMode = true;
 
    public VideoView(Context context, SfGClient client) {
        this(context, null, 0);
        this.client = client;
        
    }

    public VideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoView(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
        LayoutInflater.from(context).inflate(R.layout.card_videoview, this);

        imageView = (ImageView) findViewById(R.id.imageView);
    }
   
    public void enableVideoView(boolean enable) {
    	videoViewMode = enable;
    	client.enableVideo(enable);
    }
    
    public boolean isVideoViewMode() {
    	return videoViewMode;
    }
    
    public void setListener(ChangeListener listener) {
        changeListener = listener;
    }
    
    public Handler getImageHandler() {
    	return imageHandler;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
    }
    
    public void start() {
    	running = true;
    }
    
    public void stop() {
     	running = false;
    }

	private Handler imageHandler = new Handler() {
		
    	@Override
    	public void handleMessage(Message msg) {
    		if (running) {
    			Bitmap frame = client.getLatestFrame();
    			if (frame != null) {
    				imageView.setImageBitmap(frame);
    				changeListener.onChange();
    			}
    		}
    	}
    };
}
