package ru.romananchugov.yandexschoolanchugov.fragmetns;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import ru.romananchugov.yandexschoolanchugov.R;

/**
 * Created by romananchugov on 17.04.2018.
 */

public class LogoutAcceptDialog extends DialogFragment {
    @SuppressLint("ValidFragment")
    private LogoutAcceptDialog(){}

    public static LogoutAcceptDialog newInstance(){
        return new LogoutAcceptDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.logout_dialog_title);
        builder.setMessage(R.string.logout_dialog_message);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getActivity().finish();
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
