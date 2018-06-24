package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Voice;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities.AppLogicActivity;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionManager;
import com.google.android.gms.nearby.connection.Payload;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import static android.os.Process.THREAD_PRIORITY_AUDIO;
import static android.os.Process.setThreadPriority;
import static com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionManager.getAppLogicActivity;


public final class VoiceTransmission implements IVoice {

    private boolean isRecording = false;
    private static final String TAG = "VoiceTransmission";

    private ConnectionManager cM;

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ConnectionManager.ConnectionManagerBinder myBinder = (ConnectionManager.ConnectionManagerBinder) service;
            cM = myBinder.getService();
        }
    };

    public VoiceTransmission(){
        Intent intent = new Intent(getAppLogicActivity(), ConnectionManager.class);
        getAppLogicActivity().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        getAppLogicActivity().serviceConnections.add(mServiceConnection);
    }

    /**
     * The background thread recording audio for us.
     */
    private Thread mThread;

    @Override
    public void startRecordingVoice() {
        if (isRecording) {
            Log.i(TAG, "Already recording");
            return;
        }
        isRecording = true;
        final MinimalAudioBuffer buffer = new MinimalAudioBuffer();
        final AudioRecord record =
                new AudioRecord(
                        MediaRecorder.AudioSource.DEFAULT,
                        buffer.sampleRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        buffer.size);

        final ArrayList<OutputStream> outputStreamList = new ArrayList<>();
        for (final String id : cM.getEstablishedConnections().keySet()) {
            Log.i(TAG, "RECORDING FOR: " + id);
            final ParcelFileDescriptor[] payloadPipe;
            try {
                payloadPipe = ParcelFileDescriptor.createPipe();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to create ParcelFileDescriptor.createPipe()", e);
                break;
            }
            // Send the first half of the payload (the read side) to Nearby Connections.
            cM.getPayloadSender().
                    startSendingVoice(id, Payload.fromStream(payloadPipe[0]));
            //Create output stream
            final OutputStream mOutputStream = new ParcelFileDescriptor.AutoCloseOutputStream(payloadPipe[1]);
            outputStreamList.add(mOutputStream);
        }
        //Create Thread and start recording
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.w(TAG, "startRecordingVoice()");
                setThreadPriority(THREAD_PRIORITY_AUDIO);
                record.startRecording();
                if (record.getState() != AudioRecord.STATE_INITIALIZED) {
                    Log.w(TAG, "Failed starting recording");
                    return;
                }
                // Read the bytes from the AudioRecord and write them
                // to our output stream while recording.
                try {
                    while (isRecording) {
                        int len = record.read(buffer.data, 0, buffer.size);
                        if (len >= 0 && len <= buffer.size) {
                            for (OutputStream mOutputStream : outputStreamList) {
                                mOutputStream.write(buffer.data, 0, len);
                                mOutputStream.flush();
                            }
                        } else {
                            Log.w(TAG, "Unexpected length returned: " + len);
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Exception with recording stream", e);
                } finally {
                    Log.i(TAG, "Executing finally");
                    try {
                        for (OutputStream mOutputStream : outputStreamList) {
                            mOutputStream.close();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to close output stream", e);
                    }
                    finally {
                        try {
                            record.stop();
                        } catch (IllegalStateException e) {
                            Log.e(TAG, "Failed to stop AudioRecord", e);
                        }
                        finally {
                            stopThread(mThread);
                        }
                    }
                }
            }
        });
        mThread.start();
    }

    @Override
    public void stopRecordingVoice() {
        Log.i(TAG, "stopRecordingVoice()");
        isRecording = false;
    }

    @Override
    public synchronized void playAudio(final InputStream inputStream) {
        Log.i(TAG, "playAudio() InputStream: " + inputStream);
        mThread =
                new Thread() {
                    @Override
                    public void run() {
                        Log.i(TAG, "starting run() in mThread");
                        setThreadPriority(THREAD_PRIORITY_AUDIO);

                        MinimalAudioBuffer buffer = new MinimalAudioBuffer();
                        AudioTrack audioTrack =
                                new AudioTrack(
                                        AudioManager.STREAM_MUSIC,
                                        buffer.sampleRate,
                                        AudioFormat.CHANNEL_OUT_MONO,
                                        AudioFormat.ENCODING_PCM_16BIT,
                                        buffer.size,
                                        AudioTrack.MODE_STREAM);
                        audioTrack.play();

                        int length;
                        try {
                            while ((length = inputStream.read(buffer.data)) > 0) {
                                Log.i(TAG, length + "");
                                audioTrack.write(buffer.data, 0, length);
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Error while trying to play stream", e);
                        } finally {
                            Log.i(TAG, "Executing finally");
                            try {
                                inputStream.close();
                            } catch (IOException e) {
                                Log.e(TAG, "Failed to close input stream", e);
                            }
                            audioTrack.release();
                            stopThread(this);
                        }
                    }
                };
        mThread.start();
    }

    private void stopThread(Thread thread) {
        try {
            thread.join();
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted while joining AudioRecorder thread", e);
            Thread.currentThread().interrupt();
        }
        //Track finished playing
        Toast.makeText(ConnectionManager.getAppLogicActivity(), "Ende der Sprachnachricht.", Toast.LENGTH_SHORT).show();
    }
}
