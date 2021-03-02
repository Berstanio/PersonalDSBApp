package de.berstanio.personaldsb.ui.freerooms;

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

import de.berstanio.ghgparser.DSBNotLoadableException;
import de.berstanio.personaldsb.R;
import de.berstanio.personaldsb.Utils;
import de.berstanio.personaldsblib.FreeRoomDSB;

public class FreeRoomsFragment extends Fragment {
    
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_freerooms, container, false);
        WebView webView = root.findViewById(R.id.freeroomview);
        Utils.initialiseWebView(webView);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        Thread thread = new Thread(){
            @Override
            public void run() {
                String html;
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("plans", Context.MODE_PRIVATE);
                try {
                    html = FreeRoomDSB.refresh(getResources().openRawResource(R.raw.rawpage));

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("freeroom", html);
                    editor.apply();
                } catch (IOException | ClassNotFoundException | DSBNotLoadableException e) {
                    html = sharedPreferences.getString("freeroom", "");
                    e.printStackTrace();
                    Utils.showStackTrace(e, getActivity());
                }

                String finalHtml = html;
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> ((WebView) getView().findViewById(R.id.freeroomview)).loadDataWithBaseURL(null, finalHtml, "text/HTML", "UTF-8", null));
            }
        };
        thread.start();
    }
}
