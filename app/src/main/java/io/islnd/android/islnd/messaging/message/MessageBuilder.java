package io.islnd.android.islnd.messaging.message;

import io.islnd.android.islnd.messaging.Encoder;
import io.islnd.android.islnd.messaging.Identity;

public class MessageBuilder {
    public static Message buildIdentityMessage(String mailbox, Identity identity) {
        return new Message(
                mailbox,
                MessageType.IDENTITY,
                new Encoder().encodeToString(identity.toByteArray()));
    }

    public static Message buildProfileMessage(String mailbox, ProfileMessage profileMessage) {
        return new Message(
                mailbox,
                MessageType.PROFILE,
                new Encoder().encodeToString(profileMessage.toByteArray()));
    }
}
