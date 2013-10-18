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

package controllers

import play.api._
import play.api.mvc._
import com.oranda.libanius.dependencies.AppDependencyAccess
import play.api.libs.json.Json
import com.oranda.libanius.model.{WordMapping, QuizGroupHeader, Quiz}
import com.oranda.libanius.util.{StringUtil, Util}
import com.oranda.libanius.model.quizitem.{QuizItem, QuizItemViewWithChoices}
import play.api.mvc.Result

import scala.concurrent.{ future, ExecutionContext }
import ExecutionContext.Implicits.global
import com.typesafe.config.ConfigFactory


case class StatusMessage(value: String)

case class Choices(choice1: String, choice2: String, choice3: String) {
  def toArray = Array(choice1, choice2, choice3)
}
object Choices { val empty = Choices("", "", "") }

object QuizScreenController extends Controller with AppDependencyAccess {

  implicit val statusWrites = Json.writes[StatusMessage]

  implicit val choicesWrites = Json.writes[Choices]
  implicit val dataWrites = Json.writes[DataToClient]

  // Persistent (immutable) data structure used in this single-user local web application.
  var quiz: Quiz = _

  lazy val config = ConfigFactory.load()
  lazy val promptType = config.getString("libanius.promptType")
  lazy val responseType = config.getString("libanius.responseType")

  val qghEnglishGerman = QuizGroupHeader(WordMapping, promptType, responseType)
  val qghGermanEnglish = qghEnglishGerman.reverse

  def index = {
    quiz = loadQuiz
    Action { implicit request =>
      testUserWithQuizItem(usePageForData)(request)
    }
  }

  private def loadQuiz: Quiz = {
    val quizGroups = List(qghEnglishGerman, qghGermanEnglish).map(
        header => (header, dataStore.loadQuizGroup(header))).toMap
    Quiz(quizGroups)
  }

  def testUserWithQuizItem(viewForData: DataToClient => Result, prevPrompt: String = "",
      prevChoiceStrings: Array[String] = Array("", "", ""))(implicit request: Request[AnyContent]) =
    Util.stopwatch(quiz.findPresentableQuizItem, "find quiz items") match {
      case (Some((quizItem, qgWithHeader))) =>
        quiz = quiz.addOrReplaceQuizGroup(qgWithHeader.header,
            qgWithHeader.quizGroup.updatedPromptNumber)
        showNextQuizItem(viewForData, quizItem, prevPrompt, prevChoiceStrings)
      case _ =>
        Ok(views.html.finish(score))
    }

  def score: String = StringUtil.formatScore(quiz.scoreSoFar)

  def usePageForData(data: DataToClient)(implicit request: Request[AnyContent]): Result =
    Ok(views.html.index(data))
  def useJsonForData(data: DataToClient): Result = {
    implicit val DataToClientFormat = Json.format[DataToClient]
    Ok(Json.toJson(data))
  }

  def showNextQuizItem(viewForData: DataToClient => Result, quizItem: QuizItemViewWithChoices,
      prevPrompt: String, prevChoiceStrings: Array[String])
      (implicit session: Session): Result  = {

    future { dataStore.saveQuiz(quiz, conf.filesDir) }

    viewForData(DataToClient(promptType = quizItem.promptType,
        responseType = quizItem.responseType,
        score = score,
        prompt = quizItem.prompt.value,
        numCorrectResponsesInARow = quizItem.numCorrectAnswersInARow,
        choices = Array(quizItem.allChoices(0), quizItem.allChoices(1), quizItem.allChoices(2)),
        correctResponse = quizItem.correctResponse.value,
        prevPrompt = prevPrompt,
        prevChoices = prevChoiceStrings))
  }

  def processUserAnswer() = Action { implicit request =>

    val req = request.body.asFormUrlEncoded

    val result: Option[Result] = for {
      response <- req.get("response").headOption
      promptType <- req.get("promptType").headOption
      responseType <- req.get("responseType").headOption
      prompt <- req.get("prompt").headOption
      correctResponse <- req.get("correctResponse").headOption
      responseChoice1 <- req.get("responseChoice1").headOption
      responseChoice2 <- req.get("responseChoice2").headOption
      responseChoice3 <- req.get("responseChoice3").headOption
      quizGroupHeader <- Some(QuizGroupHeader(WordMapping, promptType, responseType))
      quizItem <- quiz.findQuizItem(quizGroupHeader, prompt, correctResponse)
    } yield processAnswerAndShowNextQuizItem(response, prompt,
        makePrevChoiceStrings(responseChoice1, responseChoice2, responseChoice3),
            quizGroupHeader, quizItem)
    result.getOrElse {
      l.logError("Problem with data submitted by the form for processUserAnswer")
      testUserWithQuizItem(useJsonForData)
    }
  }

  private def makePrevChoiceStrings(choices: String*): Array[String] =
    choices.map(makePrevChoiceString(_)).toArray

  private def makePrevChoiceString(choice: String): String = {
    val values = quiz.findResponsesFor(choice, qghGermanEnglish) match {
      case Nil => quiz.findPromptsFor(choice, qghEnglishGerman)
      case values => values
    }
    choice + " = " + values.mkString(", ")
  }

  private def processAnswerAndShowNextQuizItem(response: String, prevPrompt: String,
      prevChoices: Array[String], quizGroupHeader: QuizGroupHeader, quizItem: QuizItem)
      (implicit request: Request[AnyContent]): Result = {
    val isCorrect = quizItem.correctResponse.matches(response)
    Util.stopwatch(quiz = quiz.updateWithUserAnswer(isCorrect, quizGroupHeader, quizItem),
        "updateWithUserAnswer")
    testUserWithQuizItem(useJsonForData, prevPrompt, prevChoices)
  }

  def getStatus = Action {
    Ok(Json.toJson(StatusMessage("Change to status message...")))
  }

  def removeCurrentWord =  Action { implicit request =>
    val req = request.body.asFormUrlEncoded

    val result: Option[Result] = for {
      promptType <- req.get("promptType").headOption
      responseType <- req.get("responseType").headOption
      prompt <- req.get("prompt").headOption
      correctResponse <- req.get("correctResponse").headOption
    } yield removeWordAndShowNextQuizItem(promptType, responseType, prompt, correctResponse)
    result.getOrElse {
      l.logError("Problem with data submitted by the form for removeCurrentWord")
      testUserWithQuizItem(useJsonForData)
    }
  }

  private def removeWordAndShowNextQuizItem(promptType: String, responseType: String,
      prompt: String, correctResponse: String)(implicit request: Request[AnyContent]): Result = {
    val qgHeader = QuizGroupHeader(WordMapping, promptType, responseType)
    val quizItem = QuizItem(prompt, correctResponse)
    val (updatedQuiz, wasRemoved) = quiz.removeQuizItem(quizItem, qgHeader)
    quiz = updatedQuiz
    if (wasRemoved) l.log("Deleted quiz item " + quizItem)
    else l.logError("Failed to remove " + quizItem)
    testUserWithQuizItem(useJsonForData)
  }


  def javascriptRoutes = Action { implicit request =>
    Ok(Routes.javascriptRouter("jsRoutes")
        (routes.javascript.QuizScreenController.processUserAnswer,
         routes.javascript.QuizScreenController.removeCurrentWord)
    ).as(JAVASCRIPT)
  }

}