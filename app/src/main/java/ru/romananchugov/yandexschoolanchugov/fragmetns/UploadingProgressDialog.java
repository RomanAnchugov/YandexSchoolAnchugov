package ru.romananchugov.yandexschoolanchugov.fragmetns;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.romananchugov.yandexschoolanchugov.R;

/**
 * Created by romananchugov on 14.04.2018.
 */

public class UploadingProgressDialog extends DialogFragment {
    @SuppressLint("ValidFragment")
    private UploadingProgressDialog(){}

    public static UploadingProgressDialog newInstance(){
        return new UploadingProgressDialog();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.uploading_progress_fragment, container, false);
        getDialog().setTitle(R.string.wait_upload);
        setCancelable(false);
        return v;
    }


}
