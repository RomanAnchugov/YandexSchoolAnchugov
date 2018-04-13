package ru.romananchugov.yandexschoolanchugov.fragmetns;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.exceptions.NetworkIOException;
import com.yandex.disk.rest.exceptions.ServerIOException;
import com.yandex.disk.rest.exceptions.WrongMethodException;
import com.yandex.disk.rest.json.Link;

import ru.romananchugov.yandexschoolanchugov.R;
import ru.romananchugov.yandexschoolanchugov.network.RestClientUtil;

/**
 * Created by romananchugov on 13.04.2018.
 */

@SuppressLint("ValidFragment")
public class AddNewPhotoFragment extends Fragment {
    private static final String TAG = AddNewPhotoFragment.class.getSimpleName();

    private RestClient client;
    private Credentials credentials;


    private AddNewPhotoFragment(Credentials credentials){
        Log.i(TAG, "AddNewPhotoFragment: constructor");
        this.credentials = credentials;
        client = RestClientUtil.getInstance(credentials);
        getUploadLink();
    }

    @NonNull
    public static AddNewPhotoFragment newInstance(Credentials credentials){
        return new AddNewPhotoFragment(credentials);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.add_new_photo_fragment, container, false);
        return v;
    }

    public void getUploadLink(){
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    Link link = client.getUploadLink("", false).;
                } catch (ServerIOException e) {
                    Log.i(TAG, "getUploadLink: " + e.getMessage());
                    e.printStackTrace();
                } catch (WrongMethodException e) {
                    Log.i(TAG, "getUploadLink: " + e.getMessage());
                    e.printStackTrace();
                } catch (NetworkIOException e) {
                    Log.i(TAG, "getUploadLink: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
}
