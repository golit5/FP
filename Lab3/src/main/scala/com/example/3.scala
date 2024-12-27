package com.example

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.util.Try
import scala.math.Pi
import scala.math.cos
import scala.io.StdIn.readLine

object IntegrateActor:
    case class Message(integral: IntegrateSystem.Integral, replyTo: ActorRef[Double])
    
    def apply(): Behavior[Message] = Behaviors.receive { (context, message) =>
        message match
            case Message(IntegrateSystem.Integral(function, leftBorder, rightBorder, steps), replyTo) => {

                    val step: Double = (rightBorder - leftBorder) / steps
                    val result = Range(0, steps)
                                        .map(leftBorder + step / 2 + step * _)
                                        .map(function(_) * step)
                                        .reduce(_ + _)
                    context.log.info("Sending %f to \n%s".format(result, replyTo.toString()))
                    replyTo ! result
                    
                }
        Behaviors.same
    }

object IntegralSumActor:

    def apply(steps:Int):Behavior[Double] = 
        sumBehavior(0.0, steps)

    def sumBehavior(currentSum:Double, stepsLeft:Int):Behavior[Double] = Behaviors.setup { (context) =>
        if (currentSum >= stepsLeft) {
            context.log.info("%s:\n INTEGRAL EQUALS %f".format(context.self.toString(), currentSum))
            Behaviors.stopped
        }
        Behaviors.receive { (context, message) =>
            sumBehavior(currentSum + message, stepsLeft - 1)
        }
    }

object IntegrateSystem:
    case class Message(integral: Integral, splitCount: Int)
    case class Integral(function: Double=>Double, leftBorder: Double, rightBorder: Double, steps: Int)
    
    def apply():Behavior[Message] = Behaviors.setup { (context) => 
        val integrateActors = Seq(context.spawn(IntegrateActor(), s"integrateActor${System.nanoTime()}"),
                                  context.spawn(IntegrateActor(), s"integrateActor${System.nanoTime()}"),
                                  context.spawn(IntegrateActor(), s"integrateActor${System.nanoTime()}"),
                                  context.spawn(IntegrateActor(), s"integrateActor${System.nanoTime()}"))
        
        Behaviors.receive { (context, message) =>
            message match
                case Message(Integral(function, leftBorder, rightBorder, steps), splitCount) => {

                    val sumActor = context.spawn(IntegralSumActor(splitCount), s"sumActor${System.nanoTime()}")
                    
                    val step: Double = (rightBorder-leftBorder)/splitCount
                    for (i <- Range(0,splitCount)) {
                        integrateActors(i % integrateActors.length) 
                        ! 
                        IntegrateActor.Message(
                            Integral(
                                function, 
                                leftBorder + i * step, 
                                leftBorder + (i + 1) * step, 
                                steps
                            ), 
                            sumActor
                        )
                        
                    }
                }
            Behaviors.same
        }
    }



@main def main(): Unit = {
    val integrateSystem: ActorSystem[IntegrateSystem.Message] = ActorSystem(IntegrateSystem(), "integrateSystem")
    integrateSystem ! IntegrateSystem.Message(IntegrateSystem.Integral(x => cos(x),0,Pi,100),10)
    integrateSystem ! IntegrateSystem.Message(IntegrateSystem.Integral(x => x*0.5,0,2,100),10)
}