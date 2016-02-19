package org.island.messaging.crypto;

public class EncryptedData {
    public EncryptedData(String blob) {
        this.blob = blob;
    }

    private String blob;

    public String getBlob() {
        return blob;
    }
}
