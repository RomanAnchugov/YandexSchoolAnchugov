package ru.romananchugov.yandexschoolanchugov.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.romananchugov.yandexschoolanchugov.R;

/**
 * Created by romananchugov on 28.04.2018.
 *
 * Фрагмент - о приложении
 */

@SuppressLint("ValidFragment")
public class AboutAppFragment extends Fragment {

    private TextView copyrightTextView;

    private AboutAppFragment(){}
    public static AboutAppFragment newInstance(){
        return new AboutAppFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.about_app_fragment, container, false);
        copyrightTextView = v.findViewById(R.id.copyright_text_view);
        setCopyrightLink();
        return v;
    }

    //делаем ссылку на апи кликабельной
    private void setCopyrightLink() {
        copyrightTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
