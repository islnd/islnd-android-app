###NOTE: This communication software is in an experimental state, and it's security is not guaranteed.

## What is islnd?
Islnd is an Android app where people can share thoughts and ideas with their friends. It uses end-to-end encryption, so that no intermediate party can read messages, and it uses a messaging protocol that obscures metadata.

##The communication model
When islnd is installed, the device creates a public/private key pair and third key called a *group key*. The public/private key pair is used to sign messages and verify their authenticity. The *group key* is used encrypting and decrypting the messages (symmetric encryption).  

When two users become friends, they share their public key and *group key* with each other through a QR code scan. If user A is friends with {B, C, D}, then all four parties will know A's *group key*. This allows a message to be encrypted once and sent to many recipients. Since all of these users have the *group key*, authenticity is provided by signing and verifying messages with a public/private key pair. In this case, a message from A would be encrypted with A's *group key* and signed with A's private key.  

##Non-repudiation
Since all messages are signed and verified with a user's public/private key pair, any third-party can verify that a message was created by the user that posseses the private-key. This is different than a protocol like OTR, where both parties can verify each others' messages, but neither party can prove any message to a third-party. Non-repudiation may be undesirable for certain scenarios, and users should choose whatever protocol is most appropriate for their security requirements and threat models.

##Obscuring meta-data
Users have no unique username that they use on the service. Instead, the app chooses a random *alias*. Friends all know what *alias* their friend is currently using. Part of the messaging protocol is that users can choose a new *alias* at any time.  

The server stores all of the data events in a database. The server simply sees an *alias* and an encrypted binary blob. Since the *alias* can change at any time, no metadata patterns can be revealed by static analysis of the data on the server. Certain types of traffic analysis are possible and will be discussed later.  

From the server's point of view, posts, comments, and changes to a user's profile 
all look the same. An *alias* and an encrypted binary blob.
##Forward secrecy
The protocol will allow users to change their *group key* at any time. The advantage of this is that if any device is compromised, content that was encrypted with previous *group keys* cannot be decrypted. However, the decrypted content may be stored on the phone that was compromised -- for example if Sally's *group key* is stolen from Bob's phone, Bob's phone probably has all of Sally's posts on it as well. So even though Sally's *group key* cannot decrypt all of Sally's messages, the comromised device will have many of Sally's message on it.
##Revealing meta-data through traffic analysis
Even if a user changes their *alias* frequently, it is likely that they requests to the server are coming from the same IP address. If the server records these IP addresses, they can make connections between all of the *aliases* and consider them one user. The ease of creating these logical connections varies depending on the access mode of the user -- a home router, a network at school or work, connecting over a mobile data carrier, VPN, Tor, etc...

Metadata is also revealed through the queries themselves. When a user gets new content, they query the server for the events for a set of *aliases*. Even though users can change their *alias* at any time, a single query like this reveals that at this state in time, that set of *aliases* was related in some way. A query like this may also reveal a logical connection between a users' *aliases*. For example, I query for users [A, B, C, D]. Then user D changes their alias to E. On my next query, I query for users [A, B, C, E]. If a server records and analyzes these queries, a connection between *alias* D and E can be revealed.  

Another possible analysis method is counting the number of times users query for a specific *alias*. This could reveal the popularity of a certain user or content item.  

We believe these analysis opportunities could be eliminated if the network is sufficiently distributed. However, this has not been formally proven, and the protocol to create this de-centralized version of islnd has not been proposed.

##Current issues with the app

* Our app stores all messages in plain-text on the device itself. If the Android device has not enabled disk encryption and the device is stolen, all data can easily be comprimised. We want to provide a way to encrypt all app data with a password.
* Our app stores the user's secret keys in the shared preferences. If the device is compromised the keys can be compromised.
* Users are changing *aliases* every time they post content, but they are not changing their *group key*, so forward secrecy is not provided.

## Installation

	- IDE: Android Studio 1.5
	- SDK and Tools
		- API 23
		- Android SDK Tools 24.4.1
		- Android SDK Platform-tools 23.1
		- Android SDK Build-tools 23.0.1
		- Android Support Repository 25
		- Android Support Library

## Contributing

We're not ready for that yet!

## License

TODO: Write license