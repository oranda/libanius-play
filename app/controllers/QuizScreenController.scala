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

package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json.{JsValue, Json}
import com.oranda.libanius.model.Quiz
import com.typesafe.config.ConfigFactory
import com.oranda.libanius.model.quizgroup.{WordMapping, QuizGroupHeader}
import com.oranda.libanius.dependencies.AppDependencyAccess
import com.oranda.libanius.util.{StringUtil, Util}
import com.oranda.libanius.model.quizitem.{QuizItem, QuizItemViewWithChoices}
import scala.{Array, Some}

import scala.concurrent.{ future, ExecutionContext }
import ExecutionContext.Implicits.global



case class Choices(choice1: String, choice2: String, choice3: String) {
  def toArray = Array(choice1, choice2, choice3)
}
object Choices { val empty = Choices("", "", "") }

object QuizScreenController extends Controller with AppDependencyAccess {

  implicit val dataWrites = Json.writes[DataToClient]
  implicit val choicesWrites = Json.writes[Choices]


  // Persistent (immutable) data structure used in this single-user local web application.
  var quiz: Quiz = _

  lazy val config = ConfigFactory.load()
  lazy val promptType = config.getString("libanius.promptType")
  lazy val responseType = config.getString("libanius.responseType")

  val qghPromptToResponse = QuizGroupHeader(WordMapping, promptType, responseType, "|")
  val qghResponseToPrompt = qghPromptToResponse.reverse

  def index = {
    quiz = loadQuiz
    Action { implicit request =>
      Ok(views.html.index())
    }
  }

  def initialLoad = Action { implicit request => testUserWithQuizItem() }

  def score: String = StringUtil.formatScore(quiz.scoreSoFar)

  private def loadQuiz: Quiz = {
    val quizGroups = List(qghPromptToResponse, qghResponseToPrompt).map(
        header => (header, dataStore.loadQuizGroup(header))).toMap
    Quiz(quizGroups)
  }

  def getParam(name: String)(implicit request: Request[JsValue]) =
    request.body.\(name).asOpt[String].headOption

  def getArrayParam(name: String)(implicit request: Request[JsValue]): Option[Array[String]] =
    request.body.\(name).asOpt[Array[String]].headOption

  def processUserResponse() = Action(parse.json) { implicit request =>

    val result: Option[Result] = for {
      response <- getParam("response")
      promptType <- getParam("promptType")
      responseType <- getParam("responseType")
      prompt <- getParam("prompt")
      correctResponse <- getParam("correctResponse")
      choices <- getArrayParam("choices")
      quizGroupHeader <- Some(QuizGroupHeader(WordMapping, promptType, responseType, "|"))
      quizItem <- quiz.findQuizItem(quizGroupHeader, prompt, correctResponse)
    } yield processResponseAndShowNextQuizItem(response, prompt,
        makePrevChoiceStrings(choices, quizGroupHeader), correctResponse,
        quizGroupHeader, quizItem)

    result.getOrElse {
      l.logError("Problem with data submitted by the form for processUserResponse")
      testUserWithQuizItem()
    }
  }

  private def makePrevChoiceStrings(choices: Array[String], quizGroupHeader: QuizGroupHeader):
      Array[String] =
    choices.map(makePrevChoiceString(_, quizGroupHeader))

  private def makePrevChoiceString(choice: String, quizGroupHeader: QuizGroupHeader): String = {
    val values = (quiz.findPromptsFor(choice, quizGroupHeader) match {
      case Nil => quiz.findResponsesFor(choice, quizGroupHeader.reverse)
      case values => values
    }).toList.slice(0, 3)
    choice + " = " + values.mkString(", ")
  }

  private def processResponseAndShowNextQuizItem(response: String, prevPrompt: String,
      prevChoices: Array[String], prevCorrectResponse: String,
      quizGroupHeader: QuizGroupHeader, quizItem: QuizItem): Result = {

    val isCorrect = quizItem.correctResponse.matches(response)
    Util.stopwatch(quiz = quiz.updateWithUserResponse(isCorrect, quizGroupHeader, quizItem),
        "updateWithUserAnswer")
    testUserWithQuizItem(prevPrompt, prevChoices, response, prevCorrectResponse)
  }

  def testUserWithQuizItem(prevPrompt: String = " ",
      prevChoiceStrings: Array[String] = Array("", "", ""), prevResponse: String = "",
      prevCorrectResponse: String = ""): Result = {
    val result: Result = Util.stopwatch(quiz.findPresentableQuizItem, "find quiz items") match {
      case (Some((quizItem, qgWithHeader))) =>
        quiz = quiz.addOrReplaceQuizGroup(qgWithHeader.header,
            qgWithHeader.quizGroup.updatedPromptNumber)
        showNextQuizItem(quizItem, prevPrompt, prevChoiceStrings, prevResponse, prevCorrectResponse)
      case _ => Ok
    }
    result
  }

  def finish = Action { Ok(views.html.finish()) }

  def showNextQuizItem(quizItem: QuizItemViewWithChoices, prevPrompt: String,
      prevChoiceStrings: Array[String], prevResponse: String, prevCorrectResponse: String):
      Result = {

    future { dataStore.saveQuiz(quiz, conf.filesDir) }

    Ok(Json.toJson(DataToClient(promptType = quizItem.promptType,
        responseType = quizItem.responseType,
        score = score,
        prompt = quizItem.prompt.value,
        numCorrectResponsesInARow = quizItem.numCorrectAnswersInARow,
        choices = Array(quizItem.allChoices(0), quizItem.allChoices(1), quizItem.allChoices(2)),
        correctResponse = quizItem.correctResponse.value,
        prevPrompt = prevPrompt,
        prevChoices = prevChoiceStrings,
        prevResponse = prevResponse,
        prevCorrectResponse = prevCorrectResponse)))
  }

  def removeCurrentWord =  Action(parse.json) { implicit request =>
    val result: Option[Result] = for {
      promptType <- getParam("promptType")
      responseType <- getParam("responseType")
      prompt <- getParam("prompt")
      correctResponse <- getParam("correctResponse")
    } yield removeWordAndShowNextQuizItem(promptType, responseType, prompt, correctResponse)
    result.getOrElse {
      l.logError("Problem with data submitted by the form for removeCurrentWord")
      testUserWithQuizItem()
    }
  }

  private def removeWordAndShowNextQuizItem(promptType: String, responseType: String,
      prompt: String, correctResponse: String): Result = {
    val qgHeader = QuizGroupHeader(WordMapping, promptType, responseType, "|")
    val quizItem = QuizItem(prompt, correctResponse)
    val (updatedQuiz, wasRemoved) = quiz.removeQuizItem(quizItem, qgHeader)
    quiz = updatedQuiz
    if (wasRemoved) l.log("Deleted quiz item " + quizItem)
    else l.logError("Failed to remove " + quizItem)
    testUserWithQuizItem()
  }
}

