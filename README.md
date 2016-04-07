###NOTE: This communication software is in an experimental state, and it's security is not guaranteed.

## What is islnd?
Islnd is an Android app where people can share thoughts and ideas with their friends. It uses end-to-end encryption, so that no intermediate party can read messages, and it uses a messaging protocol that obscures metadata.

## Who should use islnd?
Islnd is designed for people who want to have social interaction online, but they are uncomfortable with third parties reading their thoughts and ideas. No knowledge of cryptography is required to use islnd.

## Our philosophy
We believe that a company that offers a communication does not inherently have a right to know everything people communicate on the tool.

## Supported devices
Islnd is currently available on Android devices.

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
		
## Technical documentation
For more information, please see [the wiki](https://github.com/Daviddt17/island-android-app/wiki/Technical-documentation)

## Contributing

We're not ready for that yet!

## License

TODO: Write license