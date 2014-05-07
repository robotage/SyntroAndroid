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
import android.util.Log;


public class SyntroAV {
	
	public static final String TRACE_TAG = "SyntroAV";
	
	// record header parameter field values
	
    public static final int SYNTRO_RECORDHEADER_PARAM_NOOP = 0;  	// indicates a filler record
    public static final int SYNTRO_RECORDHEADER_PARAM_REFRESH = 1;	// indicates a refresh MJPEG frame
    public static final int SYNTRO_RECORDHEADER_PARAM_NORMAL = 2;   // indicates a normal record
    public static final int SYNTRO_RECORDHEADER_PARAM_PREROLL = 3;  // indicates a preroll frame
    public static final int SYNTRO_RECORDHEADER_PARAM_POSTROLL = 4; // indicates a postroll frame

    // AVMUX subtype codes
    
	public static final int SYNTRO_RECORD_TYPE_AVMUX_UNKNOWN = -1;	// unknown mux
	public static final int SYNTRO_RECORD_TYPE_AVMUX_MJPPCM = 0;	// MJPEG + PCM interleaved
	public static final int SYNTRO_RECORD_TYPE_AVMUX_MP4 = 1;		// MP4 mux
	public static final int SYNTRO_RECORD_TYPE_AVMUX_OGG = 2;		// Ogg mux
	public static final int SYNTRO_RECORD_TYPE_AVMUX_WEBM = 3;		// Webm mux
	public static final int SYNTRO_RECORD_TYPE_AVMUX_RTP = 4;		// RTP interleave format
	public static final int SYNTRO_RECORD_TYPE_AVMUX_RTPCAPS = 5;	// RTP caps
	public static final int SYNTRO_RECORD_TYPE_AVMUX_END = 6;		// the end
 
	// Video subtype codes
	
	public static final int SYNTRO_RECORD_TYPE_VIDEO_UNKNOWN = -1;	// unknown format or not present
	public static final int SYNTRO_RECORD_TYPE_VIDEO_MJPEG = 0;		// MJPEG compression
	public static final int SYNTRO_RECORD_TYPE_VIDEO_MPEG1 = 1;		// MPEG1 compression
	public static final int SYNTRO_RECORD_TYPE_VIDEO_MPEG2 = 2;		// MPEG2 compression
	public static final int SYNTRO_RECORD_TYPE_VIDEO_H264 = 3;		// H264 compression
	public static final int SYNTRO_RECORD_TYPE_VIDEO_VP8 = 4;		// VP8 compression
	public static final int SYNTRO_RECORD_TYPE_VIDEO_THEORA = 5;	// theora compression
	public static final int SYNTRO_RECORD_TYPE_VIDEO_RTPMPEG4 = 6;	// mpeg 4 over RTP
	public static final int SYNTRO_RECORD_TYPE_VIDEO_RTPCAPS = 7;	// RTP caps message
	public static final int SYNTRO_RECORD_TYPE_VIDEO_END = 8;		// the end
	
	// Audio subtype codes
	
	public static final int SYNTRO_RECORD_TYPE_AUDIO_UNKNOWN = -1;	// unknown format or not present
	public static final int SYNTRO_RECORD_TYPE_AUDIO_PCM = 0;		// PCM/WAV uncompressed
	public static final int SYNTRO_RECORD_TYPE_AUDIO_MP3 = 1;		// MP3 compression
	public static final int SYNTRO_RECORD_TYPE_AUDIO_AC3 = 2;		// AC3 compression
	public static final int SYNTRO_RECORD_TYPE_AUDIO_AAC = 3;		// AAC compression
	public static final int SYNTRO_RECORD_TYPE_AUDIO_VORBIS = 4;	// Vorbis compression
	public static final int SYNTRO_RECORD_TYPE_AUDIO_RTPAAC = 5;	// aac over RTP
	public static final int SYNTRO_RECORD_TYPE_AUDIO_RTPCAPS = 6;	// RTP caps message
	public static final int SYNTRO_RECORD_TYPE_AUDIO_END = 7;		// the end

	// The AVMUX record
	
