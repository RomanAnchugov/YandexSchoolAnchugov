package ru.romananchugov.yandexschoolanchugov.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xgc1986.parallaxPagerTransformer.ParallaxPagerTransformer;

import java.util.List;

import ru.romananchugov.yandexschoolanchugov.R;
import ru.romananchugov.yandexschoolanchugov.activities.MainActivity;
import ru.romananchugov.yandexschoolanchugov.adapters.SliderAdapter;
import ru.romananchugov.yandexschoolanchugov.models.GalleryItem;

import static ru.romananchugov.yandexschoolanchugov.utils.Constants.SLIDER_IMAGES_POSITION_KEY;

/**
 * Created by romananchugov on 11.04.2018.
 *
 * Фрагмент слайдера фотографий
 */

@SuppressLint("ValidFragment")
public class SliderDialogFragment extends DialogFragment implements View.OnClickListener{

    private static final String TAG = SliderDialogFragment.class.getSimpleName();

    private MainActivity activity;
    private List<GalleryItem> galleryItems;
    private int position;

    private RelativeLayout infoContainer;
    private TextView imagesCountTextView;
    private TextView imageTitleTextView;
    private TextView imageDateTextView;
    private ImageButton backButton;
    private ImageButton shareButton;

    private ViewPager viewPager;

    private SliderDialogFragment(List<GalleryItem> galleryItems, MainActivity activity){
        position = 0;
        this.galleryItems = galleryItems;
        this.activity = activity;
    }
    public static SliderDialogFragment newInstance(List<GalleryItem> galleryItems, MainActivity activity){
        return new SliderDialogFragment(galleryItems, activity);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.slider_fragment, container, false);

        position = getArguments().getInt(SLIDER_IMAGES_POSITION_KEY);
        imagesCountTextView = v.findViewById(R.id.images_count_text_view);
        imageTitleTextView = v.findViewById(R.id.image_title_text_view);
        imageDateTextView = v.findViewById(R.id.image_date_text_view);
        infoContainer = v.findViewById(R.id.slider_info_container);
        backButton = v.findViewById(R.id.slider_back_button);
        backButton.setOnClickListener(this);
        shareButton = v.findViewById(R.id.slider_share_button);
        shareButton.setOnClickListener(this);

        viewPager = v.findViewById(R.id.slider_view_pager);
        //использую стронний трансформер для параллакс эффекта
        ParallaxPagerTransformer transformer = new ParallaxPagerTransformer(R.id.slider_item_image_view);
        transformer.setSpeed(.6f);
        viewPager.setPageTransformer(false, transformer);

        viewPager.setAdapter(new SliderAdapter(activity, galleryItems, infoContainer));
        viewPager.addOnPageChangeListener(viewPagerChangeListener);

        setCurrentImage(position);

        return v;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.slider_back_button:
                this.dismiss();
                break;
            case R.id.slider_share_button:
                if(galleryItems.get(position).getDownloadLink() != null) {
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_TEXT,
                            galleryItems.get(position).getDownloadLink());
                    shareIntent.setType("text/plain");
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.share_images_to)));
                }else{
                    Toast.makeText(getActivity(), R.string.wait_for_load, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    public void setCurrentImage(int position){
        viewPager.setCurrentItem(position, true);
        displayImageInfo(this.position);
    }

    //обновление информации о фотографии
    public void displayImageInfo(int position){
        imagesCountTextView.setText(getResources().getString(R.string.slider_position, (position + 1), galleryItems.size()));

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
