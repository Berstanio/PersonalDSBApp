package de.berstanio.personaldsb;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Configuration;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Utils {

    public static void showStackTrace(Exception e, Activity activity){
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);

        activity.runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage("Wenn du Internetzugang hast und das DSB funktioniert, lass dir den Fehlercode ausgeben und sende ihn Sebastian!");
            builder.setTitle("DSB Plan konnte leider nicht runtergeladen werden!");
            builder.setCancelable(true);
            builder.setPositiveButton("Die App hat KEINEN Fehler!", (dialog, id) -> {

            });

            String s = stringWriter.toString();
            builder.setNegativeButton("Die App hat einen Fehler!", (dialog, id) -> {
                TextView showText = new TextView(activity);
                showText.setText(s);
                showText.setTextIsSelectable(true);

                AlertDialog.Builder error = new AlertDialog.Builder(activity);
                error.setView(showText);
                error.setTitle("Sende den Fehler bitte an Sebastian!");
                error.setCancelable(true);
                error.setPositiveButton("Bitte kopier es in meinen Zwischenspeicher!", (dialog2, id2) -> {
                    @SuppressLint("WrongConstant") ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("PersonalDSB FehlerCode", s);
                    clipboard.setPrimaryClip(clip);
                });

                error.setNegativeButton("Ich kopiere es selber!", (dialog2, id2) -> {

                });
                error.create().show();
            });

            builder.create().show();
        });
    }

    public static void initialiseWebView(WebView webView){
        webView.setWebViewClient(new WebViewClient());
        if(WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            int nightModeFlags = webView.getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            switch (nightModeFlags) {
                case Configuration.UI_MODE_NIGHT_YES:
                case Configuration.UI_MODE_NIGHT_UNDEFINED:
                    WebSettingsCompat.setForceDark(webView.getSettings(), WebSettingsCompat.FORCE_DARK_ON);
                    break;
            }
        }
    }

}
