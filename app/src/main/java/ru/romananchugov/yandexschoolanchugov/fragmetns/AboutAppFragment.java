package ru.romananchugov.yandexschoolanchugov.fragmetns;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.romananchugov.yandexschoolanchugov.R;

/**
 * Created by romananchugov on 28.04.2018.
 */

@SuppressLint("ValidFragment")
public class AboutAppFragment extends Fragment {
    private AboutAppFragment(){}

    public static AboutAppFragment newInstance(){
        return new AboutAppFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.about_app_fragment, container, false);
        return v;
    }
}
