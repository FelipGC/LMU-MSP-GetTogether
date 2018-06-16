package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Voice;


import android.media.MediaRecorder;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionManager;
import com.google.android.gms.nearby.connection.Payload;

import java.io.IOException;

import static android.os.Process.THREAD_PRIORITY_AUDIO;
import static android.os.Process.setThreadPriority;


public final class VoiceTransmission implements IVoice {

    private static final String TAG = "VoiceTransmission";
    /**
     * The background thread recording audio for us.
     */
    private Thread mThread;
    private MediaRecorder mRecorder;

    @Override
    public void startRecordingVoice() {
        mThread = new Thread() {
            @Override
            public void run() {
                Log.i(TAG, "startRecordingVoice()");
                setThreadPriority(THREAD_PRIORITY_AUDIO);
                ParcelFileDescriptor[] payloadPipe = new ParcelFileDescriptor[0];
                try {
                    payloadPipe = ParcelFileDescriptor.createPipe();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "createPipe() failed");
                    return;
                }

                // Send the first half of the payload (the read side) to Nearby Connections.
                ConnectionManager.getInstance().getPayloadSender().startSendingVoice(Payload.fromStream(payloadPipe[0]));
                // Use the second half of the payload (the write side) in AudioRecorder.
                ParcelFileDescriptor pfd = new ParcelFileDescriptor(payloadPipe[1]);
                //Set up the media recorder
                mRecorder = new MediaRecorder();
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mRecorder.setOutputFile(pfd.getFileDescriptor());
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                try {
                    mRecorder.prepare();
                    mRecorder.start();
                } catch (IOException e) {
                    Log.e(TAG, "prepare() failed");
                }
            }
        };
        mThread.start();
    }

    @Override
    public void stopRecordingVoice() {
        Log.i(TAG, "stopRecordingVoice()");
        if(mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }
}
