package ru.romananchugov.yandexschoolanchugov.service;

import com.yandex.disk.rest.json.Resource;

/**
 * Created by romananchugov on 07.04.2018.
 */

public class GalleryItem {
    private String name, path, etag, contentType, publicUrl, mediaType;
    private boolean dir;
    private long contentLength, lastUpdated;

    public GalleryItem(Resource resource) {
        this.name = resource.getName();
        this.path = resource.getPath() != null ? resource.getPath().getPath() : null;  // Must throw an exception in real life code
        this.etag = resource.getMd5();
        this.contentType = resource.getMimeType();
        this.publicUrl = resource.getPublicUrl();
        this.mediaType = resource.getMediaType();
        this.dir = resource.isDir();
        this.contentLength = resource.getSize();
        this.lastUpdated = resource.getModified() != null ? resource.getModified().getTime() : 0;
    }

    @Override
    public String toString() {
        return "GalleryItem{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", etag='" + etag + '\'' +
                ", contentType='" + contentType + '\'' +
                ", publicUrl='" + publicUrl + '\'' +
                ", mediaType='" + mediaType + '\'' +
                ", dir=" + dir +
                ", contentLength=" + contentLength +
                ", lastUpdated=" + lastUpdated +
                '}';
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getEtag() {
        return etag;
    }

    public String getContentType() {
        return contentType;
    }

    public String getPublicUrl() {
        return publicUrl;
    }

    public String getMediaType() {
        return mediaType;
    }

    public boolean isDir() {
        return dir;
    }

    public long getContentLength() {
        return contentLength;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }
}
