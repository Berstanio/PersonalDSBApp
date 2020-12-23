package de.berstanio.personaldsb.ui.settings;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

import de.berstanio.ghgparser.DSBNotLoadableException;
import de.berstanio.personaldsb.MainActivity;
import de.berstanio.personaldsb.R;
import de.berstanio.personaldsblib.PersonalDSBLib;

public class SettingsFragment extends Fragment {
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        SharedPreferences sharedPreferences = MainActivity.mainActivity.getSharedPreferences("darkmode", Context.MODE_PRIVATE);

        Switch darkTheme = root.findViewById(R.id.darkThemeSwitch);
        boolean checked = sharedPreferences.getBoolean("DarkMode", false);
        darkTheme.setChecked(checked);

        darkTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("DarkMode", isChecked);
            editor.apply();
        });

        Switch externSwitch = root.findViewById(R.id.externServer);
        boolean externServer = sharedPreferences.getBoolean("ExternServer", true);
        externSwitch.setChecked(externServer);

        externSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("ExternServer", isChecked);
            editor.apply();
            Thread thread = new Thread(){
                @Override
                public void run() {
                    try {
                        PersonalDSBLib.init(getResources().openRawResource(R.raw.rawpage), MainActivity.mainActivity.getFilesDir(), isChecked);
                    } catch (IOException | DSBNotLoadableException e) {
                        e.printStackTrace();
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw);
                        e.printStackTrace(pw);
                        Message message = MainActivity.mainActivity.handler.obtainMessage(0, sw.toString());
                        message.sendToTarget();
                    }
                }
            };
            thread.start();
        });

        Switch dateSwitch = root.findViewById(R.id.dateSwitch);
        boolean date = sharedPreferences.getBoolean("DateSwitch", false);
        dateSwitch.setChecked(date);

        dateSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NavigationView navigationView = MainActivity.mainActivity.findViewById(R.id.nav_view);
            if (isChecked){
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());

                navigationView.getMenu().getItem(0).setTitle(simpleDateFormat.format(cal.getTime()));

                cal.add(Calendar.WEEK_OF_YEAR, 1);
                navigationView.getMenu().getItem(1).setTitle(simpleDateFormat.format(cal.getTime()));
            }else {
                navigationView.getMenu().getItem(0).setTitle("Diese Woche");
                navigationView.getMenu().getItem(1).setTitle("Nächste Woche");
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("DateSwitch", isChecked);
            editor.apply();
        });


        Button deletePlans = root.findViewById(R.id.buttonReload);
        deletePlans.setOnClickListener(v -> {
            PersonalDSBLib.reloadPlans();
        });

        Button deleteUsers = root.findViewById(R.id.deleteUser);
        deleteUsers.setOnClickListener(v -> PersonalDSBLib.setUser(null));
        return root;
    }

}
