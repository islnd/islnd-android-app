package org.island.messaging.server;

public class ProfilePost {
    public ProfilePost(String blob) {
        this.blob = blob;
    }

    private String blob;

    public String getBlob() {
        return blob;
    }
}
