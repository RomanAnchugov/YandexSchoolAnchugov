package ru.photogallery.romananchugov.photogallery;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by romananchugov on 27.12.2017.
 */

public class FlickrFetchr {

    private static final String TAG = "FlickFetchr";
    private static final String API_KEY = "6f95d4475c52bb67f5a29c3b7dad2a13";
    private static final String FLICKR_URL = "https://api.flickr.com/services/rest/";
    private static final String FETCH_RECENTS_METHOD = "flickr.photos.getRecent";
    private static final String SEARCH_METHOD = "flickr.photos.search";
    private static final Uri ENDPOINT = Uri.parse("https://api.flickr.com/services/rest")
            .buildUpon()
            .appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s")
            .build();


    public static int PAGE = 1;

    public byte[] getUrlBytes(String urlSpec) throws IOException{
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage() + ": with" + urlSpec);
            }

            int byteRead = 0;
            byte[] buffer = new byte[1024];
            while((byteRead = in.read(buffer)) > 0){
                out.write(buffer, 0, byteRead);
            }
            out.close();
            return out.toByteArray();
        }finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException{
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryItem> fetchRecentPhotos(){
        String url = buildUrl(FETCH_RECENTS_METHOD, null);
        return downloadGalleryItems(url);
    }

    public List<GalleryItem> searchPhotos(String query){
        String url = buildUrl(SEARCH_METHOD, query);
        return downloadGalleryItems(url);
    }

    private List<GalleryItem> downloadGalleryItems(String url){

        List<GalleryItem> items = new ArrayList<>();

        try{
            String jsonString = getUrlString(url);
            //Log.i(TAG, "Recieved JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            items = parseItems(items, jsonBody);
        }catch (IOException ioe){
            Log.e(TAG, "Failed to fetch items", ioe);
        }catch (JSONException je){
            Log.e(TAG, "Failed to parse Json", je);
        }
        return items;
    }

    private List<GalleryItem> parseItems(List<GalleryItem> items, JSONObject jsonBody) throws IOException, JSONException{
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");

        for(int i = 0; i < photoJsonArray.length(); i++){
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);
            GalleryItem item;

            if(!photoJsonObject.has("url_s")){
                continue;
            }

            Gson gson = new GsonBuilder().create();
            item = gson.fromJson(photoJsonObject.toString(), GalleryItem.class);
            item.setmOwner(photoJsonObject.getString("owner"));
            items.add(item);
        }
        return items;
    }

    private String buildUrl(String method, String query){
        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                .appendQueryParameter("method", method)
                .appendQueryParameter("page", PAGE + "");
        if(method.equals(SEARCH_METHOD)){
            uriBuilder.appendQueryParameter("text", query);
        }

        return uriBuilder.build().toString();
    }
}
