package ru.romananchugov.yandexschoolanchugov.fragments;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.File;

import ru.romananchugov.yandexschoolanchugov.R;
import ru.romananchugov.yandexschoolanchugov.activities.MainActivity;

import static ru.romananchugov.yandexschoolanchugov.utils.Constants.PICK_IMAGE;

/**
 * Created by romananchugov on 13.04.2018.
 *
 * Диалог для добавления фотографии
 */

@SuppressLint("ValidFragment")
public class AddNewPhotoDialog extends DialogFragment {
    private static final String TAG = AddNewPhotoDialog.class.getSimpleName();

    private MainActivity activity;
    private String title;
    private Button button;


    private AddNewPhotoDialog(MainActivity activity, String title){
        this.title = title;
        this.activity = activity;
    }

    @NonNull
    public static AddNewPhotoDialog newInstance(MainActivity activity, String title){
        return new AddNewPhotoDialog(activity, title);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.add_new_photo_fragment, container, false);
        if(activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle(title);
        }
        getDialog().setTitle(title);
        button = v.findViewById(R.id.open_gallery_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseFile();
            }
        });

        return v;
    }

    //открываем галерею для выбора картинки
    public void chooseFile(){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);

        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = pictureDirectory.getPath();
        Uri data = Uri.parse(pictureDirectoryPath);
        intent.setDataAndType(data, "image/*");

        //результат получаем в главной активити
        activity.startActivityForResult(Intent.createChooser(intent, getString(R.string.select_photo)), PICK_IMAGE);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if(activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
        }
        super.onDismiss(dialog);

    }
}
