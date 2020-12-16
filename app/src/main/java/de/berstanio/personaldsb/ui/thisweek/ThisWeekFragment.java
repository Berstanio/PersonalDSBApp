package de.berstanio.personaldsb.ui.thisweek;

import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;

import de.berstanio.ghgparser.DSBNotLoadableException;
import de.berstanio.personaldsb.MainActivity;
import de.berstanio.personaldsb.R;
import de.berstanio.personaldsblib.PersonalDSBLib;

public class ThisWeekFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_thisweek, container, false);

        Calendar calendar = Calendar.getInstance();
        int week = calendar.get(Calendar.WEEK_OF_YEAR);
        WebView webView  = root.findViewById(R.id.thisweek);
        webView.setWebViewClient(new WebViewClient());
        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    String s = PersonalDSBLib.generateHTMLFile(week);
                    MainActivity.mainActivity.runOnUiThread(() -> webView.loadDataWithBaseURL(null, s, "text/HTML", "UTF-8", null));
                } catch (DSBNotLoadableException e) {
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
        return root;
    }

}