Libanius-Play
================

*The Libanius-Play client is not currently being maintained. You might prefer to go to its successor, the Libanius-ScalaJs-React project: https://github.com/oranda/libanius-scalajs-react*

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


Implementation
==============

Libanius-Play is implemented using the Play Framework and AngularJS. 

AngularJS is a JavaScript library. Like jQuery it is geared towards asynchronous communication, but it provides an MVC framework. The Play Framework is also MVC, so the result is an MVC-within-MVC architecture (see diagram).  

![Libanius](http://3.bp.blogspot.com/-QCM4u1BrN30/Use39dLf0rI/AAAAAAAAACQ/6yMZkOckq9c/s1600/libanius-nestedMVC-small.jpg)

The AngularJS controller has a services layer to fetch data from the Scala back-end. Then it updates the page. The nice thing about it is that I don't have to update variables in the web page one by one: I can just bind variables in the web page to attributes in the model. This is achieved in the HTML using AngularJS tags like ng-model and the use of curly braces like this:  

```html
<span id="prompt-word">{{ quizData.prompt }}</span>
```

There are a number of variables like this scattered through the HTML, but they can all be updated at once in a single line of JavaScript if the Play backend returns a JSON structure. (The data structure can be defined as a case class in Scala on the server, and will be dynamically mirrored as a JavaScript structure on the client-side without the need to map attributes manually.) Here is a simplified version of what happens in the JavaScript controller after the server has processed a user response: basically it returns a new quiz item.  

```javascript
services.processUserResponse($scope.quizData, response).then(function(freshQuizData) {
   $scope.quizData = freshQuizData.data // updates values in the web page automagically
   labels.setColors($scope.quizData)    // extra UI work using a custom JavaScript module called labels
}
```

In the first line, services.processUserResponse is a function in the services.js file which sends user input to the server, and immediately returns a promise of new data. The promise has a handy then function which takes a callback argument, describing what happens when the data actually comes through, i.e. updating the view.


License
=======

Most Libanius-Play source files are made available under the terms of the GNU Affero General Public License (AGPL).
See individual files for details.

Attribution info is in [SOURCES](SOURCES.md).
