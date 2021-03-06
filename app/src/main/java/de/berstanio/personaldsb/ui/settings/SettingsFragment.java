package de.berstanio.personaldsb.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import de.berstanio.ghgparser.DSBNotLoadableException;
import de.berstanio.personaldsb.R;
import de.berstanio.personaldsb.Utils;
import de.berstanio.personaldsblib.PersonalDSBLib;

public class SettingsFragment extends Fragment {
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        SharedPreferences sharedPreferences = root.getContext().getSharedPreferences("darkmode", Context.MODE_PRIVATE);

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
                        PersonalDSBLib.init(getResources().openRawResource(R.raw.rawpage), getContext().getFilesDir(), isChecked);
                    } catch (DSBNotLoadableException e) {
                        e.printStackTrace();
                        Utils.showStackTrace(e, getContext());
                    }
                }
            };
            thread.start();
        });

        Switch dateSwitch = root.findViewById(R.id.dateSwitch);
        boolean date = sharedPreferences.getBoolean("DateSwitch", false);
        dateSwitch.setChecked(date);

        dateSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
            if (isChecked){
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
                Calendar cal = Calendar.getInstance(Locale.GERMANY);
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
            SharedPreferences planPref = root.getContext().getSharedPreferences("plans", Context.MODE_PRIVATE);
            planPref.edit().remove("nextweek").apply();
            planPref.edit().remove("freeroom").apply();
            planPref.edit().remove("thisweek").apply();
        });

        Button deleteUsers = root.findViewById(R.id.deleteUser);
        deleteUsers.setOnClickListener(v -> PersonalDSBLib.setUser(null));
        return root;
    }

}
