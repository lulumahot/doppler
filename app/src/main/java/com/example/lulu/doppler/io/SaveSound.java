package com.example.lulu.doppler.io;

import android.os.Environment;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Lucien on 12/02/2016.
 */
public class SaveSound {

    private File waveFile;
    private short[] rawData;
    private int sampleRateInHz;

    public SaveSound(short[] rawData, int sampleRateInHz,String s){
        this.rawData=rawData;
        this.sampleRateInHz=sampleRateInHz;
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File (sdCard.getAbsolutePath() + "/EchoDoppler");
        dir.mkdirs();
        Calendar c = Calendar.getInstance();
        Date d = c.getTime();
        //int seconds = c.get(Calendar.SECOND);
        waveFile = new File(dir, s+d.toString()+".wav");
    }
    public SaveSound(short[] rawData, int sampleRateInHz){
        this.rawData=rawData;
        this.sampleRateInHz=sampleRateInHz;
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File (sdCard.getAbsolutePath() + "/EchoDoppler");
        dir.mkdirs();
        Calendar c = Calendar.getInstance();
        Date d = c.getTime();
        //int seconds = c.get(Calendar.SECOND);
        waveFile = new File(dir, d.toString()+".wav");
    }

    public File getWaveFile() {
        return waveFile;
    }

    public void setWaveFile(File waveFile) {
        this.waveFile = waveFile;
    }

    public short[] getRawData() {
        return rawData;
    }

    public void setRawData(short[] rawData) {
        this.rawData = rawData;
    }

    public int getSampleRateInHz() {
        return sampleRateInHz;
    }

    public void setSampleRateInHz(int sampleRateInHz) {
        this.sampleRateInHz = sampleRateInHz;
    }

    public void rawToWave() throws IOException {
        DataOutputStream output = null;
        System.out.println("okok" + rawData.length);
        try {
            output = new DataOutputStream(new FileOutputStream(waveFile));
            // WAVE header
            // see http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
            writeString(output, "RIFF"); // chunk id
            writeInt(output, 36 + rawData.length*2); // chunk size
            writeString(output, "WAVE"); // format
            writeString(output, "fmt "); // subchunk 1 id
            writeInt(output, 16); // subchunk 1 size
            writeShort(output, (short) 1); // audio format (1 = PCM)
            writeShort(output, (short) 1); // number of channels
            writeInt(output, sampleRateInHz); // sample rate
            writeInt(output, sampleRateInHz *2); // byte rate
            writeShort(output, (short) 2); // block align
            writeShort(output, (short) 16); // bits per sample
            writeString(output, "data"); // subchunk 2 id
            writeInt(output, rawData.length*2 ); // subchunk 2 size
            // Audio data (conversion big endian -> little endian)

            /*short[] shorts = new short[rawData.length / 2];
            ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);*/
            ByteBuffer bytes = ByteBuffer.allocate(rawData.length * 2);
            for (short s : rawData) {
                //System.out.println("tropico" + " " + s + " " + swap(s));
                bytes.putShort(swap(s));
            }
            output.write(bytes.array());
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    private void writeInt(final DataOutputStream output, final int value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
        output.write(value >> 16);
        output.write(value >> 24);
    }

    private void writeShort(final DataOutputStream output, final short value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
    }

    private void writeString(final DataOutputStream output, final String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            output.write(value.charAt(i));
        }
    }

    public static short swap (short value)
    {
        int b1 = value & 0xff;
        int b2 = (value >> 8) & 0xff;

        return (short) (b1 << 8 | b2 << 0);
    }
}
