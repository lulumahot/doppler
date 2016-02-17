package com.example.lulu.doppler.activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.lulu.doppler.R;
import com.example.lulu.doppler.tools.WaveletFilter;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    private com.github.amlcurran.showcaseview.targets.Target t1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageButton ib = (ImageButton) findViewById(R.id.imageButton);
        Button b = (Button) findViewById(R.id.button);
        t1 = new ViewTarget(R.id.imageButton, this);
        final ShowcaseView scv = new ShowcaseView.Builder(this)
                .setTarget(com.github.amlcurran.showcaseview.targets.Target.NONE)
                .setContentTitle("Aide :")
                .setContentText("Cliquez sur le coeur pour démarrer")
                .hideOnTouchOutside()
                .build();
        scv.setButtonText("OK");
        scv.hide();
        b.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                scv.setShowcase(t1, true);
                scv.show();
            }
        });



        ib.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ResultDisplayActivity.class);
                startActivity(intent);
                scv.hide();
            }
        });
        List<Double> adec = new ArrayList<>();
        for(int i=0 ; i<16 ; i++)
            adec.add((double) i);
        List<Double> d = WaveletFilter.decomposition(adec, WaveletFilter.HI_D,WaveletFilter.LO_D);
        for (int i = 0 ; i<d.size() ; i++)
            System.out.println("okok "+i + " " +d.get(i));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
