package de.berstanio.personaldsb.ui.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RemoteViews;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;

import de.berstanio.ghgparser.DSBNotLoadableException;
import de.berstanio.ghgparser.User;
import de.berstanio.personaldsb.MainActivity;
import de.berstanio.personaldsb.R;
import de.berstanio.personaldsblib.PersonalDSBLib;

public class WebWidgetProvider extends AppWidgetProvider {

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        WebView.enableSlowWholeDocumentDraw();
        RemoteViews views=new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        Intent intent = new Intent(context, getClass());
        intent.setAction("onClick");
        intent.putExtra("ids", appWidgetId);

        views.setOnClickPendingIntent(R.id.draw, PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));

        Bundle options=appWidgetManager.getAppWidgetOptions(appWidgetId);
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();

        int maxHeight = (int) Math.round(options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT) * (metrics.densityDpi/160f));
        int minWidth = (int) Math.round(options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) * (metrics.densityDpi/160f));

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        CustomWebView webView = new CustomWebView(context);

        Calendar calendar = Calendar.getInstance();
        int week = calendar.get(Calendar.WEEK_OF_YEAR);
        StringBuilder stringBuilder = new StringBuilder();
        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    PersonalDSBLib.init(null, context.getFilesDir(), true);
                    String s = PersonalDSBLib.generateHTMLFile(week);
                    stringBuilder.append(s);
                } catch (DSBNotLoadableException | IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
        while (thread.isAlive()){
        }

        webView.layout(0, 0, minWidth + 50, maxHeight);
        webView.loadDataWithBaseURL(null, stringBuilder.toString(), "text/HTML", "UTF-8", null);

        Handler handler=new Handler();
        handler.postDelayed(() -> {

            Bitmap canvasBitmap = Bitmap.createBitmap(webView.getRealWidth(), maxHeight, Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(canvasBitmap);
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            webView.draw(canvas);

            views.setImageViewBitmap(R.id.draw, Bitmap.createBitmap(canvasBitmap, 0,0,minWidth, maxHeight));

            appWidgetManager.updateAppWidget(appWidgetId, views);


            SharedPreferences sharedPreferences = MainActivity.mainActivity.getSharedPreferences("widget", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("BitMap", bitMapToString(canvasBitmap));
            editor.putInt("MinWidth", minWidth);
            editor.putBoolean("flipped", false);
            editor.apply();

        }, 5000);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        RemoteViews views=new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        if ("onClick".equals(intent.getAction())){

            SharedPreferences sharedPreferences = MainActivity.mainActivity.getSharedPreferences("widget", Context.MODE_PRIVATE);
            int minWidth = sharedPreferences.getInt("MinWidth", 0);

            Bitmap bitmap = stringToBitMap(sharedPreferences.getString("BitMap", ""));
            Bitmap newBitMap;
            boolean flipped = sharedPreferences.getBoolean("flipped", false);
            if (!flipped) {
                 newBitMap = Bitmap.createBitmap(bitmap, bitmap.getWidth() - minWidth, 0, minWidth, bitmap.getHeight());
            }else {
                newBitMap = Bitmap.createBitmap(bitmap, 0, 0, minWidth, bitmap.getHeight());
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("flipped", !flipped);
            editor.apply();

            views.setImageViewBitmap(R.id.draw, newBitMap);
            AppWidgetManager.getInstance(context).updateAppWidget(new int[]{intent.getIntExtra("ids", 0)}, views);
        }
    }

    public String bitMapToString(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,0, byteArrayOutputStream);
        byte [] b = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    public Bitmap stringToBitMap(String encodedString){
        try {
            byte [] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }


    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateAppWidget(context, appWidgetManager, appWidgetIds[0]);
    }
}