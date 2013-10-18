Libanius-Play
================

Libanius is an app to aid learning. Basically it presents "quiz items" to the user, and for each one the user must select the correct answer option. Quiz items are presented at random according to a certain algorithm. An item has to be answered correctly several times before it is considered learnt.

The core use is as a vocabulary builder in a new language, but it is designed to be flexible enough to present questions and answers of all types.

The implementation is in Scala. There are Android and Web-based interfaces.

This project is the Web interface to Libanius, implemented using the Play Framework. The core Libanius code is located here: https://github.com/oranda/libanius

Suggestions for new features and code improvements will be happily received by:

James McCabe <james@oranda.com>


Install
=======

Currently this is just a single-user application and you need to have Scala and the Play Framework installed on your PC to get this to work. It has been tested with Play 2.2, sbt 0.13, and browsers Chrome 17, and Firefox 20. 

To install, either download the zip file for this project or clone it with git:

    git clone git://github.com/oranda/libanius-play

Then cd to the libanius-play directory and run it:

    play run

Then just open your browser at http://localhost:9000/


Screenshots
===========

![Libanius](https://github.com/oranda/libanius-play/raw/master/docs/screenshotQuizV01.png)


License
=======

The application is licenced under the Apache License, Version 2.0.

Attribution info is in [SOURCES](SOURCES.md).