	public static final int SYNTRO_RECORD_AVMUX_SPARE0 = 0;				// unused - set to 0
	public static final int SYNTRO_RECORD_AVMUX_VIDEOSUBTYPE = 2;	// the video subtype
	public static final int SYNTRO_RECORD_AVMUX_AUDIOSUBTYPE = 3;	// the audio subtype
	public static final int SYNTRO_RECORD_AVMUX_MUXSIZE = 4;		// length of muxed data
	public static final int SYNTRO_RECORD_AVMUX_VIDEOSIZE = 8;		// length of video data
	public static final int SYNTRO_RECORD_AVMUX_AUDIOSIZE = 12;		// length of audio data
	public static final int SYNTRO_RECORD_AVMUX_VIDEOWIDTH = 16;	// width of video frames
	public static final int SYNTRO_RECORD_AVMUX_VIDEOHEIGHT = 18;	// height of video frames
	public static final int SYNTRO_RECORD_AVMUX_FRAMERATE = 20;		// frame rate
	public static final int SYNTRO_RECORD_AVMUX_VIDEOSPARE = 22;	// unused
	public static final int SYNTRO_RECORD_AVMUX_AUDIOSAMPLERATE = 24;	// audio samples per second
	public static final int SYNTRO_RECORD_AVMUX_AUDIOCHANNELS = 28;	// number of audio channels
	public static final int SYNTRO_RECORD_AVMUX_AUDIOSAMPLESIZE = 30;// bit size of samples
	public static final int SYNTRO_RECORD_AVMUX_AUDIOSPARE = 32;	// unused
	public static final int SYNTRO_RECORD_AVMUX_SPARE1 = 34;		// unused - set to 0

	public static final int SYNTRO_RECORD_AVMUX_LENGTH = 36;		// total length of header

	// cracked values
	
	public SyntroMessage message;							// the message itself
	public byte seqno;										// the sequence number
	public long timestamp;									// the record timestamp
	
	public int avmuxSize;									// length of mux data
	public int videoSize;									// length of video data
	public int audioSize;									// length of audio data
	public int avmuxSubtype;								// the mux subtype
	public byte videoSubtype;								// type of video stream
	public byte audioSubtype;								// type of audio stream
	public int videoWidth;									// width of frame
	public int videoHeight;									// height of frame
	public int videoFramerate;								// framerate
	public int audioSampleRate;								// sample rate of audio
	public int audioChannels;								// number of channels
	public int audioSampleSize;								// size of samples (in bits)
	
	public int muxOffset;									// position in array of mux data
	public int videoOffset;									// position in array of video data
	public int audioOffset;									// position in array of audio data
	
	public boolean crackAvmux(SyntroMessage message) {
		
		int ptr;
				
		this.message = message;
		if (message.getDataLength() < (SyntroDefs.SYNTRO_RECORD_HEADER_LENGTH + SYNTRO_RECORD_AVMUX_LENGTH)) {
			Log.e(TRACE_TAG, "Got avmux packet but was too short " + message.getDataLength());
			return false;
		}
		
		try {
			//	Get stuff from Ehead
			
			ptr = SyntroDefs.SYNTRO_MESSAGE_LENGTH;			// start of ehead
			
			seqno = message.get()[ptr + SyntroDefs.SYNTRO_EHEAD_SEQ];
			
			// get stuff from record header
			
			ptr += SyntroDefs.SYNTRO_EHEAD_LENGTH;
					
			int type = SyntroUtils.convertUC2ToInt(message.get(), ptr + SyntroDefs.SYNTRO_RECORD_HEADER_TYPE);
			if (type != SyntroDefs.SYNTRO_RECORD_TYPE_AVMUX) {
				Log.e(TRACE_TAG, "Record is not avmux type " + type);
				return false;
			}
			
			avmuxSubtype = SyntroUtils.convertUC2ToInt(message.get(), ptr + SyntroDefs.SYNTRO_RECORD_HEADER_SUBTYPE);
			timestamp = SyntroUtils.convertUC8ToLong(message.get(), ptr + SyntroDefs.SYNTRO_RECORD_HEADER_TIMESTAMP);

			// get stuff from avmux header
			
			ptr += SyntroDefs.SYNTRO_RECORD_HEADER_LENGTH;
			
			avmuxSize = SyntroUtils.convertUC4ToInt(message.get(), ptr + SYNTRO_RECORD_AVMUX_MUXSIZE);
			videoSize = SyntroUtils.convertUC4ToInt(message.get(), ptr + SYNTRO_RECORD_AVMUX_VIDEOSIZE);
			audioSize = SyntroUtils.convertUC4ToInt(message.get(), ptr + SYNTRO_RECORD_AVMUX_AUDIOSIZE);
			videoSubtype = message.get()[ptr + SYNTRO_RECORD_AVMUX_VIDEOSUBTYPE];
			audioSubtype = message.get()[ptr + SYNTRO_RECORD_AVMUX_AUDIOSUBTYPE];
			videoWidth = SyntroUtils.convertUC2ToInt(message.get(), ptr + SYNTRO_RECORD_AVMUX_VIDEOWIDTH);
			videoHeight = SyntroUtils.convertUC2ToInt(message.get(), ptr + SYNTRO_RECORD_AVMUX_VIDEOHEIGHT);
			videoFramerate = SyntroUtils.convertUC2ToInt(message.get(), ptr + SYNTRO_RECORD_AVMUX_FRAMERATE);
			audioSampleRate = SyntroUtils.convertUC4ToInt(message.get(), ptr + SYNTRO_RECORD_AVMUX_AUDIOSAMPLERATE);
			audioSampleSize = SyntroUtils.convertUC2ToInt(message.get(), ptr + SYNTRO_RECORD_AVMUX_AUDIOSAMPLESIZE);
			audioChannels = SyntroUtils.convertUC2ToInt(message.get(), ptr + SYNTRO_RECORD_AVMUX_AUDIOCHANNELS);
			
			if (message.getDataLength() != (SyntroDefs.SYNTRO_EHEAD_LENGTH +
											SyntroDefs.SYNTRO_RECORD_HEADER_LENGTH + 
											SYNTRO_RECORD_AVMUX_LENGTH +
											avmuxSize + videoSize + audioSize)) {
				Log.e(TRACE_TAG, "Size of avmux packet doesn't matched size in header");
				return false;
			}
			muxOffset = SyntroDefs.SYNTRO_MESSAGE_LENGTH + 
					SyntroDefs.SYNTRO_EHEAD_LENGTH +
					SyntroDefs.SYNTRO_RECORD_HEADER_LENGTH + 
					SYNTRO_RECORD_AVMUX_LENGTH;
			
			videoOffset = muxOffset + avmuxSize;
			audioOffset = videoOffset + videoSize;
			
		} catch (Exception e) {
			Log.e(TRACE_TAG, "Failed to crack avmux header " + e);
			return false;
		}
		
		return true;
	}
	
