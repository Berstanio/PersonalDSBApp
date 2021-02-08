package de.berstanio.personaldsb;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import de.berstanio.ghgparser.DSBNotLoadableException;
import de.berstanio.personaldsblib.PersonalDSBLib;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    public static MainActivity mainActivity;
    public NavController navController;
    public Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;
        handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.mainActivity);
                builder.setMessage("Wenn du Internetzugang hast und das DSB funktioniert, lass dir den Fehlercode ausgeben und sende ihn Sebastian!");
                builder.setTitle("DSB Plan konnte leider nicht runtergeladen werden!");
                builder.setCancelable(true);
                builder.setPositiveButton("Die App hat KEINEN Fehler!", (dialog, id) -> {

                });

                String s = msg.obj.toString();
                builder.setNegativeButton("Die App hat einen Fehler!", (dialog, id) -> {
                    TextView showText = new TextView(MainActivity.mainActivity);
                    showText.setText(s);
                    showText.setTextIsSelectable(true);

                    AlertDialog.Builder error = new AlertDialog.Builder(MainActivity.mainActivity);
                    error.setView(showText);
                    error.setTitle("Sende den Fehler bitte an Sebastian!");
                    error.setCancelable(true);
                    error.setPositiveButton("Bitte kopier es in meinen Zwischenspeicher!", (dialog2, id2) -> {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("PersonalDSB FehlerCode", s);
                        clipboard.setPrimaryClip(clip);
                    });

                    error.setNegativeButton("Ich kopiere es selber!", (dialog2, id2) -> {

                    });
                    error.create().show();
                });

                builder.create().show();
            }
        };


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_thisweek, R.id.nav_nextweek, R.id.nav_freerooms, R.id.nav_readplan, R.id.nav_settings)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        this.navController = navController;
        SharedPreferences sharedPreferences = getSharedPreferences("darkmode", Context.MODE_PRIVATE);
        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    boolean externServer = sharedPreferences.getBoolean("ExternServer", true);
                    PersonalDSBLib.init(getResources().openRawResource(R.raw.rawpage), getFilesDir(), externServer);
                } catch (IOException | DSBNotLoadableException e) {
                    e.printStackTrace();
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    Message message = handler.obtainMessage(0, sw.toString());
                    message.sendToTarget();
                }
                if (PersonalDSBLib.getUser() == null){
                    runOnUiThread(() -> {
                        navController.navigate(R.id.nav_readplan);
                    });
                }
            }
        };
        thread.start();

        Thread night = new Thread(){
            @Override
            public void run() {
                while (thread.isAlive()){

                }
                if (sharedPreferences.getBoolean("DarkMode", false)){
                    runOnUiThread(() -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES));
                }else {
                    runOnUiThread(() -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO));
                }
            }
        };
        night.start();
        if (sharedPreferences.getBoolean("DateSwitch", false)){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
            Calendar cal = Calendar.getInstance(Locale.GERMANY);
            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());

            navigationView.getMenu().getItem(0).setTitle(simpleDateFormat.format(cal.getTime()));

            cal.add(Calendar.WEEK_OF_YEAR, 1);
            navigationView.getMenu().getItem(1).setTitle(simpleDateFormat.format(cal.getTime()));
        }
    }

    public void onAboutClick(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.mainActivity);
        builder.setMessage("Jsoup Copyright (c) 2009-2020 Jonathan Hedley, MIT license see https://jsoup.org/license for details\nJSON Copyright (c) 2002 JSON.org, MIT license see http://json.org/license.html for details");
        builder.setTitle("Lizenzen");
        builder.setCancelable(true);
        builder.setPositiveButton("Verstanden!", (dialog, id) -> {
        });

        builder.create().show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}