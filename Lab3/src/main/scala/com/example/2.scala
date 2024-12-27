package com.example

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.io.StdIn.readLine


object AddingServer:
    
    type Addable = Int | Double | String
    
    case class AddPair(a:Addable, b:Addable)
    case class ServerMessage(pair:AddPair, replyTo:ActorRef[Addable])
    
    def apply[A]():Behavior[ServerMessage] = Behaviors.receive{
        (context, message)=>
            
            message.replyTo ! {(message.pair.a, message.pair.b) match
                case (a:String, b:Addable) => a + b
                case (a:Addable, b:String) => b.prependedAll(a.toString())
                case (a:Int, b:Int) => a + b
                case (a:Double, b:Double) => a + b
                case (a:Int, b:Double) => a.toDouble + b
                case (a:Double, b:Int) => a + b.toDouble
            }
        
        Behaviors.same
    }

object AddingClient:

    case class ClientMessage(pair:AddingServer.AddPair, sendTo:ActorRef[AddingServer.ServerMessage])

    def apply[A]():Behavior[ClientMessage | AddingServer.Addable] = Behaviors.receive{
        (context, message)=>
            {message match 
                case (msg:ClientMessage) =>
                    msg.sendTo ! AddingServer.ServerMessage(msg.pair, context.self)
                case (result:AddingServer.Addable) =>
                    println("The result of adding is %s".format(message.toString()))
            }
        Behaviors.same
    }
    
object AddingSystem:
    
    def apply():Behavior[AddingServer.AddPair] = Behaviors.setup{ (context)=>
        
        val server = context.spawn(AddingServer(), "server")
        val client = context.spawn(AddingClient(), "client")
        
        Behaviors.receive{ (context, message) =>
            println(message.toString())
            client ! AddingClient.ClientMessage(message, server)
            Behaviors.same
        }
    }
@main def AddingMain():Unit =
    val system = ActorSystem(AddingSystem(),"system")
    while (true) {
        //Сложение только строк, чтобы не усложнять код
        println("Enter the first Addable:")
        val a = readLine()
        println("Enter the second Addable:")
        val b = readLine()
        system ! AddingServer.AddPair(a, b)
    }