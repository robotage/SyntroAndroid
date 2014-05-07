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

package com.rt.syntro.lib;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class SyntroAudioWriter implements Runnable {

	public static final String TRACE_TAG = "SyntroAudioWriter: ";

	private Thread t;
	boolean running = true;

    private SyntroAudioData currentAudio = new SyntroAudioData();
    private AudioTrack audioTrack = null;
	
	private LinkedBlockingQueue<SyntroAudioData> audioQueue = new LinkedBlockingQueue<SyntroAudioData>();
	
	public SyntroAudioWriter() {
	
		t = new Thread(this, "SyntroAudioWriter");
		t.start();
	}
	
	public void exitThread() {
		running = false;
	}
	synchronized public void addAudioData(SyntroAudioData data) {
		if (audioQueue.size() > 5) {
			audioQueue.poll();
		}
		try {
			audioQueue.put(data);
		} catch (InterruptedException e) {
			
		}
	}
	
	public void run() {
		while (running) {
			try {
				SyntroAudioData newAudio = audioQueue.poll(100, TimeUnit.MILLISECONDS);
				if (newAudio == null)
					continue;
				if ((audioTrack == null) || (newAudio.channels != currentAudio.channels) ||
					(newAudio.sampleRate != currentAudio.sampleRate) ||
					(newAudio.sampleSize != currentAudio.sampleSize)) {
				currentAudio = newAudio;
				audioOutOpen();
			} else {
				currentAudio = newAudio;
			}
			audioTrack.write(currentAudio.audio, 0, currentAudio.audio.length);

			} catch (InterruptedException e) {
				
			}
		}
		if (audioTrack != null)
			audioTrack.stop();
		Log.d(TRACE_TAG, "Exiting");
	}
	
	private void audioOutOpen() {
	 	audioTrack = null;
	  	audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, currentAudio.sampleRate,
	 							currentAudio.channels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO,
	  							currentAudio.sampleSize == 8 ? AudioFormat.ENCODING_PCM_8BIT : AudioFormat.ENCODING_PCM_16BIT,
	  							currentAudio.sampleRate, AudioTrack.MODE_STREAM);
	  	audioTrack.play();
	}

	
}
