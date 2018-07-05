package de.lmu.msp.gettogether.Voice;


import java.io.InputStream;

public interface IVoice {
    void startRecordingVoice();
    void stopRecordingVoice();
    void playAudio(InputStream inputStream);
}
