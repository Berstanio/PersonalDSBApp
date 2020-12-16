package de.berstanio.personaldsb.ui.freerooms;

import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.json.JSONException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

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
                try {
                    String html = FreeRoomDSB.refresh(getResources().openRawResource(R.raw.raum));
                    MainActivity.mainActivity.runOnUiThread(() -> {
                        WebView webView = MainActivity.mainActivity.findViewById(R.id.freeroomview);
                        webView.setWebViewClient(new WebViewClient());
                        webView.loadDataWithBaseURL(null, html, "text/HTML", "UTF-8", null);
                    });
                } catch (IOException | JSONException e) {
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
