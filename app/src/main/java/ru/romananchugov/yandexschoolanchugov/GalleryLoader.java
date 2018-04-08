package ru.romananchugov.yandexschoolanchugov;

import android.content.Context;
import android.os.Handler;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.ResourcesArgs;
import com.yandex.disk.rest.ResourcesHandler;
import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.exceptions.ServerException;
import com.yandex.disk.rest.json.Resource;
import com.yandex.disk.rest.json.ResourceList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.romananchugov.yandexschoolanchugov.service.GalleryItem;
import ru.romananchugov.yandexschoolanchugov.service.RestClientUtil;

/**
 * Created by romananchugov on 07.04.2018.
 */

public class GalleryLoader extends AsyncTaskLoader<List<GalleryItem>> {
    private static final String TAG = "GalleryLoader";

    private static final int ITEMS_PER_REQUEST = 10;

    private Handler handler;
    private Credentials credentials;
    private boolean hasCancelled;

    private List<GalleryItem> galleryItemList;

    public GalleryLoader(Context context, Credentials credentials) {
        super(context);
        this.credentials = credentials;
        handler = new Handler();
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<GalleryItem> loadInBackground() {
        galleryItemList = new ArrayList<>();
        hasCancelled = false;

        int offset = 0;
        RestClient client = null;
        try {
            client = RestClientUtil.getInstance(credentials);
            int size = 0;

            do {
                Log.i(TAG, "loadInBackground: gallerItemList.size = " + size );

                ResourceList resourceList = client.getFlatResourceList(new ResourcesArgs.Builder()
                        .setLimit(ITEMS_PER_REQUEST)
                        .setMediaType("image")
                        .setSort(ResourcesArgs.Sort.created)
                        .setOffset(offset)
                        .setParsingHandler(new ResourcesHandler() {
                            @Override
                            public void handleItem(Resource item) {
                                galleryItemList.add(new GalleryItem(item));
                            }
                        })
                        .build());

                offset += ITEMS_PER_REQUEST;

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        deliverResult(new ArrayList<>(galleryItemList));
                    }
                });

                size = resourceList.getItems().size();

            } while (!hasCancelled && offset < ITEMS_PER_REQUEST * 3);

            return galleryItemList;

        } catch (IOException | ServerException ex) {
            Log.d(TAG, "loadInBackground", ex);

        }
        return galleryItemList;
    }

    @Override
    protected void onReset() {
        super.onReset();
        hasCancelled = true;
    }
}
