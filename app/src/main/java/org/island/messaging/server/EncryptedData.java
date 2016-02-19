package org.island.messaging.server;

public class EncryptedData {
    public EncryptedData(String blob) {
        this.blob = blob;
    }

    private String blob;

    public String getBlob() {
        return blob;
    }
}
