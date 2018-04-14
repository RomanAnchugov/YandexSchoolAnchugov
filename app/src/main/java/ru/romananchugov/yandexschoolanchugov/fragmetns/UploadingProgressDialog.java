package ru.romananchugov.yandexschoolanchugov.fragmetns;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

import ru.romananchugov.yandexschoolanchugov.R;

/**
 * Created by romananchugov on 14.04.2018.
 */

public class UploadingProgressDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.uploading_progress_fragment, null));

        return builder.create();
    }
}
