package ru.romananchugov.yandexschoolanchugov.network;

import com.yandex.disk.rest.json.DiskInfo;
import com.yandex.disk.rest.json.Link;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import ru.romananchugov.yandexschoolanchugov.models.DownloadLink;
import ru.romananchugov.yandexschoolanchugov.models.TrashResponse;

/**
 * Created by romananchugov on 09.04.2018.
 */

public interface DiskClientApi {

    @GET("v1/disk/resources/download")
    Call<DownloadLink> getDownloadFileLink(@Header("Authorization") String token, @Query("path") String pathToFile);

    @GET("v1/disk/resources/upload")
    Call<Link> getUploadLink(@Header("Authorization") String token, @Query("path") String path);

    @DELETE("v1/disk/resources")
    Call<Link> deletePhoto(@Header("Authorization") String token, @Query("path")String path, @Query("permanently") String permanently);

    @GET("v1/disk/")
    Call<DiskInfo> getStorageInfo(@Header("Authorization") String token);

    @DELETE("v1/disk/trash/resources")
    Call<Link> clearTrash(@Header("Authorization") String token);

    @GET("v1/disk/trash/resources")
    Call<TrashResponse> getTrashResources(@Header("Authorization") String token, @Query("path") String path);

    @PUT("v1/disk/trash/resources/restore")
    Call<Link> restorePhoto(@Header("Authorization") String token, @Query("path") String path);
}
