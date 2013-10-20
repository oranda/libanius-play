/*
 * Copyright 2013 James McCabe <james@oranda.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

$(function() {
    $('.response-choice-button').click(function() {
      submitAnswer(this)
    });
    $('#delete-button').click(function() {
      removeCurrentWord()
    });
});

function submitAnswer(selectedButton)
{
    resetPrevChoiceLabels()

    correctResponse = $("#correct-response").val()
    setColorsOnResponse(correctResponse, selectedButton)

    promptType = $("#prompt-type").val()
    responseType = $("#response-type").text()
    prompt = $("#prompt-word").text()

    choice1 = $("#response-choice-1").val()
    choice2 = $("#response-choice-2").val()
    choice3 = $("#response-choice-3").val()

    jsRoutes.controllers.QuizScreenController.processUserAnswer().ajax({
        url: '/processUserAnswer',
        data: "response=" + selectedButton.value +
            "&promptType=" + promptType + "&responseType=" + responseType +
            "&prompt=" + prompt + "&correctResponse=" + correctResponse +
            "&responseChoice1=" + choice1 + "&responseChoice2=" + choice2 +
            "&responseChoice3=" + choice3,
        dataType: 'html',
        success: function(data) {
          data = JSON.parse(data)
          setTimeout(function () {
            loadNewQuizItem(data);
          }, 500)
        }
    });
}

function removeCurrentWord()
{
    promptType = $("#prompt-type").val()
    responseType = $("#response-type").text()
    prompt = $("#prompt-word").text()
    correctResponse = $("#correct-response").val()

    jsRoutes.controllers.QuizScreenController.removeCurrentWord().ajax({
        url: '/removeCurrentWord',
        data: "&promptType=" + promptType + "&responseType=" + responseType +
            "&prompt=" + prompt + "&correctResponse=" + correctResponse,
        dataType: 'html',
        success: function(data) {
          data = JSON.parse(data)
          setTimeout(function () {
            loadNewQuizItem(data);
          }, 500)
        }
    });
}

function setColorsOnResponse(correctResponse, selectedButton)
{
    $('.response-choice-button').each(function(i, responseChoiceButton) {
        prevChoiceLabel = document.querySelectorAll('.prev-choice')[i]
        setColors(correctResponse, selectedButton, responseChoiceButton, prevChoiceLabel)
    })
}

function resetPrevChoiceLabels()
{
    $('.prev-choice').each(function(i, label) {
       $(label).text("")
       resetColors(label)
    })
}

function setColors(correctResponse, selectedButton, responseChoiceButton, prevChoiceLabel)
{
    responseChoice = responseChoiceButton.value
    selectedChoice = selectedButton.value

    if (responseChoice == correctResponse)
        setColorCorrect(new Array(responseChoiceButton, prevChoiceLabel))
    else if (responseChoice == selectedChoice)
        setColorIncorrect(new Array(responseChoiceButton, prevChoiceLabel))
}

function setColorCorrect(elems)
{
    jQuery.each(elems, function(i, elem) { elem.classList.add('correct-response') })
}

function setColorIncorrect(elems)
{
    jQuery.each(elems, function(i, elem) { elem.classList.add('incorrect-response') })
}

function resetButtonAndLabelColors()
{
    $('.response-choice-button').each(function(i, button) {
        resetColors(button)
    })
}

function resetColors(elem)
{
    elem.classList.remove('correct-response')
    elem.classList.remove('incorrect-response')
}

function loadNewQuizItem(data)
{
    resetButtonAndLabelColors()

    $("#prompt-word").text(data.prompt)
    $("#response-type").text(data.responseType)
    $("#score-text").text("Score: " + data.score)
    $("#num-correct-responses").text("(correctly answered " +
        data.numCorrectResponsesInARow + " times)")

    $("#correct-response").attr('value', data.correctResponse)
    $("#response-choice-1").attr('value', data.choices[0])
    $("#response-choice-2").attr('value', data.choices[1])
    $("#response-choice-3").attr('value', data.choices[2])
    $("#prev-prompt").text(data.prevPrompt)
    $("#prev-choice-1").text(data.prevChoices[0])
    $("#prev-choice-2").text(data.prevChoices[1])
    $("#prev-choice-3").text(data.prevChoices[2])
}