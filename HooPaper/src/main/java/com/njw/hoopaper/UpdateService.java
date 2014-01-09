package com.njw.hoopaper;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.IBinder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by nwillia2 on 05/01/2014.
 */
public class UpdateService extends Service {
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            // retreive a new image from project weather and save it to disk
            File f  = new File(getFilesDir(), "CurrentImage.jpg");
            if (!f.exists()) {
                new GetImageTask().execute("http://farm8.staticflickr.com/7351/11593244336_d44d0b9c4c_b.jpg");
            }
            return Service.START_NOT_STICKY;
        }

        @Override
        public IBinder onBind(Intent intent) {
            //TODO for communication return IBinder implementation
            return null;
        }

    private class GetImageTask extends AsyncTask<String, Void, InputStream> {

        @Override
        protected InputStream doInBackground(String... strings) {
            InputStream input = null;
            try {
                URL url = new URL(strings[0]);
                HttpGet httpRequest = null;
                httpRequest = new HttpGet(url.toURI());

                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse response = httpclient.execute(httpRequest);

                HttpEntity entity = response.getEntity();
                BufferedHttpEntity b_entity = new BufferedHttpEntity(entity);
                input = b_entity.getContent();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return input;
        }

        @Override
        protected void onPostExecute(InputStream input) {
            try {
                // delete file
                File f =  new File(getFilesDir(), "CurrentImage.jpg");
                if (f.exists()) {
                    f.delete();
                }

                // save a new one
                FileOutputStream outputStream = openFileOutput("CurrentImage.jpg", Context.MODE_PRIVATE);
                Bitmap b = BitmapFactory.decodeStream(input);
                b.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
