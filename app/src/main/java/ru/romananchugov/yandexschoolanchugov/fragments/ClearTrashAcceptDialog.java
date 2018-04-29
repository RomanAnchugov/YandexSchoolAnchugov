package ru.romananchugov.yandexschoolanchugov.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import com.yandex.disk.rest.json.Link;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.romananchugov.yandexschoolanchugov.R;
import ru.romananchugov.yandexschoolanchugov.activities.MainActivity;
import ru.romananchugov.yandexschoolanchugov.network.DiskClientApi;

import static ru.romananchugov.yandexschoolanchugov.activities.MainActivity.TOKEN;
import static ru.romananchugov.yandexschoolanchugov.utils.Constants.BASE_URL;

/**
 * Created by romananchugov on 29.04.2018.
 */

//диалог для принятия очистки корзины
@SuppressLint("ValidFragment")
public class ClearTrashAcceptDialog extends DialogFragment {

    private MainActivity activity;

    private ClearTrashAcceptDialog(MainActivity activity){
        this.activity = activity;
    }

    public static ClearTrashAcceptDialog newInstance(MainActivity activity){
        return new ClearTrashAcceptDialog(activity);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Внимание");
        builder.setIcon(R.drawable.ic_forever_delete);
        builder.setMessage("Все файлы будут безвозвратно удалены");
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                clearTrash();
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        return builder.create();
    }

    //очищает корзину при положительном ответе в диалоге
    private void clearTrash(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String token = preferences.getString(TOKEN, null);


        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create());

        final Retrofit retrofit = builder.build();

        DiskClientApi clientApi = retrofit.create(DiskClientApi.class);
        final Call<Link> call = clientApi.clearTrash("OAuth " + token);

        call.enqueue(new Callback<Link>() {
            @Override
            public void onResponse(Call<Link> call, Response<Link> response) {
                Toast.makeText(activity, R.string.successful_trash_clear, Toast.LENGTH_SHORT).show();
                call.cancel();
            }

            @Override
            public void onFailure(Call<Link> call, Throwable t) {
                Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
