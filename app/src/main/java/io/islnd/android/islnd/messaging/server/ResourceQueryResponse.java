package io.islnd.android.islnd.messaging.server;

import java.util.List;

import io.islnd.android.islnd.messaging.crypto.EncryptedResource;

public class ResourceQueryResponse {
    private Data data;

    private class Data {
        List<EncryptedResource> resources;
    }

    public List<EncryptedResource> getResources() {
        return this.data.resources;
    }
}
