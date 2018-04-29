package ru.romananchugov.yandexschoolanchugov.utils;

import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.github.lzyzsd.circleprogress.ArcProgress;

/**
 * Created by romananchugov on 29.04.2018.
 */

//анимация для прогресса в StorageInfoFragment
public class ArcProgressAnimation extends Animation {
    private ArcProgress arcProgress;
    private double  to;

    public ArcProgressAnimation(ArcProgress arcProgress, double to) {
        super();
        this.arcProgress = arcProgress;
        this.to = to;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        double value =  to * interpolatedTime;
        arcProgress.setProgress((int) value);
    }
}
