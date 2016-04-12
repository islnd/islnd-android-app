package io.islnd.android.islnd.messaging.server;

import java.util.ArrayList;
import java.util.List;

public class ResourceQuery {
    private final List<String> data;

    public ResourceQuery(String resourceKey) {
        this.data = new ArrayList<>();
        this.data.add(resourceKey);
    }

    public ResourceQuery(List<String> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String s : data) {
            sb.append(s + ", ");
        }

        return sb.toString();
    }
}
