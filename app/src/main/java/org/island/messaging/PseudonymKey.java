package org.island.messaging;

import java.io.Serializable;
import java.security.Key;

public class PseudonymKey implements Serializable {
    private long uniqueID;
    private String username;
    private String pseudonym;
    private Key key;

    public PseudonymKey(long uniqueID, String username, String pseudonym, Key key) {
        this.uniqueID = uniqueID;
        this.username = username;
        this.pseudonym = pseudonym;
        this.key = key;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PseudonymKey)) {
            return false;
        }

        PseudonymKey otherKey = (PseudonymKey)other;
        return this.uniqueID == otherKey.uniqueID
                && this.pseudonym.equals(otherKey.pseudonym)
                && this.username.equals(otherKey.username)
                && this.key.equals(otherKey.key);
    }

    public boolean isNewer(PseudonymKey pseudonymKey) {
        return (uniqueID > pseudonymKey.uniqueID);
    }

    public String getUsername() {
        return this.username;
    }
}
