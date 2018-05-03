package ru.romananchugov.yandexschoolanchugov.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.yandex.disk.rest.json.Resource;

/**
 * Created by romananchugov on 07.04.2018.
 */

public class GalleryItem implements Parcelable {
    public static final Creator<GalleryItem> CREATOR = new Creator<GalleryItem>() {
        @Override
        public GalleryItem createFromParcel(Parcel in) {
            return new GalleryItem(in);
        }

        @Override
        public GalleryItem[] newArray(int size) {
            return new GalleryItem[size];
        }
    };
    private String name, path, etag, contentType, publicUrl, mediaType;
    private boolean dir;
    private long contentLength, lastUpdated;
    private String downloadLink, preview, mime, date;

    public GalleryItem(Resource resource) {
        this.name = resource.getName();
        this.path = resource.getPath() != null ? resource.getPath().getPath() : null;  // Must throw an exception in real life code
        this.etag = resource.getMd5();
        this.contentType = resource.getMimeType();
        this.publicUrl = resource.getPublicUrl();
        this.mediaType = resource.getMediaType();
        this.dir = resource.isDir();
        this.preview = resource.getPreview();
        this.contentLength = resource.getSize();
        this.mime = resource.getMimeType();
        this.lastUpdated = resource.getModified() != null ? resource.getModified().getTime() : 0;
        this.date = resource.getCreated().toString();
    }

    private GalleryItem(Parcel in) {
        name = in.readString();
        path = in.readString();
        etag = in.readString();
        contentType = in.readString();
        publicUrl = in.readString();
        mediaType = in.readString();
        dir = in.readByte() != 0;
        contentLength = in.readLong();
        lastUpdated = in.readLong();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(path);
        parcel.writeString(name);
        parcel.writeLong(contentLength);
        parcel.writeLong(lastUpdated);
        parcel.writeByte((byte) (dir ? 1 : 0));
        parcel.writeString(etag);
        parcel.writeString(contentType);
        parcel.writeString(publicUrl);
        parcel.writeString(mediaType);
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

    public String getPreview() {
        return preview;
    }

    public String getMime() {
        return mime;
    }

    public String getDate() {
        return date;
    }
}
