petrolr_OBDTerminal
===================

This is a simple OBD Terminal for Android that can be used to pass commands and receive responses from an OBDII device. Compatible with any Bluetooth OBD reader. You type in the commands. The terminal gives you back the OBD response. Works on most cars 1996 and newer. That's about it.

How to use:

The various PIDs it's possible to request are described here: http://en.wikipedia.org/wiki/OBD-II_PIDs

You request the PID through the terminal by combining the Mode number with the PID number in hexadecimal. For example: to request Engine RPM one would enter 010C. 

The exact form of the OBD response will very depending on the vehicle OBD protocol and the settings on the OBD reader you're using. A typical response would look something like this: (Assuming one has requested engine RPM) "51 0C 0F 04". Where "51 0C" is 5 + requested mode number + requested PID number, "0F" is the A value from the OBD and "04" is the B value from the OBD. The A & B values can then be converted into more useful figures through the formulas as given on the wikipedia paged linked to above.

Responses from the OBD are written to a log file found on the phone under /android/data/petrolr/files.

If you have questions, contact me, and check out the consumer OBD app we built for Android here http://petrolr.com. 

http://petrolr.com || petrolr.app@gmail.com || matthew.helm@gmail.com
