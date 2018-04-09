/*
* (C) 2015 Yandex LLC (https://yandex.com/)
*
* The source code of Java SDK for Yandex.Disk REST API
* is available to use under terms of Apache License,
* Version 2.0. See the file LICENSE for the details.
*/

package ru.romananchugov.yandexschoolanchugov;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.yandex.disk.rest.ProgressListener;
import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.exceptions.ServerException;
import com.yandex.disk.rest.exceptions.http.HttpCodeException;

import java.io.File;
import java.io.IOException;

import ru.romananchugov.yandexschoolanchugov.service.Credentials;
import ru.romananchugov.yandexschoolanchugov.service.GalleryItem;
import ru.romananchugov.yandexschoolanchugov.service.RestClientUtil;

public class DownloadFileFragment extends IODialogFragment {

    private static final String TAG = "LoadFileFragment";

    private static final String WORK_FRAGMENT_TAG = "LoadFileFragment.Background";

    private static final String FILE_ITEM = "example.file.item";

    private static final int PROGRESS_DIV = 1024 * 1024;

    private Credentials credentials;
    private GalleryItem item;

    private DownloadFileRetainedFragment workFragment;

    public static DownloadFileFragment newInstance(Credentials credentials, GalleryItem item) {
        DownloadFileFragment fragment = new DownloadFileFragment();

        Bundle args = new Bundle();
        args.putParcelable(CREDENTIALS, credentials);
        args.putParcelable(FILE_ITEM, item);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        credentials = getArguments().getParcelable(CREDENTIALS);
        item = getArguments().getParcelable(FILE_ITEM);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FragmentManager fragmentManager = getFragmentManager();
        workFragment = (DownloadFileRetainedFragment) fragmentManager.findFragmentByTag(WORK_FRAGMENT_TAG);
        if (workFragment == null || workFragment.getTargetFragment() == null) {
            workFragment = new DownloadFileRetainedFragment();
            fragmentManager.beginTransaction().add(workFragment, WORK_FRAGMENT_TAG).commit();
            workFragment.loadFile(getActivity(), credentials, item);
        }
        workFragment.setTargetFragment(this, 0);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (workFragment != null) {
            workFragment.setTargetFragment(null, 0);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = new ProgressDialog(getActivity());
        dialog.setTitle(R.string.app_name);
        dialog.setMessage(item.getName());
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setIndeterminate(true);
        dialog.setButton(ProgressDialog.BUTTON_NEUTRAL, getString(R.string.app_name), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                onCancel();
            }
        });
        dialog.setOnCancelListener(this);
        dialog.show();
        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        onCancel();
    }

    private void onCancel() {
        workFragment.cancelDownload();
    }

    public void onDownloadComplete(File file) {
        dialog.dismiss();
        makeWorldReadableAndOpenFile(file);
    }

    private void makeWorldReadableAndOpenFile(File file) {
        file.setReadable(true, false);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), item.getContentType());
        startActivity(Intent.createChooser(intent, getText(R.string.app_name)));
    }

    public void setDownloadProgress(long loaded, long total) {
        if (dialog != null) {
            if (dialog.isIndeterminate()) {
                dialog.setIndeterminate(false);
            }
            if (total > Integer.MAX_VALUE) {
                dialog.setProgress((int)(loaded / PROGRESS_DIV));
                dialog.setMax((int)(total / PROGRESS_DIV));
            } else {
                dialog.setProgress((int)loaded);
                dialog.setMax((int)total);
            }
        }
    }

    public static class DownloadFileRetainedFragment extends IODialogRetainedFragment implements ProgressListener {



        private boolean cancelled;
        private File result;

        public void loadFile(final Context context, final Credentials credentials, final GalleryItem item) {
            result = new File(context.getFilesDir(), new File(item.getPath()).getName());

            new Thread(new Runnable() {
                @Override
                public void run () {
                    try {
                        RestClient client = RestClientUtil.getInstance(credentials);
                        client.downloadFile(item.getPath(), result, DownloadFileRetainedFragment.this);
                        downloadComplete();
                    } catch (HttpCodeException ex) {
                        Log.d(TAG, "loadFile", ex);
                    } catch (IOException | ServerException ex) {
                        Log.d(TAG, "loadFile", ex);
                        sendException(ex);
                    }
                }
            }).start();
        }

        @Override
        public void updateProgress (final long loaded, final long total) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    DownloadFileFragment targetFragment = (DownloadFileFragment) getTargetFragment();
                    if (targetFragment != null) {
                        targetFragment.setDownloadProgress(loaded, total);
                    }
                }
            });
        }

        @Override
        public boolean hasCancelled () {
            return cancelled;
        }

        public void downloadComplete() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    DownloadFileFragment targetFragment = (DownloadFileFragment) getTargetFragment();
                    if (targetFragment != null) {
                        targetFragment.onDownloadComplete(result);
                    }
                }
            });
        }

        public void cancelDownload() {
            cancelled = true;
        }
    }
}
