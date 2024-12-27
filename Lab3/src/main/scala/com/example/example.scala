package com.example2

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.example.GreeterMain.SayHello
import scala.util.Try
import akka.util.Timeout
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit

//Актор, суммирующий значения
object AddingActor:
    //Определим тип суммируемого значения как Int или Double или String
    type Addable = Int | Double | String
    //Опишем сообщение, которое принимает актор: 2 суммируемых значения
    //и ссылка на актора, которому нужно будет вернуть результат
    case class AddMessage(a:Addable, b:Addable, replyTo:ActorRef[Addable])
    //Опишем поведение при приёме сообщения
    def apply[A]():Behavior[AddMessage] = Behaviors.receive{
        (context, message)=>
            //Pattern-matching для разных случаев и отправка сообщения назад
            message.replyTo ! {(message.a, message.b) match
                case (a:String, b:Addable) => a + b
                case (a:Addable, b:String) => b.prependedAll(a.toString())
                case (a:Int, b:Int) => a + b
                case (a:Double, b:Double) => a + b
            }
        //Поведение после обработки сообщения не меняется
        Behaviors.same
    }
//Наш ActorSystem
object AddingSystem:
    //Поведение при инициализации ActorSystem
    def apply():Behavior[AddingActor.Addable] = Behaviors.setup{ (context)=>
        //Порождение актора, суммирующего значения.
        //В переменной adder мы получим ссылку на нового актора
        val adder = context.spawn(AddingActor(), "adder")
        //Отправка сообщения порождённому актору.
        //context.self - получение ссылки на себя
        adder ! AddingActor.AddMessage(3,8,context.self)
        //После завершения инициализации мы меняем поведение
        //на записывание получаемых сообщений в лог
        Behaviors.receive{ (context, message) =>
            println(message.toString())
            Behaviors.same
        }
    }
@main def AddingMain():Unit =
    val system = ActorSystem(AddingSystem(),"system")



//Как и в прошлом примере, это актор,
//принимающий сообщения с числами и отправляющий их по обратному адресу
object AskAdder:
    //тип для сообщений
    case class Add(a:Double, b:Double, replyTo:ActorRef[Double])
    def apply():Behavior[Add] = Behaviors.receive{ (context, message) =>
        //Ждём секунду перед обработкой
        Thread.sleep(1)
        //Отправка ответа
        message.replyTo ! (message.a + message.b)
        Behaviors.same
    }
//Актор, использующий ask
object Asker:
    //тип для сообщений
    case class Add(a:Double, b:Double, whoToAsk:ActorRef[AskAdder.Add])
    //неявное определеление таймаута. Необязательно, можно передавать его
    //через дополнительный каррированный параметр (см. сигнатуру метода ask)
    given timeout:Timeout = Timeout(Duration.create(5, TimeUnit.SECONDS))
    //Поведение актора. Тип сообщений - Add или Double
    def apply():Behavior[Double|Add] = Behaviors.receive{(context, message) =>
        message.match
            //Если получили Double - логгируем его
            case x: Double => context.log.info(x.toString())
            //Если получили Add...
            case Add(a, b, whoToAsk) =>
                //Используя метод ask, запрашиваем результат у актора,
                //указанного в сообщении
                //В данном случае, мы посылаем сообщение по "адресу" в
                //переменной whoToAsk
                //Само сообщение формируется функцией, где аргумент -
                //ссылка на актор возвращающей сообщение.
                //Далее следует каррированный пареметр,
                //в который помещается функция,
                //которая будет использована при получении ответа, чтобы
                //преобразовать Try[T]=>T, Где T - ожидаемый тип ответа.
                //Эта функция будет выполнена над результатом Future[T],
                //который формируется для ожидания ответа в контексте актора.
                context.ask[AskAdder.Add, Double]
                    (whoToAsk, ref => AskAdder.Add(a,b,ref))
                    ((x:Try[Double]) => x.get)
        //Вне зависимости от сообщения, логгируем его
        println(s"message $message handled")
        Behaviors.same
    }

//Наша ActorSystem
object AskSystem:
    def apply():Behavior[(Double,Double)] = Behaviors.setup{ context=>
        //Порождаем по актору каждого типа
        val adder = context.spawn(AskAdder(),"adder")
        val asker = context.spawn(Asker(),"asker")
        //Отправляем asker'у сообщение, которое он перенаправит
        asker ! Asker.Add(3,90,adder)
        //Сразу после этого отправляем другое, соответствующее типу ответа
        asker ! 50
        //Логика перенаправления сообщений
        Behaviors.receiveMessage{
                case (a,b) =>
                    asker ! Asker.Add(a,b,adder)
                    Behaviors.same
        }
    }


@main def askMain():Unit =
    import Asker.Add
    //Порождаем систему
    val system = ActorSystem(AskSystem(),"system")
    //Отправляем ей парочку сообщений
    system ! (1,2)
    system ! (3,17)
    //Как и tell ("!"), ask - неблокирующая операция,
    //Поэтому после отправки сообщения, актор продолжит свою работу,
    //что видно из вывода логгера
