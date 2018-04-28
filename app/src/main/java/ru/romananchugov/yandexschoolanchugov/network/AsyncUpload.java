package ru.romananchugov.yandexschoolanchugov.network;

/**
 * Created by romananchugov on 28.04.2018.
 */

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.widget.Toast;

import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.exceptions.ServerException;
import com.yandex.disk.rest.json.Link;

import java.io.File;
import java.io.IOException;

import ru.romananchugov.yandexschoolanchugov.R;
import ru.romananchugov.yandexschoolanchugov.activities.MainActivity;
import ru.romananchugov.yandexschoolanchugov.fragmetns.ProgressDialog;
import ru.romananchugov.yandexschoolanchugov.models.UploaderWrapper;

import static ru.romananchugov.yandexschoolanchugov.utils.Constants.PROGRESS_DIALOG_TAG;

public class AsyncUpload extends AsyncTask<UploaderWrapper, Integer, Boolean> {

    private ProgressDialog progressDialog;

    @SuppressLint("StaticFieldLeak")
    private MainActivity activity;

    private AsyncUpload(MainActivity activity){
        this.activity = activity;
    }

    public static AsyncUpload newInstance(MainActivity activity){
        return new AsyncUpload(activity);
    }

    @Override
    protected void onPreExecute() {
        progressDialog = ProgressDialog.newInstance();
        progressDialog.show(activity.getSupportFragmentManager(), PROGRESS_DIALOG_TAG);
    }

    @Override
    protected Boolean doInBackground(UploaderWrapper... uploaderWrappers) {
        UploaderWrapper uploaderWrapper = uploaderWrappers[0];

        com.yandex.disk.rest.Credentials credentials = uploaderWrapper.getCredentials();
        RestClient client = RestClientUtil.getInstance(credentials);

        File file = uploaderWrapper.getFile();
        Link link = uploaderWrapper.getLink();

        try {
            client.uploadFile(link, true, file, null);
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        } catch (ServerException e) {
            e.printStackTrace();
            return true;
        } catch (RuntimeException e){
            e.printStackTrace();
            return true;
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean isError) {
        if(!isError) {
            Toast.makeText(activity.getApplicationContext(), R.string.successful_uploading, Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(activity.getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
        }
        activity = null;
        progressDialog.dismiss();
    }
}
