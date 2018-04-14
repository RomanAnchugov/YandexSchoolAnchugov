package ru.romananchugov.yandexschoolanchugov.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.exceptions.ServerException;
import com.yandex.disk.rest.json.Link;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.romananchugov.yandexschoolanchugov.R;
import ru.romananchugov.yandexschoolanchugov.adapters.UploadingProgressDialog;
import ru.romananchugov.yandexschoolanchugov.fragmetns.GalleryListFragment;
import ru.romananchugov.yandexschoolanchugov.interfaces.DiskClientApi;
import ru.romananchugov.yandexschoolanchugov.models.UploaderWrapper;
import ru.romananchugov.yandexschoolanchugov.network.RestClientUtil;

import static ru.romananchugov.yandexschoolanchugov.utils.Constants.BASE_URL;
import static ru.romananchugov.yandexschoolanchugov.utils.Constants.PICK_IMAGE;

public class MainActivity extends AppCompatActivity {

    public static final String FRAGMENT_TAG = "gallery";
    public static final String CLIENT_ID = "959666c7ee9942f6b9ffec283205e35c";
    public static final String AUTH_URL = "https://oauth.yandex.ru/authorize?response_type=token&client_id=" + CLIENT_ID;
    public static final String USERNAME = "ymra.username";
    public static final String TOKEN = "ymra.token";
    private static final String TAG = "MainActivity";

    private UploadingProgressDialog progressFragmentDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressFragmentDialog = new UploadingProgressDialog();

        if (getIntent() != null && getIntent().getData() != null) {
            onLogin();
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String token = preferences.getString(TOKEN, null);
        Log.i(TAG, "onCreate: " + token);
        if (token == null) {
            startLogin();
            return;
        }

        if (savedInstanceState == null) {
            startFragment();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case PICK_IMAGE:
                if(resultCode == Activity.RESULT_OK && data != null){
                    Uri uri = data.getData();
                    File file = new File(getPath(uri));

                    if(file.exists()) {
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                        String username = preferences.getString(MainActivity.USERNAME, null);
                        String token = preferences.getString(TOKEN, null);
                        Credentials credentials = new Credentials(username, token);
                        getUploadLink(credentials, file);
                    }else{
                        Toast.makeText(this, "You should choose image from gallery", Toast.LENGTH_LONG).show();
                    }
                }
                break;

        }
    }

    private void startFragment() {
        Log.i(TAG, "startFragment: startFragment()");
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, GalleryListFragment.newInstance(), FRAGMENT_TAG)
                .commit();
    }

    public void startLogin() {
        Log.i(TAG, "startLogin: startLogin()");

        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(AUTH_URL)));
        //new AuthDialogFragment().show(getSupportFragmentManager(), "auth");
    }

    public void onLogin() {
        Log.i(TAG, "onLogin: onLogin()");

        Uri data = getIntent().getData();
        setIntent(null);

        Pattern pattern = Pattern.compile("access_token=(.*?)(&|$)");
        Matcher matcher = pattern.matcher(data.toString());

        if (matcher.find()) {
            final String token = matcher.group(1);
            if (!TextUtils.isEmpty(token)) {
                Log.d(TAG, "onLogin: token: " + token);
                saveToken(token);
            } else {
                Log.w(TAG, "onRegistrationSuccess: empty token");
            }
        } else {
            Log.w(TAG, "onRegistrationSuccess: token not found in return url");
        }
    }

    private void saveToken(String token) {
        Log.i(TAG, "saveToken: saveToken()");
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString(USERNAME, "");
        editor.putString(TOKEN, token);
        editor.apply();
    }

    public void getUploadLink(final Credentials credentials, final File file){
        final String token = credentials.getToken();

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create());

        final Retrofit retrofit = builder.build();

        DiskClientApi diskClientApi = retrofit.create(DiskClientApi.class);
        final Call<Link> call = diskClientApi.getUploadLink("OAuth " + token, file.getName());

        call.enqueue(new Callback<Link>() {
            @Override
            public void onResponse(Call<Link> call, final Response<Link> response) {

                if(!response.message().equals("conflict")) {
                    progressFragmentDialog.show(getSupportFragmentManager(), "progress");
                    UploaderWrapper uploaderWrapper = new UploaderWrapper(response.body(), file, credentials);
                    new AsyncUpload().execute(uploaderWrapper);
                }else{
                    Log.i(TAG, "onResponse: " + response);
                    Toast.makeText(getBaseContext(), "Фото с таким именем уже существует", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<Link> call, Throwable t) {

            }
        });
    }

    public String getPath(Uri uri) {

        String path = null;
        String[] projection = { MediaStore.Files.FileColumns.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        if(cursor == null){
            path = uri.getPath();
        }
        else{
            cursor.moveToFirst();
            int column_index = cursor.getColumnIndexOrThrow(projection[0]);
            path = cursor.getString(column_index);
            cursor.close();
        }

        return ((path == null || path.isEmpty()) ? (uri.getPath()) : path);
    }

    private class AsyncUpload extends AsyncTask<UploaderWrapper, Integer, Void>{

        @Override
        protected Void doInBackground(UploaderWrapper... uploaderWrappers) {
            UploaderWrapper uploaderWrapper = uploaderWrappers[0];

            Credentials credentials = uploaderWrapper.getCredentials();
            RestClient client = RestClientUtil.getInstance(credentials);

            File file = uploaderWrapper.getFile();
            Link link = uploaderWrapper.getLink();

            try {

                client.uploadFile(link, true, file, null);
            } catch (IOException e) {
                Log.i(TAG, "doInBackground: " + e.getMessage());
                e.printStackTrace();
            } catch (ServerException e) {
                Log.i(TAG, "doInBackground: " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(getApplicationContext(), "Фото успешно загружено", Toast.LENGTH_SHORT).show();
            progressFragmentDialog.dismiss();
        }
    }



}
