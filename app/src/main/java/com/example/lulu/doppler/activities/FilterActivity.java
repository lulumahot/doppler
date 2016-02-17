package com.example.lulu.doppler.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
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
                                                          dialog.show();
                                                          //dialog.cancel();
                                                          short[] in = getShortsFromFile(file);
                                                          short[] bufferin = new short[4096];
                                                          short[] bufferout = new short[4096];
                                                          short[] out = new short[in.length];
                                                          System.out.println("pq" +in.length);
                                                          /*for (int i =0 ; i < in.length ; i=i+4096){
                                                              for (int j = 0; j<4096 ; j++){
                                                                  bufferin[j]=in[j+i];
                                                              }
                                                              //bufferout= WaveletFilter.filter(bufferin);
                                                              bufferout=bufferin;
                                                              for (int j = 0; j<4096 ; j++){
                                                                  out[j+i]=bufferout[j];
                                                              }
                                                          }
                                                          SaveSound ss =new SaveSound(out,44100,"filtre");*/
                                                          dialog.cancel();
                                                          Toast.makeText(getApplicationContext(), "Votre fichier a été filtré et enregistré avec succès",
                                                                  Toast.LENGTH_SHORT).show();
                                                      }}).showDialog();
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
