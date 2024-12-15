file:///C:/Users/Golits/Documents/FP/Lab2/src/main/scala/Main.scala
### java.nio.file.InvalidPathException: Illegal char <:> at index 3: jar:file:///C:/Users/Golits/AppData/Local/Coursier/cache/v1/https/repo1.maven.org/maven2/org/scala-lang/scala-library/2.13.14/scala-library-2.13.14-sources.jar!/scala/Tuple2.scala

occurred in the presentation compiler.

presentation compiler configuration:


action parameters:
offset: 2811
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
          case (x: Left[Boolean, String], y: Right[Boolean, String]) => Right(y.value)
          case (x: Right[Boolean, String], y: Left[Boolean, String]) => Right(x.value)
          case (x: Right[Boolean, String], y: Right[Boolean, String]) => Right(x.value + "\n" + y.value)
          case (_, _) => Left(true)
        } m@@
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
```



#### Error stacktrace:

```
java.base/sun.nio.fs.WindowsPathParser.normalize(WindowsPathParser.java:204)
	java.base/sun.nio.fs.WindowsPathParser.parse(WindowsPathParser.java:175)
	java.base/sun.nio.fs.WindowsPathParser.parse(WindowsPathParser.java:77)
	java.base/sun.nio.fs.WindowsPath.parse(WindowsPath.java:92)
	java.base/sun.nio.fs.WindowsFileSystem.getPath(WindowsFileSystem.java:231)
	java.base/java.nio.file.Path.of(Path.java:148)
	java.base/java.nio.file.Paths.get(Paths.java:69)
	scala.meta.io.AbsolutePath$.apply(AbsolutePath.scala:58)
	scala.meta.internal.metals.MetalsSymbolSearch.$anonfun$definitionSourceToplevels$2(MetalsSymbolSearch.scala:70)
	scala.Option.map(Option.scala:242)
	scala.meta.internal.metals.MetalsSymbolSearch.definitionSourceToplevels(MetalsSymbolSearch.scala:69)
	dotty.tools.pc.completions.CaseKeywordCompletion$.dotty$tools$pc$completions$CaseKeywordCompletion$$$sortSubclasses(MatchCaseCompletions.scala:342)
	dotty.tools.pc.completions.CaseKeywordCompletion$.matchContribute(MatchCaseCompletions.scala:292)
	dotty.tools.pc.completions.Completions.advancedCompletions(Completions.scala:350)
	dotty.tools.pc.completions.Completions.completions(Completions.scala:120)
	dotty.tools.pc.completions.CompletionProvider.completions(CompletionProvider.scala:90)
	dotty.tools.pc.ScalaPresentationCompiler.complete$$anonfun$1(ScalaPresentationCompiler.scala:146)
```
#### Short summary: 

java.nio.file.InvalidPathException: Illegal char <:> at index 3: jar:file:///C:/Users/Golits/AppData/Local/Coursier/cache/v1/https/repo1.maven.org/maven2/org/scala-lang/scala-library/2.13.14/scala-library-2.13.14-sources.jar!/scala/Tuple2.scala