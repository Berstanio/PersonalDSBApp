package de.berstanio.personaldsb.ui.nextweek;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
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
import de.berstanio.personaldsblib.PersonalDSBLib;

public class NextWeekFragment extends Fragment {


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_nextweek, container, false);
        Calendar calendar = Calendar.getInstance(Locale.GERMANY);
        int week = calendar.get(Calendar.WEEK_OF_YEAR) + 1;
        WebView webView  = root.findViewById(R.id.nextweek);
        webView.setWebViewClient(new WebViewClient());
        if(WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            int nightModeFlags = getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            switch (nightModeFlags) {
                case Configuration.UI_MODE_NIGHT_YES:
                case Configuration.UI_MODE_NIGHT_UNDEFINED:
                    WebSettingsCompat.setForceDark(webView.getSettings(), WebSettingsCompat.FORCE_DARK_ON);
                    break;
            }
        }
        Thread thread = new Thread(){
            @Override
            public void run() {
                String html;
                SharedPreferences sharedPreferences = MainActivity.mainActivity.getSharedPreferences("plans", Context.MODE_PRIVATE);
                try {
                    html = PersonalDSBLib.generateHTMLFile(week);
                    sharedPreferences.edit().putString("nextweek", html).apply();
                } catch (DSBNotLoadableException | IOException | ClassNotFoundException e) {
                    html = sharedPreferences.getString("nextweek", "");
                    e.printStackTrace();
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    Message message = MainActivity.mainActivity.handler.obtainMessage(0, sw.toString());
                    message.sendToTarget();
                }
                String finalHtml = html;
                MainActivity.mainActivity.runOnUiThread(() -> webView.loadDataWithBaseURL(null, finalHtml, "text/HTML", "UTF-8", null));

            }
        };
        thread.start();
        return root;
    }
}