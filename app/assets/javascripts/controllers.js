/*
 * Libanius-Play
 * Copyright (C) 2013-2014 James McCabe <james@oranda.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

'use strict';

angular.module('libanius-play.controllers', ['libanius-play.services', 'widgets.buttons',
        'widgets.common', 'widgets.labels']).
    controller('QuizCtrl', function ($scope, $http, $location, services, buttons, labels) {
        console.log("Inside QuizCtrl")

        // Define what happens in the view when the initial load of data returns from the server
        $scope.initialLoad = function() {
            services.initialLoad().then(function(freshQuizData) {
                $scope.quizData = freshQuizData.data  // updates values in the web page
                buttons.resetColors()
            })
        };

        // Define what happens in the view when the post of a user response returns from the server
        $scope.processUserResponse = function(response) {
            services.processUserResponse($scope.quizData, response).then(function(freshQuizData) {
                console.log("prompt: " + freshQuizData.data.prompt)
                if (typeof freshQuizData.data.prompt == "undefined") {
                    window.location = "/finish";
                } else {
                    $scope.quizData = freshQuizData.data // updates values in the web page
                    labels.setColors($scope.quizData)
                    buttons.resetColors()
                }
            })
        };

        // Define what happens when the form in the view is submitted
        $scope.submitResponse = function(response) {
            labels.resetColors()
            buttons.setColors($scope.quizData.correctResponse, response)
            $scope.processUserResponse(response)
        };

        // Define what happens in the view after a word in removed on the server
        $scope.removeCurrentWord = function() {
            services.removeCurrentWord($scope.quizData).then(function() {
                $scope.initialLoad()
            })
        };

        $scope.initialLoad();
    });