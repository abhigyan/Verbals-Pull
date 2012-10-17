/**
 * VerbalsPull1.0
 * 
 * FlickrLoad.java 17 okt. 2012
 */
package nl.tudelft.stitpronounce;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * This activity receives a user's location-tag and uses it to fetch random
 * public images from Flickr. The Flickr image fetch code is based on sample
 * code provided at:
 * http://android-er.blogspot.nl/2011/07/display-flickr-photos-
 * in-gallery-using.html
 * 
 * @author a.singh
 * @version 17 okt. 2012
 * 
 */
public class FlickrLoad extends Activity {

    public class BackgroundThread extends Thread {
        int cnt;
        volatile boolean running = false;

        @Override
        public void run() {
            String searchResult = QueryFlickr(text);
            myFlickrImage = ParseJSON(searchResult);
            handler.sendMessage(handler.obtainMessage());
        }

        void setRunning(boolean b) {
            running = b;
            cnt = 10;
        }
    }

    public class FlickrImage {
        String Farm;
        Bitmap FlickrBitmap;
        String Id;
        String Owner;
        String Secret;
        String Server;

        String Title;

        FlickrImage(String _Id, String _Owner, String _Secret, String _Server, String _Farm,
                    String _Title) {
            Id = _Id;
            Owner = _Owner;
            Secret = _Secret;
            Server = _Server;
            Farm = _Farm;
            Title = _Title;

            FlickrBitmap = preloadBitmap();
        }

        public Bitmap getBitmap() {
            return FlickrBitmap;
        }

        private Bitmap preloadBitmap() {
            Bitmap bm = null;

            String FlickrPhotoPath = "http://farm" + Farm + ".static.flickr.com/" + Server
                                     + "/" + Id + "_" + Secret + "_m.jpg";

            URL FlickrPhotoUrl = null;

            try {
                FlickrPhotoUrl = new URL(FlickrPhotoPath);

                HttpURLConnection httpConnection = (HttpURLConnection) FlickrPhotoUrl.openConnection();
                httpConnection.setDoInput(true);
                httpConnection.connect();
                InputStream inputStream = httpConnection.getInputStream();
                bm = BitmapFactory.decodeStream(inputStream);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bm;
        }
    }

    class FlickrAdapter extends BaseAdapter {
        private final Context context;
        private final FlickrImage[] FlickrAdapterImage;;

        FlickrAdapter(Context c, FlickrImage[] fImage) {
            context = c;
            FlickrAdapterImage = fImage;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return FlickrAdapterImage.length;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return FlickrAdapterImage[position];
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            ImageView image;
            if (convertView == null) {
                image = new ImageView(context);
                image.setLayoutParams(new Gallery.LayoutParams(LayoutParams.WRAP_CONTENT,
                                                               LayoutParams.WRAP_CONTENT));
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                image.setPadding(8, 8, 8, 8);
            } else {
                image = (ImageView) convertView;
            }

            image.setImageBitmap(FlickrAdapterImage[position].getBitmap());

            return image;
        }

    }

    private final String DEFAULT_SEARCH = "Holland";
    private String location;
    private String text = "Netherlands";
    BackgroundThread backgroundThread;
    Bitmap bmFlickr;
    // Replace API Key below with your API key
    String FlickrApiKey = "cc64b77f04506630b22ae77d1f2eba2d";
    String FlickrQuery_format = "&format=json";
    String FlickrQuery_key = "&api_key=";
    String FlickrQuery_nojsoncallback = "&nojsoncallback=1";
    String FlickrQuery_per_page = "&per_page=10";

    String FlickrQuery_tag = "&tags=";
    String FlickrQuery_url = "http://api.flickr.com/services/rest/?method=flickr.photos.search";
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
            photoBar.setAdapter(new FlickrAdapter(FlickrLoad.this, myFlickrImage));
        }
    };
    FlickrImage[] myFlickrImage;
    Gallery photoBar;

    ProgressDialog progressDialog;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flickrload);

        photoBar = (Gallery) findViewById(R.id.photoBar);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            text = bundle.getString("key");
            location = text;
            text = text.replaceAll(" ", "_");
            // Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        } else {
            text = DEFAULT_SEARCH;
            // Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        }

        String dialogTemp1 = getResources().getString(R.string.dia6);
        String dialogWait = dialogTemp1 + location;
        Intent intent = new Intent("nl.tudelft.stitpronounce.TTS");
        intent.putExtra("key", dialogWait);
        startActivity(intent);

        progressDialog = ProgressDialog.show(FlickrLoad.this, "ProgressDialog", "Wait!");
        backgroundThread = new BackgroundThread();
        backgroundThread.setRunning(true);
        backgroundThread.start();
    }

    private FlickrImage[] ParseJSON(String json) {

        FlickrImage[] flickrImage = null;

        bmFlickr = null;
        String flickrId;
        String flickrOwner;
        String flickrSecret;
        String flickrServer;
        String flickrFarm;
        String flickrTitle;

        try {
            JSONObject JsonObject = new JSONObject(json);
            JSONObject Json_photos = JsonObject.getJSONObject("photos");
            JSONArray JsonArray_photo = Json_photos.getJSONArray("photo");

            flickrImage = new FlickrImage[JsonArray_photo.length()];
            for (int i = 0; i < JsonArray_photo.length(); i++) {
                JSONObject FlickrPhoto = JsonArray_photo.getJSONObject(i);
                flickrId = FlickrPhoto.getString("id");
                flickrOwner = FlickrPhoto.getString("owner");
                flickrSecret = FlickrPhoto.getString("secret");
                flickrServer = FlickrPhoto.getString("server");
                flickrFarm = FlickrPhoto.getString("farm");
                flickrTitle = FlickrPhoto.getString("title");
                flickrImage[i] = new FlickrImage(flickrId, flickrOwner, flickrSecret,
                                                 flickrServer, flickrFarm, flickrTitle);
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return flickrImage;
    }

    private String QueryFlickr(String q) {

        String qResult = null;

        String qString = FlickrQuery_url + FlickrQuery_per_page + FlickrQuery_nojsoncallback
                         + FlickrQuery_format + FlickrQuery_tag + q + FlickrQuery_key
                         + FlickrApiKey;

        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(qString);

        try {
            HttpEntity httpEntity = httpClient.execute(httpGet).getEntity();

            if (httpEntity != null) {
                InputStream inputStream = httpEntity.getContent();
                Reader in = new InputStreamReader(inputStream);
                BufferedReader bufferedreader = new BufferedReader(in);
                StringBuilder stringBuilder = new StringBuilder();

                String stringReadLine = null;

                while ((stringReadLine = bufferedreader.readLine()) != null) {
                    stringBuilder.append(stringReadLine + "\n");
                }

                qResult = stringBuilder.toString();
                inputStream.close();
            }

        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return qResult;
    }

}