package de.berstanio.personaldsb.ui.nextweek;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Locale;

import de.berstanio.ghgparser.DSBNotLoadableException;
import de.berstanio.personaldsb.MainActivity;
import de.berstanio.personaldsb.R;
import de.berstanio.personaldsb.Utils;
import de.berstanio.personaldsblib.PersonalDSBLib;

public class NextWeekFragment extends Fragment {


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_nextweek, container, false);

        WebView webView  = root.findViewById(R.id.nextweek);
        Utils.initialiseWebView(webView);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        Calendar calendar = Calendar.getInstance(Locale.GERMANY);
        int week = calendar.get(Calendar.WEEK_OF_YEAR) + 1;

        Thread thread = new Thread() {
            @Override
            public void run() {
                String html;
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("plans", Context.MODE_PRIVATE);
                try {
                    html = PersonalDSBLib.generateHTMLFile(week);
                    sharedPreferences.edit().putString("nextweek", html).apply();
                } catch (DSBNotLoadableException | IOException | ClassNotFoundException e) {
                    html = sharedPreferences.getString("nextweek", "");
                    e.printStackTrace();
                    Utils.showStackTrace(e, getActivity());
                }
                String finalHtml = html;
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> ((WebView) getView().findViewById(R.id.nextweek)).loadDataWithBaseURL(null, finalHtml, "text/HTML", "UTF-8", null));

            }
        };
        thread.start();
    }
}