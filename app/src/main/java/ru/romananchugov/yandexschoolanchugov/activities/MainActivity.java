package ru.romananchugov.yandexschoolanchugov.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.exceptions.ServerException;
import com.yandex.disk.rest.json.Link;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.romananchugov.yandexschoolanchugov.R;
import ru.romananchugov.yandexschoolanchugov.fragmetns.DeletePhotosDialog;
import ru.romananchugov.yandexschoolanchugov.fragmetns.GalleryListFragment;
import ru.romananchugov.yandexschoolanchugov.fragmetns.LogoutAcceptDialog;
import ru.romananchugov.yandexschoolanchugov.fragmetns.UploadingProgressDialog;
import ru.romananchugov.yandexschoolanchugov.interfaces.DiskClientApi;
import ru.romananchugov.yandexschoolanchugov.models.GalleryItem;
import ru.romananchugov.yandexschoolanchugov.models.UploaderWrapper;
import ru.romananchugov.yandexschoolanchugov.network.RestClientUtil;

import static ru.romananchugov.yandexschoolanchugov.utils.Constants.BASE_URL;
import static ru.romananchugov.yandexschoolanchugov.utils.Constants.PICK_IMAGE;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    public static final String GALLERY_FRAGMENT_TAG = "Gallery";
    public static final String CLIENT_ID = "959666c7ee9942f6b9ffec283205e35c";
    public static final String AUTH_URL = "https://oauth.yandex.ru/authorize?response_type=token&client_id=" + CLIENT_ID;
    public static final String USERNAME = "ymra.username";
    public static final String TOKEN = "ymra.token";
    private static final String TAG = "MainActivity";

    private UploadingProgressDialog progressFragmentDialog;

    private Toolbar toolbar;

    private List<ImageView> selectedViews;
    private List<GalleryItem> selectedItems;
    private boolean isSelectionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        selectedViews = new ArrayList<>();
        selectedItems = new ArrayList<>();
        isSelectionMode = false;

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                closeKeyboard();
                cancelSelectionMode();
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.open_nav_drawer, R.string.close_nav_drawer);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        progressFragmentDialog = UploadingProgressDialog.newInstance();

        if (getIntent() != null && getIntent().getData() != null) {
            onLogin();
        }

        navigationView.getMenu().getItem(0).setChecked(true);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String token = preferences.getString(TOKEN, null);
        if (token == null) {
            startLogin();
            return;
        }

        if (savedInstanceState == null) {
            startFragment();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.delete_forever:
                DeletePhotosDialog.newInstance(selectedItems, this).show(getSupportFragmentManager(), "delete dialog");
                break;
        }
        return false;
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        boolean isGalleryVisible = getSupportFragmentManager().findFragmentByTag(GALLERY_FRAGMENT_TAG).isVisible();

        switch (item.getItemId()){
            case R.id.gallery_menu_item:
                if(!isGalleryVisible) {
                    addFragment(GalleryListFragment.newInstance("Я.Галерея", this), GALLERY_FRAGMENT_TAG);
                    item.setChecked(true);
                }
                break;
            case R.id.about_menu_item:
                item.setChecked(true);
                break;
            case R.id.logout_menu_item:
                logout();
                break;
        }

        closeNavDrawer();
        closeKeyboard();
        return true;
    }

    @Override
    public void onBackPressed() {
        if(isSelectionMode){
            cancelSelectionMode();
        }else{
            super.onBackPressed();
        }
    }

    public boolean isSelectionMode() {
        return isSelectionMode;
    }

    public void setSelectionMode(boolean selectionMode) {
        isSelectionMode = selectionMode;
        if(isSelectionMode){
            toolbar.getMenu().clear();
            toolbar.inflateMenu(R.menu.delete_photo);
        }
    }

    public List<ImageView> getSelectedViews() {
        return selectedViews;
    }

    private void startFragment() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, GalleryListFragment.newInstance("Я.Галерея", this), GALLERY_FRAGMENT_TAG)
                .commit();
    }

    public void startLogin() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(AUTH_URL)));
    }

    public void logout(){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString(USERNAME, "");
        editor.putString(TOKEN, null);
        editor.apply();
        LogoutAcceptDialog.newInstance().show(getSupportFragmentManager(), "dialog");
    }

    public void onLogin() {
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
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString(USERNAME, "");
        editor.putString(TOKEN, token);
        editor.apply();
    }

    //ссылка для загрузки файла на диск
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

    //получаем путь выбранной фотографии
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

    public void addFragment(Fragment fragment, String tag){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft = ft.replace(R.id.fragment_container, fragment, tag);
        ft.commit();
    }

    public void closeNavDrawer(){
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    public void closeKeyboard(){
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.fragment_container);
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(frameLayout.getWindowToken(), 0);
    }

    //добавляет новый элемент в выбранные
    public void addViewInSelected(ImageView imageView, GalleryItem galleryItem){
        selectedViews.add(imageView);
        selectedItems.add(galleryItem);
        updateToolbar();
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_preload_placeholder));
        Log.i(TAG, "addViewInSelected: " + selectedViews.size());
    }
    //удаляет элемент из выбранных
    public void removeViewFromSelected(ImageView imageView, GalleryItem galleryItem){
        selectedViews.remove(imageView);
        selectedItems.remove(galleryItem);
        updateToolbar();
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_image));
        Log.i(TAG, "removeViewFromSelected: " + selectedViews.size());
    }

    public void updateToolbar(){
        toolbar.setTitle(
                getResources()
                        .getString(R.string.selection_placeholder,
                                String.valueOf(getSelectedViews().size())));
    }

    public void cancelSelectionMode(){
        selectedViews.clear();
        toolbar.getMenu().clear();
        toolbar.setTitle(getResources().getString(R.string.app_name));
        isSelectionMode = false;
    }

    //асинхронная загрузка файла на диск
    @SuppressLint("StaticFieldLeak")
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
