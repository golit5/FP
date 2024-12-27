//#full-example
package com.example

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.example.GreeterMain.SayHello

//#greeter-actor
object Greeter {
  // Определение двух сообщений:
  // - Greet: запрос на привествие, принимающий имя и референс на актора, которому нужно отправить ответ
  // - Greeted: сообщение с результатом привествия, содержит имя и ссылку на актора, который отправил ответ
  final case class Greet(whom: String, replyTo: ActorRef[Greeted])
  final case class Greeted(whom: String, from: ActorRef[Greet])

  // Поведение актера Greeter для полученного запроса на приветствие
  def apply(): Behavior[Greet] = Behaviors.receive { (context, message) =>
    // Выводим сообщение с привествием
    println("Hello %s!".format(message.whom))
    
    //#greeter-send-messages
    // Отправляем сообщение Greeted обратно отправителю, подтверждая привествие
    message.replyTo ! Greeted(message.whom, context.self)
    //#greeter-send-messages
    
    // Не меняем поведение
    Behaviors.same
  }
}
//#greeter-actor

//#greeter-bot
object GreeterBot {

  // Функция, создающая бота, который будет отвечать на привествия до достижения максимального количества
  def apply(max: Int): Behavior[Greeter.Greeted] = {
    bot(0, max) // Инициализация с начальным счётчиком привествий
  }

  // Вспомогательная рекурсивная функция для отслеживания количества привествий
  private def bot(greetingCounter: Int, max: Int): Behavior[Greeter.Greeted] =
    Behaviors.receive { (context, message) =>
      // Увеличиваем счётчик привествий
      val n = greetingCounter + 1
      // Логируем, что был получен ответ
      println("Greeting %d for %s".format(n, message.whom))
      
      // Если достигнут предел максимальных привествий, останавливаем актера
      if (n == max) {
        Behaviors.stopped
      } else {
        // Если не достигнут предел, отправляем запрос на новое привествие
        message.from ! Greeter.Greet(message.whom, context.self)
        // Рекурсивный вызов для продолжения работы
        bot(n, max)
      }
    }
}
//#greeter-bot

//#greeter-main
object GreeterMain {

  // Сообщение, которое отправляется для начала общения
  final case class SayHello(name: String)

  // Главный метод для создания начального поведения
  def apply(): Behavior[SayHello] =
    Behaviors.setup { context =>
      //#create-actors
      // Создание актора Greeter, который будет заниматься привествиями
      val greeter = context.spawn(Greeter(), "greeter")
      //#create-actors

      // Поведение для обработки сообщения SayHello
      Behaviors.receiveMessage { message =>
        //#create-actors
        // Создание актора GreeterBot, который принимать привествия от greeter
        val replyTo = context.spawn(GreeterBot(max = 3), message.name)
        //#create-actors
        
        // Отправка актора Greeter запроса на привествие
        greeter ! Greeter.Greet(message.name, replyTo)
        
        // Возврат к ожиданию следующего сообщения
        Behaviors.same
      }
    }
}
//#greeter-main

//#main-class
object AkkaQuickstart extends App {
  //#actor-system
  // Создание акторной системы с актором GreeterMain
  val greeterMain: ActorSystem[GreeterMain.SayHello] = ActorSystem(GreeterMain(), "AkkaQuickStart")
  //#actor-system

  //#main-send-messages
  // Отправка сообщения SayHello с именем "Charles", чтобы инициировать общение между Greeter и GreeterBot
  greeterMain ! SayHello("Charles")
  //#main-send-messages
}
//#main-class
//#full-example
