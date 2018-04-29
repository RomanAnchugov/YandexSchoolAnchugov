package ru.romananchugov.yandexschoolanchugov.fragments;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.yandex.disk.rest.json.DiskInfo;
import com.yandex.disk.rest.json.Link;
import com.yandex.disk.rest.json.Resource;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.romananchugov.yandexschoolanchugov.R;
import ru.romananchugov.yandexschoolanchugov.activities.MainActivity;
import ru.romananchugov.yandexschoolanchugov.models.TrashResponse;
import ru.romananchugov.yandexschoolanchugov.network.DiskClientApi;

import static ru.romananchugov.yandexschoolanchugov.activities.MainActivity.TOKEN;
import static ru.romananchugov.yandexschoolanchugov.utils.Constants.BASE_URL;
import static ru.romananchugov.yandexschoolanchugov.utils.Constants.CLEAR_TRASH_DIALOG_TAG;

/**
 * Created by romananchugov on 29.04.2018.
 */

@SuppressLint("ValidFragment")
public class StorageInfoFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = StorageInfoFragment.class.getSimpleName();

    private MainActivity activity;

    private ArcProgress storageInfoPg;
    private ArcProgress trashInfoPg;
    private Button clearTrashButton;
    private Button restoreTrashButton;

    private StorageInfoFragment(MainActivity activity) {
        this.activity = activity;
    }

    public static StorageInfoFragment newInstance(MainActivity activity) {
        return new StorageInfoFragment(activity);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.storage_info_fragment, container, false);

        storageInfoPg = v.findViewById(R.id.storage_info_pb);
        storageInfoPg.setMax(100);
        trashInfoPg = v.findViewById(R.id.trash_info_pb);
        trashInfoPg.setMax(100);
        clearTrashButton = v.findViewById(R.id.clear_trash_button);
        clearTrashButton.setOnClickListener(this);
        restoreTrashButton = v.findViewById(R.id.restore_trash_button);
        restoreTrashButton.setOnClickListener(this);

        loadStorageInfo();

        return v;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.clear_trash_button:
                ClearTrashAcceptDialog.newInstance(activity)
                        .show(activity.getSupportFragmentManager(), CLEAR_TRASH_DIALOG_TAG);
                break;
            case R.id.restore_trash_button:
                restoreTrash();
                break;
        }
    }

    //загружает информацию о диске
    private void loadStorageInfo() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String token = preferences.getString(TOKEN, null);


        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create());

        final Retrofit retrofit = builder.build();

        DiskClientApi clientApi = retrofit.create(DiskClientApi.class);
        final Call<DiskInfo> call = clientApi.getStorageInfo("OAuth " + token);

        call.enqueue(new Callback<DiskInfo>() {
            @Override
            public void onResponse(Call<DiskInfo> call, Response<DiskInfo> response) {
                if (response.body() != null){
                    double overallPercent = (double)response.body().getUsedSpace() / response.body().getTotalSpace();
                    double trashPercent = (double)response.body().getTrashSize() / response.body().getTotalSpace();

                    storageInfoPg.setProgress((int)(overallPercent * 100));
                    trashInfoPg.setProgress((int)(trashPercent * 100));
                    call.cancel();
                }
            }

            @Override
            public void onFailure(Call<DiskInfo> call, Throwable t) {
                Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
                call.cancel();
            }
        });
    }

    //загружает список ресурсов в корзине и вызывает восстановление для фотографий
    private void restoreTrash(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String token = preferences.getString(TOKEN, null);

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create());

        final Retrofit retrofit = builder.build();

        DiskClientApi clientApi = retrofit.create(DiskClientApi.class);
        final Call<TrashResponse> call = clientApi.getTrashResources("OAuth " + token, "/");

        call.enqueue(new Callback<TrashResponse>() {
            @Override
            public void onResponse(Call<TrashResponse> call, Response<TrashResponse> response) {
                boolean restoringFlag = false;
                if(response.body().getResourceList() != null) {
                    for (Resource resource : response.body().getResourceList().getItems()) {
                        if(resource.getMimeType().equals("image/jpeg")
                                || resource.getMimeType().equals("image/png")
                                || resource.getMimeType().equals("image/bmp")){

                            restore(resource.getPath().getPath());
                            restoringFlag = true;

                        }
                    }
                }

                if(!restoringFlag){
                    Toast.makeText(activity, "Фотографий в корзине нет", Toast.LENGTH_SHORT).show();
                }
                call.cancel();
            }

            @Override
            public void onFailure(Call<TrashResponse> call, Throwable t) {
                Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show();
                call.cancel();
            }
        });
    }

    //восстанавливает фотографию из корзины
    public void restore(String path){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String token = preferences.getString(TOKEN, null);

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create());

        final Retrofit retrofit = builder.build();

        DiskClientApi clientApi = retrofit.create(DiskClientApi.class);
        final Call<Link> call = clientApi.restorePhoto("OAuth " + token, path);

        call.enqueue(new Callback<Link>() {
            @Override
            public void onResponse(Call<Link> call, Response<Link> response) {
                if(response.code() == 201){
                    Toast.makeText(activity, R.string.successful_restored_trash, Toast.LENGTH_SHORT).show();
                }
                Log.i(TAG, "onResponse: " + response.message());
                call.cancel();
            }

            @Override
            public void onFailure(Call<Link> call, Throwable t) {
                Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show();
                call.cancel();
            }
        });


    }
}
