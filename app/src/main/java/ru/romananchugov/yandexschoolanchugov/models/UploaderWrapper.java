package ru.romananchugov.yandexschoolanchugov.models;

import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.json.Link;

import java.io.File;

/**
 * Created by romananchugov on 14.04.2018.
 */

public class UploaderWrapper {

    private Link link;
    private File file;
    private Credentials credentials;

    public UploaderWrapper(Link link, File file, Credentials credentials) {
        this.link = link;
        this.file = file;
        this.credentials = credentials;
    }

    public Link getLink() {
        return link;
    }

    public File getFile() {
        return file;
    }

    public Credentials getCredentials() {
        return credentials;
    }
}
