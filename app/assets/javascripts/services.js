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

angular.module('libanius-play.services', []).service('services', function ($http) {

    var initialLoad = function() {
        return $http.get("/initialLoad")
    }

    var processUserResponse = function(quizData, response) {
         // add the response to the quiz data
        var quizDataWithUserResponse = $.extend({}, quizData, { response: response });

        return $http.post("/processUserResponse", quizDataWithUserResponse)
    }

    var removeCurrentWord = function (quizData) {
        var postData = {
            promptType: quizData.promptType,
            responseType: quizData.responseType,
            prompt: quizData.prompt,
            correctResponse: quizData.correctResponse
        }
        return $http.post("/removeCurrentWord", postData)
    }

    return { initialLoad: initialLoad, processUserResponse: processUserResponse,
        removeCurrentWord: removeCurrentWord };
});