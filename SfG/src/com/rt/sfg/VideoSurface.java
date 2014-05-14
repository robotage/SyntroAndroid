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

import android.content.Context;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;

public class VideoSurface implements SurfaceHolder.Callback {
    private static final String TRACE_TAG = "VideoSurface";
    
    private final VideoView videoView;

    private SurfaceHolder holder;
    
    public VideoSurface(Context context, SfGClient client) {
        videoView = new VideoView(context, client);
        videoView.setListener(new VideoView.ChangeListener() {

            @Override
            public void onChange() {
                draw(videoView);
            }
        });       
    }
    
    public VideoView getVideoView() {
    	return videoView;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        int measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);

        videoView.measure(measuredWidth, measuredHeight);
        videoView.layout(0, 0, videoView.getMeasuredWidth(), videoView.getMeasuredHeight());
        
        Log.d(TRACE_TAG, "surface changed");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TRACE_TAG, "Surface created");
        this.holder = holder;
        videoView.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TRACE_TAG, "Surface destroyed");
        videoView.stop();
        holder = null;
    }
    
    private void draw(View view) {
        Canvas canvas;
        try {
            canvas = holder.lockCanvas();
        } catch (Exception e) {
            return;
        }
        if (canvas != null) {
            view.draw(canvas);
            holder.unlockCanvasAndPost(canvas);
        }
    }
}
