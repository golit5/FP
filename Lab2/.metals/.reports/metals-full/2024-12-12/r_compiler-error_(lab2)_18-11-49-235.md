file:///C:/Users/Golits/Documents/FP/Lab2/src/main/scala/Main.scala
### dotty.tools.dotc.ast.Trees$UnAssignedTypeException: type of Ident(Seq) is not assigned

occurred in the presentation compiler.

presentation compiler configuration:


action parameters:
offset: 2566
uri: file:///C:/Users/Golits/Documents/FP/Lab2/src/main/scala/Main.scala
text:
```scala
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

val LOWERCASE_LETTERS = "abcdefghijklmnopqrstuvwxyz"
val UPPERCASE_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
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
        ).collect {
          case (false, errorMsg: String) => Right(errorMsg)
          case (true, _) => Left(true)
        }.reduce {
          case Seq[Either[A, @@]]
        } getOrElse Left(true)
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
      println(s"Не соблюдены все условия!: $errors\n")
      None 
    case (_, Left(false)) =>
      println("Неизвестная ошибка\n")
      None
  }.flatMap {
    case Some(password) => Future.successful(password) 
    case None => readPassword()
  }
}
```



#### Error stacktrace:

```
dotty.tools.dotc.ast.Trees$Tree.tpe(Trees.scala:74)
	dotty.tools.dotc.util.Signatures$.applyCallInfo(Signatures.scala:208)
	dotty.tools.dotc.util.Signatures$.computeSignatureHelp(Signatures.scala:104)
	dotty.tools.dotc.util.Signatures$.signatureHelp(Signatures.scala:88)
	dotty.tools.pc.SignatureHelpProvider$.signatureHelp(SignatureHelpProvider.scala:47)
	dotty.tools.pc.ScalaPresentationCompiler.signatureHelp$$anonfun$1(ScalaPresentationCompiler.scala:422)
```
#### Short summary: 

dotty.tools.dotc.ast.Trees$UnAssignedTypeException: type of Ident(Seq) is not assigned