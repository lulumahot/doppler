package com.example.lulu.doppler;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.jtransforms.fft.DoubleFFT_1D;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class affichageRes extends ActionBarActivity {
    private LineChart chart;
    ArrayList xVals = new ArrayList();
    Boolean record=false;
    AudioManager am;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_affichage_res);
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
        AudioIn ai = new AudioIn();
        ai.execute();
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




    private class AudioIn extends AsyncTask<Void, short[], Void> {
        int sampleRateInHz = 44100;
        int channelconfig = AudioFormat.CHANNEL_IN_STEREO;
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        int bufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelconfig, audioFormat);
        AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, sampleRateInHz, channelconfig, audioFormat, bufferSize);
        AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
        short[] buffer = new short[bufferSize];
        int nombreDeShorts;


        @Override
        protected Void doInBackground(Void... params) {
            recorder.startRecording();
            at.play();
            System.out.println("iki : ok debut");
            while(recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {

                // Retourne le nombre de shorts lus, parce qu'il peut y en avoir moins que la taille du tableau
                nombreDeShorts = recorder.read(buffer, 0, bufferSize);
                //filtrage
                at.write(buffer,0,nombreDeShorts);

                System.out.println("iki : ca feed : " + nombreDeShorts);
                DoubleFFT_1D fftDo = new DoubleFFT_1D(buffer.length);
                double[] fft = new double[buffer.length * 2];
                for (int i =0 ; i<bufferSize ; i++)
                    fft[i]= (double)buffer[i];
                fftDo.realForwardFull(fft);
                double max = 0.0;
                int argmax=0;
                for (int j = 0; j<bufferSize; j++){
                    double a = Math.sqrt(fft[2 * j]*fft[2*j]  + fft[2*j+1]*fft[2*j+1]);
                    if( a > max){
                        max = a;
                        argmax= j;
                    }
                }
                double res=argmax * sampleRateInHz / bufferSize;
                System.out.println("ikik : " + argmax);
                if(res<15000) {
                    ajouterValeur(2 * res);
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
    }


}
