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

angular.module('widgets.labels', []).factory('labels', function (common) {

    var resetColors = function() {
        $('.prev-choice').each(function(i, label) {
           common.resetColors(label)
        })
    }

    String.prototype.startsWith = function(str) {
        return this.indexOf(str) == 0;
    };

    var setColors = function(quizData) {
        $.each(quizData.prevChoices, function(i, prevChoice) {
            if (prevChoice.startsWith(quizData.prevCorrectResponse + " ="))
              common.setColorCorrect($('.prev-choice')[i])
            else if (prevChoice.startsWith(quizData.prevResponse + " ="))
              common.setColorIncorrect($('.prev-choice')[i])
        })
    }

    return { resetColors: resetColors, setColors: setColors };

});