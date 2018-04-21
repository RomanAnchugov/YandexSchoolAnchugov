package ru.romananchugov.yandexschoolanchugov.fragmetns;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.yandex.disk.rest.json.Link;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.romananchugov.yandexschoolanchugov.R;
import ru.romananchugov.yandexschoolanchugov.activities.MainActivity;
import ru.romananchugov.yandexschoolanchugov.interfaces.DiskClientApi;
import ru.romananchugov.yandexschoolanchugov.models.GalleryItem;

import static ru.romananchugov.yandexschoolanchugov.activities.MainActivity.GALLERY_FRAGMENT_TAG;
import static ru.romananchugov.yandexschoolanchugov.activities.MainActivity.TOKEN;
import static ru.romananchugov.yandexschoolanchugov.utils.Constants.BASE_URL;

/**
 * Created by romananchugov on 21.04.2018.
 */

@SuppressLint("ValidFragment")
public class DeletePhotosDialog extends DialogFragment {
    private static final String TAG = DeletePhotosDialog.class.getSimpleName();

    private MainActivity activity;
    private List<GalleryItem> selectedItems;
    private UploadingProgressDialog uploadingProgressDialog;

    private DeletePhotosDialog(List<GalleryItem> selectedItems, MainActivity activity){
        this.selectedItems = selectedItems;
        this.activity = activity;
    }
    public static DeletePhotosDialog newInstance(List<GalleryItem> selectedItems, MainActivity activity){
        return new DeletePhotosDialog(selectedItems, activity);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.delete_dialog_title);
        builder.setMessage(R.string.delete_dialog_message);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteSelectedPhotos();
                uploadingProgressDialog = UploadingProgressDialog.newInstance();
                uploadingProgressDialog.show(getFragmentManager(), "deleting progress");
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        return builder.create();
    }

    public void deleteSelectedPhotos(){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String token = preferences.getString(TOKEN, null);
        activity.cancelSelectionMode();

        for(final GalleryItem item: selectedItems) {
            Retrofit.Builder builder = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create());
            final Retrofit retrofit = builder.build();
            DiskClientApi diskClientApi = retrofit.create(DiskClientApi.class);
            final Call<Link> call = diskClientApi.deletePhoto("OAuth " + token, item.getPath(), "false");

            call.enqueue(new Callback<Link>() {
                @Override
                public void onResponse(Call<Link> call, Response<Link> response) {
                    Log.i(TAG, "onResponse: " + selectedItems.size());
                    selectedItems.remove(item);

                    GalleryListFragment galleryListFragment =
                            (GalleryListFragment) activity.getSupportFragmentManager().findFragmentByTag(GALLERY_FRAGMENT_TAG);
                    galleryListFragment.removeItem(item);

                    if(selectedItems.size() == 0){
                        uploadingProgressDialog.dismiss();
                    }
                    Log.i(TAG, "onResponse: " + selectedItems.size());
                }

                @Override
                public void onFailure(Call<Link> call, Throwable t) {

                }
            });
        }
    }
}
