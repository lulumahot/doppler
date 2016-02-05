package com.example.lulu.doppler.io;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;

import com.example.lulu.doppler.listeners.OnSoundReadListener;

import java.util.ArrayList;
import java.util.Collection;

public class SoundRecorder extends AsyncTask<Void, short[], Void> {
    private int sampleRateInHz;
    private int channelConfig;
    private int audioFormat;
    int bufferSize;
    AudioRecord recorder;

    short[] buffer;
    int nbRealValues;

    private final Collection<OnSoundReadListener> listeners = new ArrayList<>();

    public SoundRecorder(int sampleRateInHz, int channelConfig, int audioFormat) {
        this.sampleRateInHz = sampleRateInHz;
        this.channelConfig = channelConfig;
        this.audioFormat = audioFormat;

        this.bufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        this.recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, sampleRateInHz, channelConfig, audioFormat, bufferSize);
        this.buffer = new short[bufferSize];
    }

    @Override
    protected Void doInBackground(Void... params) {
        recorder.startRecording();
        System.out.println("iki : ok debut");
        while(recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {

            // Retourne le nombre de valeurs réelles, parce qu'il peut y en avoir moins que la taille du tableau
            nbRealValues = recorder.read(buffer, 0, bufferSize);

            for(OnSoundReadListener listener : listeners) {
                listener.OnReceive(buffer, nbRealValues);
            }
        }

        return null;
    }


    @Override
    protected void onPostExecute(Void result) {
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    public void addListener(OnSoundReadListener listener) {
        listeners.add(listener);
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public int getSampleRateInHz() {
        return sampleRateInHz;
    }

    public int getChannelConfig() {
        return channelConfig;
    }

    public int getAudioFormat() {
        return audioFormat;
    }
 }
