package com.example.lulu.doppler;

import android.content.Intent;
import android.graphics.Color;
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

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class affichageRes extends ActionBarActivity {
    private LineChart chart;
    ArrayList xVals = new ArrayList();
    Boolean record=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_affichage_res);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        for(int i = 0 ; i<30 ; i++) {
            feedMultiple();
        }

    }

    private void feedMultiple() {

        new Thread(new Runnable() {

            @Override
            public void run() {
                for(int i = 0; i < 30; i++) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            ajouterValeur();
                            xVals.add("val");
                        }
                    });

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void ajouterValeur(){
        LineData d= chart.getData();
        LineDataSet s=null;
        if(d != null) {
            s = d.getDataSetByIndex(0);
            if (s == null) {
                s = createSet();
                d.addDataSet(s);
            }
            d.addXValue("");
            d.addEntry(new Entry((float) (Math.random() * 40), s.getEntryCount()), 0);


            // let the chart know it's data has changed
            chart.notifyDataSetChanged();

            chart.setVisibleXRangeMaximum(100);

            // move to the latest entry
            chart.moveViewToX(d.getXValCount()-30);



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
