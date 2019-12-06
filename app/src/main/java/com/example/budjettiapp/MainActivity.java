package com.example.budjettiapp;

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.time.Year;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {


    float lahtoSumma;
    float lisaysTaiVahennysMaara;
    float paivakohtainenRahaMaara;
    int paivat;

    Context context = this;

    //Muotoilu jotta nähdään 2.desimaalin tarkkuudella
    private static DecimalFormat df2 = new DecimalFormat("#.##");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Määritetään napit
        Button summanMuutos=(Button)findViewById(R.id.muokkausNappi);
        Button vahennys = (Button) findViewById(R.id.vahennysNappi);
        Button lisäys = (Button) findViewById(R.id.lisaysNappi);

        final TextView summanNaytto = (TextView) findViewById(R.id.naytaSumma);
        final EditText vahennyksenMaara = (EditText) findViewById(R.id.vahennettavaSumma);
        final EditText asetaSumma = (EditText) findViewById(R.id.summanAsetus);
        final TextView paivakohtainenBudjetti = (TextView) findViewById(R.id.paivaBudjettiTeksti);
        final EditText ilmoitaPaivat = (EditText) findViewById(R.id.paivienMaaraInput);
        final TextView paivienMaara = (TextView) findViewById(R.id.paivienMaaraTeksti);


        //Otetaan käyttöön shared preferences tiedon tallentamista varten
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();

        //Haetaan data, jos sitä on ennestään
        if(!summanNaytto.getText().toString().matches("")) {
            String rahaMäärä = pref.getString("summa", null);
            if (rahaMäärä != null) {
                rahaMäärä = rahaMäärä.replace(",", ".");
                lahtoSumma = Float.parseFloat(rahaMäärä);
                summanNaytto.setText("Käytettävissä: " + String.format("%.2f", lahtoSumma) + "€");
            }

        }

        if(paivakohtainenBudjetti.getText().toString().matches("")){
            String tallennettuTietoPaivista = pref.getString("paivaBudjetti", null);
            if(tallennettuTietoPaivista != null) {
                tallennettuTietoPaivista = tallennettuTietoPaivista.replace(",", ".");

                paivakohtainenRahaMaara = Float.parseFloat(tallennettuTietoPaivista);
                paivakohtainenBudjetti.setText("Käytettävissä per päivä: " + df2.format(paivakohtainenRahaMaara) + "€");
            }
        }

        if(paivienMaara.getText().toString().matches("")){
            paivat = pref.getInt("paivat",0);
            paivienMaara.setText("Päiviä jäljellä: " + paivat);
        }

        //////////////////////////////////////////////////////////////////

        //Päivän vaihtuessa vähennetään 1 päivä päivälaskurista
        DatePicker datePicker = new DatePicker(this);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {

            @Override
            public void onDateChanged(DatePicker datePicker, int year, int month, int dayOfMonth) {
                paivat =  paivat - 1;
                paivienMaara.setText("Päiviä jäljellä: " + paivat);
            }
        });

        //////////////////////////////////////////////////////////////////


        //Muutetaan lähtösumma ajankohtaiseksi
        summanMuutos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!asetaSumma.getText().toString().matches("")&& !ilmoitaPaivat.getText().toString().matches("")) {
                    lahtoSumma = Float.valueOf(asetaSumma.getText().toString());
                    summanNaytto.setText("Käytettävissä: " + String.format("%.2f", lahtoSumma) + "€");
                    //Tallennetaan lähtösumma
                    editor.putString("summa", df2.format(lahtoSumma));


                    String paivienmaara = ilmoitaPaivat.getText().toString();
                    paivat = Integer.parseInt(paivienmaara);
                    paivakohtainenRahaMaara = lahtoSumma / paivat;
                    paivakohtainenBudjetti.setText("Käytettävissä per päivä: " + df2.format(paivakohtainenRahaMaara) + "€");

                    //Tallennetaan päiväkohtainen summa
                    editor.putString("paivaBudjetti", df2.format(paivakohtainenRahaMaara));


                    ilmoitaPaivat.getText().clear();
                    asetaSumma.getText().clear();

                    //Asetetaan teksti jäljellä olevien päivien näyttöä varten
                    paivienMaara.setText("Päiviä jäljellä: " + paivat);

                    //Tallennetaan päivien määrä
                    editor.putInt("paivat", paivat);

                    editor.apply();
//////////////////
                      //Koitetaan päivittää widget 6.12.19 LÄHTÖSUMMA
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                    RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.budjetti_widget);
                    ComponentName thisWidget = new ComponentName(context, BudjettiWidget.class);
                    remoteViews.setTextViewText(R.id.widget_lähtösumma_text, "Käytettävissä:" + df2.format(lahtoSumma) + "€");
                    //Koitetaan päivittää widget 6.12.19 PÄIVÄT
                    remoteViews.setTextViewText(R.id.widget_päiviä_teksti, "Päiviä jäljellä: " + paivat);
                    remoteViews.setTextViewText(R.id.widget_päiväkohtainen_rahamäärä_text, "Per päivä: " + df2.format(paivakohtainenRahaMaara) + "€");
                    appWidgetManager.updateAppWidget(thisWidget, remoteViews);


                    ///////////////////////////

                } else {
                    Toast.makeText(getApplicationContext(),"Anna budjetti sekä päivien määrä",Toast. LENGTH_LONG).show();
                }

            }
        });

        //Miinustetaan käytetty raha alkuperäisestä summasta
        vahennys.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!vahennyksenMaara.getText().toString().matches("")) {
                    lisaysTaiVahennysMaara = Float.valueOf(vahennyksenMaara.getText().toString());
                    lahtoSumma = lahtoSumma - lisaysTaiVahennysMaara;
                    summanNaytto.setText("Jäljellä: " + df2.format(lahtoSumma) + "€");
                    vahennyksenMaara.getText().clear();
                    paivakohtainenRahaMaara = lahtoSumma / paivat;
                    paivakohtainenBudjetti.setText("Per päivä: " + String.format("%.2f", paivakohtainenRahaMaara) + "€");

                    //Tallenetaan summa
                    editor.putString("summa", String.format("%.2f", lahtoSumma));

                    //Tallennetaan päiväkohtainen summa
                    editor.putString("paivaBudjetti", String.format("%.2f", paivakohtainenRahaMaara));

                    //Tallennetaan päivien määrä
                    editor.putInt("paivat", paivat);

                    editor.apply();

                    //KOITETAAN PÄIVITTÄÄ WIDGET
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                    RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.budjetti_widget);
                    ComponentName thisWidget = new ComponentName(context, BudjettiWidget.class);
                    remoteViews.setTextViewText(R.id.widget_lähtösumma_text, "Käytettävissä:" + df2.format(lahtoSumma) + "€");
                    //Koitetaan päivittää widget 6.12.19 PÄIVÄT
                    remoteViews.setTextViewText(R.id.widget_päiviä_teksti, "Päiviä jäljellä: " + paivat);
                    remoteViews.setTextViewText(R.id.widget_päiväkohtainen_rahamäärä_text, "Per päivä: " + df2.format(paivakohtainenRahaMaara) + "€");
                    appWidgetManager.updateAppWidget(thisWidget, remoteViews);
                    //


                } else {
                    Toast.makeText(getApplicationContext(), "Anna vähennettävä summa", Toast.LENGTH_LONG).show();
                }
            }
        });

        //Lisätään saatu raha kokonaisbudjettiin
        lisäys.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!vahennyksenMaara.getText().toString().matches("")) {
                    lisaysTaiVahennysMaara = Float.valueOf(vahennyksenMaara.getText().toString());
                    lahtoSumma = lahtoSumma + lisaysTaiVahennysMaara;
                    summanNaytto.setText("Jäljellä: " + df2.format(lahtoSumma) + "€");
                    vahennyksenMaara.getText().clear();
                    paivakohtainenRahaMaara = lahtoSumma / paivat;
                    paivakohtainenBudjetti.setText("Per päivä: " + String.format("%.2f", paivakohtainenRahaMaara) + "€");

                    //Tallenetaan summa
                    editor.putString("summa", df2.format(lahtoSumma));

                    //Tallennetaan päiväkohtainen summa
                    editor.putString("paivaBudjetti", df2.format(paivakohtainenRahaMaara));

                    //Tallennetaan päivien määrä
                    editor.putInt("paivat", paivat);

                    editor.apply();

                    //KOITETAAN PÄIVITTÄÄ WIDGET
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                    RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.budjetti_widget);
                    ComponentName thisWidget = new ComponentName(context, BudjettiWidget.class);
                    remoteViews.setTextViewText(R.id.widget_lähtösumma_text, "Käytettävissä:" + df2.format(lahtoSumma) + "€");
                    //Koitetaan päivittää widget 6.12.19 PÄIVÄT
                    remoteViews.setTextViewText(R.id.widget_päiviä_teksti, "Päiviä jäljellä: " + paivat);
                    remoteViews.setTextViewText(R.id.widget_päiväkohtainen_rahamäärä_text, "Per päivä: " + df2.format(paivakohtainenRahaMaara) + "€");
                    appWidgetManager.updateAppWidget(thisWidget, remoteViews);
                    //
                } else {
                    Toast.makeText(getApplicationContext(), "Anna lisättävä summa", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    /*
    private void päivitäPäivät(Calendar kalenteri) {
        paivat -= 1;
    } */



    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.

        double summanNaytto = savedInstanceState.getDouble("summanNaytto");

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.

        savedInstanceState.putDouble("summanNaytto", 1.9);
    }
}
