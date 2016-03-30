package io.islnd.android.islnd.messaging.server;

import java.util.List;

public class MessageQuery {
    private final List<String> data;

    public MessageQuery(List<String> data) {
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
