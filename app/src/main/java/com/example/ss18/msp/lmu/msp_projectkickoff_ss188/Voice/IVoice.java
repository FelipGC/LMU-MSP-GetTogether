package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Voice;


import java.io.InputStream;

public interface IVoice {
    void startRecordingVoice();
    void stopRecordingVoice();
    void playAudio(InputStream inputStream);
}
