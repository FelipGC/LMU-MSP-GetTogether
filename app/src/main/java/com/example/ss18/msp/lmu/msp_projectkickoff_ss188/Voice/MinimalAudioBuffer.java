package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Voice;

import android.media.AudioFormat;
import android.media.AudioTrack;

/**
 * A buffer with the smallest supported sample rate
 */
public class MinimalAudioBuffer {

    private static final int[] POSSIBLE_SAMPLE_RATES =
            new int[]{8000, 11025, 16000, 22050, 44100, 48000};

    final int size;
    final int sampleRate;
    final byte[] data;

    protected MinimalAudioBuffer() {
        int size = -1;
        int sampleRate = -1;

        // Iterate over all possible sample rates, and try to find the shortest one. The shorter
        // it is, the faster it'll stream.
        for (int rate : POSSIBLE_SAMPLE_RATES) {
            sampleRate = rate;
            size = getMinBufferSize(sampleRate);
            if (validSize(size)) break;
        }
        // If none of them were good, then just pick 1kb
        if (!validSize(size)) size = 1024;

        this.size = size;
        this.sampleRate = sampleRate;
        data = new byte[size];
    }

    public boolean validSize(int size) {
        return size != AudioTrack.ERROR && size != AudioTrack.ERROR_BAD_VALUE;
    }

    public int getMinBufferSize(int sampleRate) {
        return AudioTrack.getMinBufferSize(
                sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
    }
}
