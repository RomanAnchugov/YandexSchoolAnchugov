package ru.romananchugov.yandexschoolanchugov.fragmetns;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ru.romananchugov.yandexschoolanchugov.R;
import ru.romananchugov.yandexschoolanchugov.adapters.SliderAdapter;
import ru.romananchugov.yandexschoolanchugov.models.GalleryItem;
import ru.romananchugov.yandexschoolanchugov.utils.Keys;

/**
 * Created by romananchugov on 11.04.2018.
 */

@SuppressLint("ValidFragment")
public class SliderDialogFragment extends DialogFragment {

    private static final String TAG = SliderDialogFragment.class.getSimpleName();

    private List<GalleryItem> galleryItems;
    private int position;

    private TextView imagesCountTextView;
    private TextView imageTitleTextView;
    private TextView imageDateTextView;

    private SliderAdapter sliderAdapter;
    private ViewPager viewPager;

    private SliderDialogFragment(){
        position = 0;
    }
    public static SliderDialogFragment newInstance(){
        SliderDialogFragment f = new SliderDialogFragment();
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.gallery_slider_fragment, container, false);

        galleryItems = (List<GalleryItem>) getArguments().getSerializable(Keys.SLIDER_IMAGES_ARRAY);
        position = getArguments().getInt(Keys.SLIDER_IMAGES_POSITION);
        imagesCountTextView = v.findViewById(R.id.images_count_text_view);
        imageTitleTextView = v.findViewById(R.id.image_title_text_view);
        imageDateTextView = v.findViewById(R.id.image_date_text_view);

        List<TextView> infoViews = new ArrayList<>();
        infoViews.add(imageDateTextView);
        infoViews.add(imageTitleTextView);
        infoViews.add(imagesCountTextView);

        viewPager = v.findViewById(R.id.slider_view_pager);
        viewPager.setAdapter(new SliderAdapter(getActivity(), galleryItems, infoViews));
        viewPager.addOnPageChangeListener(viewPagerChangeListener);

        setCurrentImage(position);

        return v;
    }



    public void setCurrentImage(int position){
        viewPager.setCurrentItem(position, false);
        displayImageInfo(this.position);
    }

    public void displayImageInfo(int position){
        //TODO: placeholder
        imagesCountTextView.setText((position + 1) + " из " +  galleryItems.size());

        GalleryItem item = galleryItems.get(position);
        imageTitleTextView.setText(item.getName());
        imageDateTextView.setText(item.getDate());
    }

    ViewPager.OnPageChangeListener  viewPagerChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            displayImageInfo(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

}
