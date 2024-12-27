package com.example2

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.example.GreeterMain.SayHello

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
            context.log.info(message.toString())
            Behaviors.same
        }
    }
@main def AddingMain():Unit =
    val system = ActorSystem(AddingSystem(),"system")
