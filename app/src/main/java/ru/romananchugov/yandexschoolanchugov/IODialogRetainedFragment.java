/*
* (C) 2015 Yandex LLC (https://yandex.com/)
*
* The source code of Java SDK for Yandex.Disk REST API
* is available to use under terms of Apache License,
* Version 2.0. See the file LICENSE for the details.
*/

package ru.romananchugov.yandexschoolanchugov;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;

public class IODialogRetainedFragment extends Fragment {

    protected Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        handler = new Handler();
    }

    protected void sendException(final Exception ex) {
        sendException(ex.getMessage());
    }

    protected void sendException(final String message) {
        handler.post(new Runnable() {
            @Override
            public void run () {
                IODialogFragment targetFragment = (IODialogFragment) getTargetFragment();
                if (targetFragment != null) {
                    targetFragment.sendException(message);
                }
            }
        });
    }
}
