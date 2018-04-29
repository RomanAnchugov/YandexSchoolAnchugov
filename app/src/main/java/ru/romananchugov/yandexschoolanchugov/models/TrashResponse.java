package ru.romananchugov.yandexschoolanchugov.models;

import com.google.gson.annotations.SerializedName;
import com.yandex.disk.rest.json.ResourceList;

/**
 * Created by romananchugov on 29.04.2018.
 */

//получает список файлов в корзине
public class TrashResponse {

    @SerializedName("_embedded")
    ResourceList resourceList;

    public ResourceList getResourceList() {
        return resourceList;
    }
}
