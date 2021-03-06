package ru.romananchugov.yandexschoolanchugov.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import ru.romananchugov.yandexschoolanchugov.R;
import ru.romananchugov.yandexschoolanchugov.activities.MainActivity;

import static ru.romananchugov.yandexschoolanchugov.activities.MainActivity.TOKEN;
import static ru.romananchugov.yandexschoolanchugov.activities.MainActivity.USERNAME;

/**
 * Created by romananchugov on 17.04.2018.
 *
 * Диалог для принятия выхода из приложения
 */

@SuppressLint("ValidFragment")
public class LogoutAcceptDialog extends DialogFragment {

    private Context context;

    private LogoutAcceptDialog(Context context){
        this.context = context;
    }

    public static LogoutAcceptDialog newInstance(Context context){
        return new LogoutAcceptDialog(context);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.logout_dialog_title);
        builder.setIcon(R.drawable.ic_logout);
        builder.setMessage(R.string.logout_dialog_message);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                editor.putString(USERNAME, "");
                editor.putString(TOKEN, null);
                editor.apply();
                if(getActivity() != null) {
                    MainActivity activity = (MainActivity) getActivity();
                    for(Fragment fragment: activity.getSupportFragmentManager().getFragments()){
                        if(fragment != null){
                            activity.getSupportFragmentManager()
                                    .beginTransaction()
                                    .remove(fragment)
                                    .commit();
                        }
                    }
                    activity.startLogin();
                }else{
                    Toast.makeText(context, R.string.reload_app, Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        return builder.create();
    }
}
