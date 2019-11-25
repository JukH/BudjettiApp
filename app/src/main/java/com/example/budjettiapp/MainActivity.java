package com.example.budjettiapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {
    Double lahtoSumma;
    Double vahennysMaara;
    //Muotoilu jotta nähdään 2.desimaalin tarkkuudella
    private static DecimalFormat df2 = new DecimalFormat("#.##");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button summanMuutos=(Button)findViewById(R.id.muokkausNappi);
        Button vahennys = (Button) findViewById(R.id.vahennysNappi);

        final TextView summanNaytto = (TextView) findViewById(R.id.naytaSumma);
        final EditText vahennyksenMaara = (EditText) findViewById(R.id.vahennettavaSumma);
        final EditText asetaSumma = (EditText) findViewById(R.id.summanAsetus);




        //Muutetaan lähtösumma ajankohtaiseksi
        summanMuutos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lahtoSumma = Double.valueOf(Float.valueOf(asetaSumma.getText().toString()));
                summanNaytto.setText("Jäljellä: "+ df2.format(lahtoSumma) + "€");
                asetaSumma.getText().clear();
            }
        });

        //Miinustetaan käytetty raha alkuperäisestä summasta
        vahennys.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vahennysMaara = Double.valueOf(vahennyksenMaara.getText().toString());
                lahtoSumma = lahtoSumma - vahennysMaara;
                summanNaytto.setText("Jäljellä: "+ df2.format(lahtoSumma) + "€");
                vahennyksenMaara.getText().clear();
            }
        });

    }
}
