package de.berstanio.personaldsb.ui.thisweek;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

import de.berstanio.ghgparser.DSBNotLoadableException;
import de.berstanio.personaldsb.R;
import de.berstanio.personaldsb.Utils;
import de.berstanio.personaldsblib.PersonalDSBLib;

public class ThisWeekFragment extends Fragment {

    private WebView webView;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_thisweek, container, false);

        WebView webView = root.findViewById(R.id.thisweek);
        Utils.initialiseWebView(webView);

        this.webView = webView;

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        Calendar calendar = Calendar.getInstance(Locale.GERMANY);
        int week = calendar.get(Calendar.WEEK_OF_YEAR);
        Thread thread = new Thread(){
            @Override
            public void run() {
                String html;
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("plans", Context.MODE_PRIVATE);
                try {
                    html = PersonalDSBLib.generateHTMLFile(week);
                    sharedPreferences.edit().putString("thisweek", html).apply();
                } catch (DSBNotLoadableException | IOException | ClassNotFoundException e) {
                    html = sharedPreferences.getString("thisweek", "");
                    e.printStackTrace();
                    Utils.showStackTrace(e, getContext());
                }
                String finalHtml = html;
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> webView.loadDataWithBaseURL(null, finalHtml, "text/HTML", "UTF-8", null));

            }
        };
        thread.start();
    }
}