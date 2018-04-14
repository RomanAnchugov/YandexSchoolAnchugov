package ru.romananchugov.yandexschoolanchugov.interfaces;

import com.yandex.disk.rest.json.Link;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import ru.romananchugov.yandexschoolanchugov.models.DownloadLink;

/**
 * Created by romananchugov on 09.04.2018.
 */

public interface DiskClientApi {

    @GET("resources/download")
    Call<DownloadLink> getDownloadFileLink(@Header("Authorization") String token, @Query("path") String pathToFile);

    @GET("resources/upload")
    Call<Link> getUploadLink(@Header("Authorization") String token, @Query("path") String path);
}
