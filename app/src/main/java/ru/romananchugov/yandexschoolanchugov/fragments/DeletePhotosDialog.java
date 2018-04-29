package ru.romananchugov.yandexschoolanchugov.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import com.yandex.disk.rest.json.Link;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import ru.romananchugov.yandexschoolanchugov.R;
import ru.romananchugov.yandexschoolanchugov.activities.MainActivity;
import ru.romananchugov.yandexschoolanchugov.models.GalleryItem;
import ru.romananchugov.yandexschoolanchugov.network.DiskClientApi;

import static ru.romananchugov.yandexschoolanchugov.utils.Constants.GALLERY_FRAGMENT_TAG;
import static ru.romananchugov.yandexschoolanchugov.utils.Constants.PROGRESS_DIALOG_TAG;

/**
 * Created by romananchugov on 21.04.2018.
 */

@SuppressLint("ValidFragment")
public class DeletePhotosDialog extends DialogFragment {
    private static final String TAG = DeletePhotosDialog.class.getSimpleName();

    private MainActivity activity;
    private List<GalleryItem> selectedItems;
    private ProgressDialog progressDialog;

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
        builder.setIcon(R.drawable.ic_trash_delete_blue);
        builder.setMessage(R.string.delete_dialog_message);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteSelectedPhotos();
                progressDialog = ProgressDialog.newInstance();
                progressDialog.show(activity.getSupportFragmentManager(), PROGRESS_DIALOG_TAG);
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
        for(final GalleryItem item: selectedItems) {
            final Retrofit retrofit = activity.getRetrofit();
            DiskClientApi diskClientApi = retrofit.create(DiskClientApi.class);
            final Call<Link> call = diskClientApi
                    .deletePhoto("OAuth " + activity.getToken(), item.getPath(), "false");

            call.enqueue(new Callback<Link>() {
                @Override
                public void onResponse(Call<Link> call, Response<Link> response) {
                    selectedItems.remove(item);

                    GalleryListFragment galleryListFragment =
                            (GalleryListFragment) activity.getSupportFragmentManager().findFragmentByTag(GALLERY_FRAGMENT_TAG);
                    galleryListFragment.removeItem(item);

                    if(selectedItems.size() == 0){
                        Toast.makeText(activity, R.string.moved_to_trash, Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                        activity.cancelSelectionMode();
                    }
                    call.cancel();
                }

                @Override
                public void onFailure(Call<Link> call, Throwable t) {
                    Toast.makeText(activity, R.string.uploading_failure, Toast.LENGTH_LONG).show();
                    call.cancel();
                    progressDialog.dismiss();
                }
            });
        }
    }
}
