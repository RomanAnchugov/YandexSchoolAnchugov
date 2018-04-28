package ru.romananchugov.yandexschoolanchugov.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
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
import com.yandex.disk.rest.json.Link;

import java.io.File;
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
import ru.romananchugov.yandexschoolanchugov.fragmetns.AboutAppFragment;
import ru.romananchugov.yandexschoolanchugov.fragmetns.DeletePhotosDialog;
import ru.romananchugov.yandexschoolanchugov.fragmetns.GalleryListFragment;
import ru.romananchugov.yandexschoolanchugov.fragmetns.LogoutAcceptDialog;
import ru.romananchugov.yandexschoolanchugov.models.GalleryItem;
import ru.romananchugov.yandexschoolanchugov.models.UploaderWrapper;
import ru.romananchugov.yandexschoolanchugov.network.AsyncUpload;
import ru.romananchugov.yandexschoolanchugov.network.DiskClientApi;

import static ru.romananchugov.yandexschoolanchugov.utils.Constants.ABOUT_FRAGMENT_TAG;
import static ru.romananchugov.yandexschoolanchugov.utils.Constants.BASE_URL;
import static ru.romananchugov.yandexschoolanchugov.utils.Constants.DELETE_DIALOG_TAG;
import static ru.romananchugov.yandexschoolanchugov.utils.Constants.GALLERY_FRAGMENT_TAG;
import static ru.romananchugov.yandexschoolanchugov.utils.Constants.LOGOUT_DIALOG_TAG;
import static ru.romananchugov.yandexschoolanchugov.utils.Constants.PICK_IMAGE;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static final String CLIENT_ID = "959666c7ee9942f6b9ffec283205e35c";
    public static final String AUTH_URL = "https://oauth.yandex.ru/authorize?response_type=token&client_id=" + CLIENT_ID;
    public static final String USERNAME = "ymra.username";
    public static final String TOKEN = "ymra.token";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int SELECTION_PADDING = 20;

    private Toolbar toolbar;
    private MainActivity activity;

    private List<ImageView> selectedViews;
    private List<GalleryItem> selectedItems;
    private boolean isSelectionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        activity = this;

        selectedViews = new ArrayList<>();
        selectedItems = new ArrayList<>();
        isSelectionMode = false;

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                closeKeyboard();
                if(isSelectionMode) {
                    cancelSelectionMode();
                }
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

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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
            addFragment(GalleryListFragment.newInstance(this), GALLERY_FRAGMENT_TAG);
        }

        Log.i(TAG, "onCreate: token - " + token);
    }

    @Override
    protected void onResume() {
        if(isSelectionMode){
            cancelSelectionMode();
        }
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.delete_to_trash:
                DeletePhotosDialog
                        .newInstance(selectedItems, this).show(getSupportFragmentManager(), DELETE_DIALOG_TAG);
                break;
            case R.id.share:
                ArrayList<Uri> sharedUrl = new ArrayList<>();
                for(GalleryItem galleryItem:selectedItems){
                    if(galleryItem.getDownloadLink() != null) {
                        sharedUrl.add(Uri.parse(galleryItem.getDownloadLink()));
                    }
                }

                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, sharedUrl);
                shareIntent.setType("image/*");
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_images_to)));
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
        GalleryListFragment galleryListFragment = (GalleryListFragment) getSupportFragmentManager().findFragmentByTag(GALLERY_FRAGMENT_TAG);

        switch (item.getItemId()){
            case R.id.gallery_menu_item:
                if(galleryListFragment == null) {
                    addFragment(GalleryListFragment.newInstance(this), GALLERY_FRAGMENT_TAG);
                    item.setChecked(true);
                }
                break;
            case R.id.about_menu_item:
                addFragment(AboutAppFragment.newInstance(), ABOUT_FRAGMENT_TAG);
                item.setChecked(true);
                toolbar.setTitle(R.string.about_app);
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
            getSupportActionBar().setTitle(R.string.app_name);
            super.onBackPressed();
        }
    }

    public boolean isSelectionMode() {
        return isSelectionMode;
    }

    public void startLogin() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(AUTH_URL)));
    }

    public void logout(){
        LogoutAcceptDialog.newInstance(this).show(getSupportFragmentManager(), LOGOUT_DIALOG_TAG);
    }

    //логин после возврата из браузера
    public void onLogin() {
        Uri data = getIntent().getData();
        setIntent(null);

        Pattern pattern = Pattern.compile("access_token=(.*?)(&|$)");
        Matcher matcher = pattern.matcher(data.toString());

        if (matcher.find()) {
            final String token = matcher.group(1);
            if (!TextUtils.isEmpty(token)) {
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

    //получаем ссылку для загрузки файла на диск
    public void getUploadLink(final Credentials credentials, final File file){
        Toast.makeText(getApplicationContext(), R.string.load_soon, Toast.LENGTH_SHORT).show();

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
                    UploaderWrapper uploaderWrapper = new UploaderWrapper(response.body(), file, credentials);
                    AsyncUpload.newInstance(activity).execute(uploaderWrapper);
                }else{
                    Log.i(TAG, "onResponse: " + response);
                    Toast.makeText(getBaseContext(), R.string.uploading_name_conflict, Toast.LENGTH_SHORT).show();
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
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft = ft.replace(R.id.fragment_container, fragment, tag);
        ft.commit();
    }

    public void closeNavDrawer(){
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    public void closeKeyboard(){
        FrameLayout frameLayout = findViewById(R.id.fragment_container);
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(frameLayout.getWindowToken(), 0);
        }
    }

    //установка значения состояния выделения
    public void setSelectionMode(boolean selectionMode) {
        isSelectionMode = selectionMode;
        if(isSelectionMode){
            toolbar.getMenu().clear();
            toolbar.inflateMenu(R.menu.selection_mode_menu);
            toolbar.setBackgroundColor(getResources().getColor(R.color.blue));
            toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        }
    }

    public List<ImageView> getSelectedViews() {
        return selectedViews;
    }

    //добавление нового элемента в выбранные
    public void addViewInSelected(ImageView imageView, GalleryItem galleryItem){
        selectedViews.add(imageView);
        selectedItems.add(galleryItem);
        updateToolbar();

        imageView.setPadding(SELECTION_PADDING, SELECTION_PADDING, SELECTION_PADDING, SELECTION_PADDING);
        imageView.setBackgroundColor(getResources().getColor(R.color.yellow));
    }

    //удаление элементов из выбранных
    public void removeViewFromSelected(ImageView imageView, GalleryItem galleryItem){
        selectedViews.remove(imageView);
        selectedItems.remove(galleryItem);
        updateToolbar();

        imageView.setPadding(1,1,1,1);
        imageView.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        if(selectedItems.size() == 0){
            cancelSelectionMode();
        }
    }

    //обновление информации на тулбаре при выделении
    public void updateToolbar(){
        toolbar.setTitle(
                getResources()
                        .getString(R.string.selection_placeholder,
                                String.valueOf(getSelectedViews().size())));
    }

    //отключение состояния выделения
    public void cancelSelectionMode(){
        for(ImageView view:selectedViews){
            view.setPadding(1,1,1,1);
            view.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }
        selectedViews.clear();
        selectedItems.clear();
        toolbar.getMenu().clear();
        toolbar.setTitle(getResources().getString(R.string.app_name));
        toolbar.setBackgroundResource(R.drawable.item_bg_card);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
        isSelectionMode = false;
    }

    //Запрос на запись файлов для api>23
    public static void verifyStoragePermissions(Activity activity) {
        //Проверяем есть ли у нас доступ
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            //Доступа нет, спрашиваем у пользователя
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }


}
