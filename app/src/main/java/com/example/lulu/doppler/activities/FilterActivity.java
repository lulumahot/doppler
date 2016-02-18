package com.example.lulu.doppler.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import com.example.lulu.doppler.R;
import com.example.lulu.doppler.io.FileChooser;
import com.example.lulu.doppler.io.SaveSound;
import com.example.lulu.doppler.tools.WaveletFilter;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class FilterActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final ProgressDialog dialog = new ProgressDialog(this);
        final File res;
        new FileChooser(this).setFileListener(new FileChooser.FileSelectedListener() {
                                                      @Override public void fileSelected(final File file){
                                                          dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                                          dialog.setMessage("Filtrage en cours veuillez patienter");
                                                          dialog.setIndeterminate(true);
                                                          dialog.setCanceledOnTouchOutside(false);
                                                          //dialog.show();

                                                          Thread t = new Thread (new Runnable() {
                                                              public void run() {
                                                                  short[] in = getShortsFromFile(file);
                                                                  short[] bufferin = new short[4096];
                                                                  short[] bufferout = new short[4096];
                                                                  short[] out = new short[in.length];
                                                                  //System.out.println("pq" +in.length);
                                                                  for (int i =0 ; i < in.length-4096 ; i=i+4096){
                                                                      System.arraycopy(in, i, bufferin, 0, 4096);
                                                                      bufferout= WaveletFilter.filter(bufferin);
                                                                      //bufferout=bufferin;
                                                                      System.arraycopy(bufferout, 0, out, i, 4096);
                                                                      //System.out.println("on en est au tour" + i);
                                                                  }
                                                                  System.out.println("le filtrag est terminé");
                                                                  SaveSound ss =new SaveSound(out,44100,"filtre");
                                                                  try {
                                                                      ss.rawToWave();
                                                                  } catch (IOException e) {
                                                                      e.printStackTrace();
                                                                  }
                                                                  System.out.println("fin");
                                                              }
                                                          });
                                                          //dialog.show();
                                                          t.start();
                                                          dialog.show();
                                                          try {
                                                              t.join();
                                                          } catch (InterruptedException e) {
                                                              e.printStackTrace();
                                                          }

                                                          dialog.dismiss();
                                                          Toast.makeText(getApplicationContext(), "Votre fichier a été filtré et enregistré avec succès",
                                                                  Toast.LENGTH_LONG).show();

                                                      }}).showDialog();
        //NavUtils.navigateUpFromSameTask(this);
    }





    short[] getShortsFromFile(File file){
        byte[] byteInput = new byte[(int)file.length() - 44];
        short[] input = new short[(int)(byteInput.length / 2f)];
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            fis.read(byteInput, 44, byteInput.length - 45);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteBuffer.wrap(byteInput).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(input);
        return input;
    }

}
