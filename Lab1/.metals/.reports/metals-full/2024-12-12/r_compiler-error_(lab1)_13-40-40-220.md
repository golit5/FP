file:///C:/Users/Golits/Documents/FP/Lab1/lab1.scala
### scala.MatchError: TypeDef(C,TypeBoundsTree(EmptyTree,EmptyTree,EmptyTree)) (of class dotty.tools.dotc.ast.Trees$TypeDef)

occurred in the presentation compiler.

presentation compiler configuration:


action parameters:
offset: 708
uri: file:///C:/Users/Golits/Documents/FP/Lab1/lab1.scala
text:
```scala
def helloI(n: Int) =
    for (i <- 0 until n) {
        val x = if (i % 2 == 0) i else n - i
        print(s"hello $x\n")
    }

def evenOdd(numbers: Seq[Int]): (Seq[Int],Seq[Int]) = 
    (Range(0,numbers.length).filter(_%2==0).map(numbers.apply(_)),
     Range(0,numbers.length).filter(_%2==1).map(numbers.apply(_)))


def max(numbers: Seq[Int]): Int = 
    numbers.reduce((max, x) => if(x > max) x else max)

def multiplyFunc(multiplier:Int): Seq[Int] => Seq[Int] =
    (numbers:Seq[Int]) => numbers.map(x => multiplier * x)

def multiply(numbers: Seq[Int], multiplier: Int): Seq[Int] = 
    numbers.map(x => multiplier * x)

def compose[A, B, C](f: A => B, g: B => C) = f.andThen(g)
№@@
trait Notification
case class Email(sender: String, title: String, body: String) extends Notification
case class SMS(caller: String, message: String) extends Notification
case class VoiceRecording(contactName: String, link: String) extends Notification
def showNotification(notification: Notification): String =
    notification match
        case Email(sender, title, _) =>
            s"You got an email from $sender with title: $title"
        case SMS(number, message) =>
            s"You got an SMS from $number! Message: $message"
        case VoiceRecording(name, link) =>
            s"You received a Voice Recording from $name! Click the link to hear it: $link"

@main def entry() = 
    println("Hello World")

    helloI(4)

    val sequence = Seq(1,2,3,4)

    val (even, odd) = evenOdd(sequence)
    println(even)
    println(odd)

    println(max(sequence))

    println(multiply(sequence,3))

    val maxFunction = max
    println(max)
    println(maxFunction)

    val someSms = SMS("12345", "Are you there?")
    val someVoiceRecording = VoiceRecording("Tom", "voicerecording.org/id/123")
    println(showNotification(someSms))
    println(showNotification(someVoiceRecording))

    val multiply2 = multiplyFunc(2)
    val mulMax = compose(multiply2, max)
    println(mulMax(sequence))
 

```



#### Error stacktrace:

```
dotty.tools.pc.completions.KeywordsCompletions$.checkTemplateForNewParents$$anonfun$2(KeywordsCompletions.scala:218)
	scala.Option.map(Option.scala:242)
	dotty.tools.pc.completions.KeywordsCompletions$.checkTemplateForNewParents(KeywordsCompletions.scala:215)
	dotty.tools.pc.completions.KeywordsCompletions$.contribute(KeywordsCompletions.scala:44)
	dotty.tools.pc.completions.Completions.completions(Completions.scala:124)
	dotty.tools.pc.completions.CompletionProvider.completions(CompletionProvider.scala:90)
	dotty.tools.pc.ScalaPresentationCompiler.complete$$anonfun$1(ScalaPresentationCompiler.scala:146)
```
#### Short summary: 

scala.MatchError: TypeDef(C,TypeBoundsTree(EmptyTree,EmptyTree,EmptyTree)) (of class dotty.tools.dotc.ast.Trees$TypeDef)