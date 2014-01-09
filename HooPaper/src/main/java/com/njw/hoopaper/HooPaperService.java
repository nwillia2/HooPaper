package com.njw.hoopaper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.service.wallpaper.WallpaperService;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.io.InputStream;
import java.util.Calendar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Created by nwillia2 on 04/01/2014.
 */
public class HooPaperService extends WallpaperService {

    private Context context;

    @Override
    public Engine onCreateEngine() {
        return new HooPaperEngine(this);
    }

    private class HooPaperEngine extends Engine {
        private boolean mVisible = false;
        private final Handler mHandler = new Handler();
        private final Runnable mUpdateDisplay = new Runnable() {
            @Override
            public void run() {
                draw();
            }
        };
        private Context context;
        private Drawable currentImage;
        private long numberOfDraws;

        public HooPaperEngine(Context context) {
            this.context = context;
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            mVisible = visible;
            if (visible) {
                draw();
            } else {
                mHandler.removeCallbacks(mUpdateDisplay);
            }
        }
        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            draw();
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            mVisible = false;
            mHandler.removeCallbacks(mUpdateDisplay);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mVisible = false;
            mHandler.removeCallbacks(mUpdateDisplay);
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);

            if (!this.isPreview()){
                // this will initially start the alarm. we also need to restart it with a new value if the preferences change.
                Calendar cal = Calendar.getInstance();

                Intent intent = new Intent(context, UpdateService.class);
                // fire the service once, then schedule it
                startService(intent);

                PendingIntent pintent = PendingIntent.getService(context, 0, intent, 0);
                AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                // Get the new data every so often. Will be defined in the shared preferences.
                alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 30 * 1000, pintent);
            }
            numberOfDraws = 0;
        }

        private void draw() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas c = null;
            try {
                c = holder.lockCanvas();
                if (c != null) {
                    if (this.isPreview()){
                        // if it's a preview then just show a default image
                        // this can come from the drawable folder
                        // change this to preview image
                        currentImage = getResources().getDrawable(R.drawable.ic_launcher);
                    } else {
                        File f = new File(getFilesDir(), "CurrentImage.jpg");
                        if (f.exists()) {
                            currentImage = Drawable.createFromPath(context.getFilesDir().getAbsolutePath() + File.separator + "CurrentImage.jpg");

                            if (currentImage == null) {
                                // change this to loading image
                                currentImage = getResources().getDrawable(R.drawable.ic_launcher);
                            }
                            currentImage.setBounds(c.getClipBounds());
                            currentImage.draw(c);
                        } else {
                            // report that image couldn't be created
                            Log.d("HooPaper", "Couldn't create drawable image from retreived file.");
                        }

                    }


                    Paint paint = new Paint();
                    paint.setColor(Color.WHITE); // Text Color
                    paint.setStrokeWidth(25); // Text Size
                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)); // Text Overlapping Pattern
                    c.drawText("Number of draws: " + String.valueOf(numberOfDraws), 50, 50, paint);
                    numberOfDraws ++;
                }
            } finally {
                if (c != null)
                    holder.unlockCanvasAndPost(c);
            }
            mHandler.removeCallbacks(mUpdateDisplay);

            if (mVisible) {
                mHandler.postDelayed(mUpdateDisplay, 5*1000); // render every 5 seconds
            }

        }
    }
}
