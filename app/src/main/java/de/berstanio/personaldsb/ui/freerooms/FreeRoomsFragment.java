package de.berstanio.personaldsb.ui.freerooms;

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

import org.json.JSONException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import de.berstanio.ghgparser.DSBNotLoadableException;
import de.berstanio.personaldsb.MainActivity;
import de.berstanio.personaldsb.R;
import de.berstanio.personaldsblib.FreeRoomDSB;

public class FreeRoomsFragment extends Fragment {
    
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_freerooms, container, false);
        
        Thread thread = new Thread(){
            @Override
            public void run() {
                String html;
                SharedPreferences sharedPreferences = MainActivity.mainActivity.getSharedPreferences("plans", Context.MODE_PRIVATE);
                try {
                    html = FreeRoomDSB.refresh(getResources().openRawResource(R.raw.rawpage));

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("freeroom", html);
                    editor.apply();


                } catch (IOException | ClassNotFoundException | DSBNotLoadableException e) {
                    html = sharedPreferences.getString("freeroom", "");
                    e.printStackTrace();
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    Message message = MainActivity.mainActivity.handler.obtainMessage(0, sw.toString());
                    message.sendToTarget();
                }

                String finalHtml = html;
                MainActivity.mainActivity.runOnUiThread(() -> {
                    WebView webView = MainActivity.mainActivity.findViewById(R.id.freeroomview);
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
                    webView.loadDataWithBaseURL(null, finalHtml, "text/HTML", "UTF-8", null);
                });
            }
        };
        thread.start();

        return root;
    }
}
