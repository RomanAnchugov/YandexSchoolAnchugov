package ru.romananchugov.yandexschoolanchugov.fragmetns;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import ru.romananchugov.yandexschoolanchugov.R;
import ru.romananchugov.yandexschoolanchugov.activities.MainActivity;

import static ru.romananchugov.yandexschoolanchugov.utils.Constants.PICK_IMAGE;

/**
 * Created by romananchugov on 13.04.2018.
 */

@SuppressLint("ValidFragment")
public class AddNewPhotoFragment extends Fragment {
    private static final String TAG = AddNewPhotoFragment.class.getSimpleName();

    private MainActivity activity;
    private String title;
    private LinearLayout linearLayout;


    private AddNewPhotoFragment(MainActivity activity, String title){
        this.title = title;
        this.activity = activity;
    }

    @NonNull
    public static AddNewPhotoFragment newInstance(MainActivity activity, String title){
        return new AddNewPhotoFragment(activity, title);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.add_new_photo_fragment, container, false);
        activity.getSupportActionBar().setTitle(title);
        linearLayout = v.findViewById(R.id.upload_image_ll);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseFile();
            }
        });

        return v;
    }

    public void chooseFile(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);

//        Intent intent = new Intent();
//        intent.setAction(Intent.ACTION_PICK);
//
//        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//        String pictureDirectoryPath = pictureDirectory.getPath();
//        Uri data = Uri.parse(pictureDirectoryPath);
//        intent.setDataAndType(data, "image/*");
//
//        activity.startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }



}
