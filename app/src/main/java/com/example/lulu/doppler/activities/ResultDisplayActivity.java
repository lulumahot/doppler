package com.example.lulu.doppler.activities;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ImageButton;

import com.example.lulu.doppler.R;
import com.example.lulu.doppler.io.SoundRecorder;
import com.example.lulu.doppler.listeners.OnSoundReadListener;
import com.example.lulu.doppler.tools.WaveletFilter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.jtransforms.fft.DoubleFFT_1D;

import java.util.ArrayList;

public class ResultDisplayActivity extends ActionBarActivity {
    private LineChart chart;
    ArrayList xVals = new ArrayList();
    Boolean record=false;
    AudioManager am;

    AudioTrack at;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_display);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Context context = getApplicationContext();
        am=(AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        //am.setMode(AudioManager.MODE_IN_CALL);
        //am.setSpeakerphoneOn(true);
        //am.setMicrophoneMute(true);

        ImageButton b = (ImageButton) findViewById(R.id.register);
        b.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ImageButton b = (ImageButton) findViewById(R.id.register);
                if(record==false) {
                    b.setImageResource(R.drawable.stop);
                    record=true;
                }else{
                    b.setImageResource(R.drawable.record);
                    record=false;
                }
                
            }
        });
        chart = (LineChart) findViewById(R.id.chart);
        // no description text
        chart.setNoDataTextDescription("You need to provide data for the chart.");
        // enable touch gestures
        chart.setTouchEnabled(true);

        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisRight().setDrawGridLines(false);

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(true);
        LineData data = new LineData();
        chart.setData(data);

        final int sampleRateInHz = 44100;
        SoundRecorder recorder = new SoundRecorder(sampleRateInHz, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        final int bufferSize = recorder.getBufferSize();

        at = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz, AudioFormat.CHANNEL_OUT_STEREO,
                recorder.getAudioFormat(), bufferSize, AudioTrack.MODE_STREAM);

        at.play();

        recorder.addListener(new OnSoundReadListener() {
            @Override
            public void OnReceive(short[] buffer, int nbRealValues) {
                at.write(buffer, 0, nbRealValues);

                WaveletFilter.filter(buffer);

                System.out.println("iki : ca feed : " + nbRealValues);
                DoubleFFT_1D fftDo = new DoubleFFT_1D(buffer.length);
                double[] fft = new double[buffer.length * 2];

                for (int i = 0; i < bufferSize; i++) {
                    fft[i] = (double) buffer[i];
                }

                fftDo.realForwardFull(fft);
                double max = 0.0;
                int argmax = 0;
                for (int j = 0; j < bufferSize; j++) {
                    double a = Math.sqrt(fft[2 * j] * fft[2 * j] + fft[2 * j + 1] * fft[2 * j + 1]);
                    if (a > max) {
                        max = a;
                        argmax = j;
                    }
                }
                double res = argmax * sampleRateInHz / bufferSize;
                System.out.println("ikik : " + argmax);

                if (res < 15000) {
                    ajouterValeur(2 * res);
                }
            }
        });

        recorder.execute();
    }

    private void feedMultiple() {

        new Thread(new Runnable() {

            @Override
            public void run() {
                for(int i = 0; i < 30; i++) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            xVals.add("val");
                        }
                    });
                }
            }
        }).start();
    }

    protected void ajouterValeur(double data){
        LineData d= chart.getData();
        LineDataSet s=null;
        if(d != null) {
            s = d.getDataSetByIndex(0);
            if (s == null) {
                s = createSet();
                d.addDataSet(s);
            }
            d.addXValue("");
            d.addEntry(new Entry((float) data, s.getEntryCount()), 0);


            // let the chart know it's data has changed
            chart.notifyDataSetChanged();

            chart.setVisibleXRangeMaximum(50);

            // move to the latest entry

            chart.moveViewToX(d.getXValCount()+25);


        }
    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, null);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setDrawCircles(false);
        set.setLineWidth(2f);
        set.setFillAlpha(255);
        set.setFillColor(Color.BLACK);
        set.setColor(Color.BLACK);
        //set.setDrawFilled(true);
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        set.setDrawCubic(true);
        return set;
    }

}
