import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global // без .Implicits ломается
import scala.concurrent.duration.Duration
import scala.concurrent.Await

import scala.util.Try
import scala.util.Success
import scala.util.Failure

import scala.io.StdIn.readLine

@main def main() = {
  var a = readPassword()
  Await.ready(a,Duration.Inf)
}

def integrate(f: Double=>Double, l: Double, r: Double, i: Int): Double = {
  val step: Double = (r - l)/i
  val values = Range(0, i).map(l + step / 2 + step * _).map(f(_) * step)
  return values.reduce(_ + _)
}


def integrateConcurent(f: Double=>Double, l: Double, r: Double, i: Int, t: Int): Double = {
  val step: Double = (r-l)/t
  val futures: Seq[Future[Double]] = Range(0, t).map((i: Int) => Future {integrate(f, l + step * i, l + step * (i + 1), i)})
  val future: Future[Seq[Double]] = Future.sequence(futures)
  val res = Await.result(future, Duration.Inf)
  return res.fold(0.0)(_ + _)
}

val LOWERCASE_LETTERS = "abcdefghijklmnopqrstuvwxyzабвгдеёжзийклмнопрстуфхцчшщъыьэуя"
val UPPERCASE_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭУЯ"
val NUMBERS_LETTERS = "1234567890"
val SPECIAL_LETTERS = "!@#$%^&*()[]{};:,./<>?|"

def hasNSymbols(passwordCandidate: String, someLetters: String, n:Int): Boolean = passwordCandidate.count(someLetters.contains(_)) >= n

def goodEnoughPassword(password:String): Boolean = {
 var reqs: Seq[Boolean] = Seq(password.length() >= 8,
                              hasNSymbols(password,LOWERCASE_LETTERS, 1),
                              hasNSymbols(password,UPPERCASE_LETTERS, 1),
                              hasNSymbols(password,NUMBERS_LETTERS, 1),
                              hasNSymbols(password,SPECIAL_LETTERS, 1))
 return reqs.reduce(_ && _)
}

def tryGoodEnoughPassword(password:String): Either[Boolean, String] = {
  Try{
    Seq(
          (password.length >= 8, "Пароль должен содержать не менее 8 символов"),
          (hasNSymbols(password, LOWERCASE_LETTERS, 1), "Пароль должен содержать хотя бы одну заглавную букву"),
          (hasNSymbols(password, UPPERCASE_LETTERS, 1), "Пароль должен содержать хотя бы одну строчную букву"),
          (hasNSymbols(password, NUMBERS_LETTERS, 1), "Пароль должен содержать хотя бы одну цифру"),
          (hasNSymbols(password, SPECIAL_LETTERS, 1), "Пароль должен содержать хотя бы один специальный символ")
        ).reduce { 
          case ((false, errorMsg_1: String),  (false, errorMsg_2: String))  => (false, errorMsg_1 + "\n" + errorMsg_2)
          case ((false, errorMsg_1: String),  (true, _))                    => (false, errorMsg_1)
          case ((true, _),                    (false, errorMsg_2: String))  => (false, errorMsg_2)
          case ((true, _),                    (true, _))                    => (true, "")
        } match {
          case (false, errorMsg: String) => Right(errorMsg)
          case (true, _) => Left(true)
        }
  } match {
    case Success(result) => result
    case Failure(exception) => Left(false)
  }
}


def readPassword(): Future[String] = {
  Future {
    printf("Введите пароль: ")
    readLine()
  }.map { password =>
    (password, tryGoodEnoughPassword(password))
  }.map {
    case (password: String, Left(true)) =>
      Some(password) 
    case (_, Right(errors)) =>
      println(s"Не соблюдены условия!:\n$errors")
      None 
    case (_, Left(false)) =>
      println("Неизвестная ошибка\n")
      None
  }.flatMap {
    case Some(password) => Future.successful(password) 
    case None => readPassword()
  }
}