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

angular.module('widgets.common', []).factory('common', function () {

    var resetColors = function(elem) {
        elem.classList.remove('correct-response')
        elem.classList.remove('incorrect-response')
    }

    var setColorCorrect = function(widget) {
        widget.classList.add('correct-response')
    }

    var setColorIncorrect = function(widget) {
        widget.classList.add('incorrect-response')
    }

    return { resetColors: resetColors, setColorCorrect: setColorCorrect,
        setColorIncorrect: setColorIncorrect };
});