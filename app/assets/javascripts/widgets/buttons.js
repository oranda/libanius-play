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

angular.module('widgets.buttons', []).factory('buttons', function (common) {

    var resetColors = function() {
        $('.response-choice-button').each(function(i, button) {
            common.resetColors(button)
        })
    }

    var setColors = function(correctResponse, selectedChoice) {
        $('.response-choice-button').each(function(i, responseChoiceButton) {
            setColorsForButton(correctResponse, selectedChoice, responseChoiceButton)
        })
    }

    var setColorsForButton = function(correctResponse, selectedChoice, responseChoiceButton) {
        var responseChoice = responseChoiceButton.value
        if (responseChoice == correctResponse)
            common.setColorCorrect(responseChoiceButton)
        else if (responseChoice == selectedChoice)
            common.setColorIncorrect(responseChoiceButton)
    }

    return { resetColors: resetColors, setColors: setColors };
});