	public boolean buildAvmux(SyntroMessage message, int param) {
		int ptr;
		
		if (message.getDataLength() < (SyntroDefs.SYNTRO_RECORD_HEADER_LENGTH + SYNTRO_RECORD_AVMUX_LENGTH +
				avmuxSize + videoSize + audioSize)) {
			Log.e(TRACE_TAG, "Build avmux but message was too short " + message.getDataLength());
			return false;
		}
		ptr = SyntroDefs.SYNTRO_MESSAGE_LENGTH + SyntroDefs.SYNTRO_EHEAD_LENGTH;	// start of record header
		
		SyntroUtils.convertIntToUC2(SyntroDefs.SYNTRO_RECORD_TYPE_AVMUX,
				message.get(), ptr + SyntroDefs.SYNTRO_RECORD_HEADER_TYPE);

		SyntroUtils.convertIntToUC2(avmuxSubtype,
				message.get(), ptr + SyntroDefs.SYNTRO_RECORD_HEADER_SUBTYPE);

		SyntroUtils.convertIntToUC2(param,
				message.get(), ptr + SyntroDefs.SYNTRO_RECORD_HEADER_PARAM);

		SyntroUtils.convertLongToUC8(timestamp,
				message.get(), ptr + SyntroDefs.SYNTRO_RECORD_HEADER_TIMESTAMP);

		ptr += SyntroDefs.SYNTRO_RECORD_HEADER_LENGTH;
		
		SyntroUtils.convertIntToUC4(avmuxSize, message.get(), ptr + SYNTRO_RECORD_AVMUX_MUXSIZE);
		SyntroUtils.convertIntToUC4(videoSize, message.get(), ptr + SYNTRO_RECORD_AVMUX_VIDEOSIZE);
		SyntroUtils.convertIntToUC4(audioSize, message.get(), ptr + SYNTRO_RECORD_AVMUX_AUDIOSIZE);
		message.get()[ptr + SYNTRO_RECORD_AVMUX_VIDEOSUBTYPE] = videoSubtype;
		message.get()[ptr + SYNTRO_RECORD_AVMUX_AUDIOSUBTYPE] = audioSubtype;
		SyntroUtils.convertIntToUC2(videoWidth, message.get(), ptr + SYNTRO_RECORD_AVMUX_VIDEOWIDTH);
		SyntroUtils.convertIntToUC2(videoHeight, message.get(), ptr + SYNTRO_RECORD_AVMUX_VIDEOHEIGHT);
		SyntroUtils.convertIntToUC2(videoFramerate, message.get(), ptr + SYNTRO_RECORD_AVMUX_FRAMERATE);
		SyntroUtils.convertIntToUC4(audioSampleRate, message.get(), ptr + SYNTRO_RECORD_AVMUX_AUDIOSAMPLERATE);
		SyntroUtils.convertIntToUC2(audioSampleSize, message.get(), ptr + SYNTRO_RECORD_AVMUX_AUDIOSAMPLESIZE);
		SyntroUtils.convertIntToUC2(audioChannels, message.get(), ptr + SYNTRO_RECORD_AVMUX_AUDIOCHANNELS);

		return true;
	}
}
