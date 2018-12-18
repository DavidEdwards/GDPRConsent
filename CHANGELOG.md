# Changelog

# 1.0.1

* Update to latest versions of dependencies.

# 1.0

* Parameters were wrong for the Java example.

# 0.9.5

* Toggling seen is not a necessary function. It must only set a request as seen.
* Use recommended method for inflating a DataBinding.

# 0.9.4

* Preferences will commit immediately to avoid them being overwritten.
* Seen requests are now set only once the user has seen them, and not when the App loads the fragment.

# 0.9.3

Due to user feedback, the following changes were made.

* Add a subtitle to the consent system. This will contain the title of the main App. This lets users know precisely what App is asking for permission.

# 0.9.2

Due to user feedback, the following changes were made.

* The circle / circle tick to the left of the title is now tappable to change consent state.
* The whole consent card is tappable to change consent state.
