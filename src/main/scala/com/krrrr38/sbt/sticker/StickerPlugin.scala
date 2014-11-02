package com.krrrr38.sbt.sticker

import sbt._
import Keys._
import com.krrrr38.sbt.sticker.notify.Growl

import scala.util.{Try, Success, Failure}

object StickerPlugin extends AutoPlugin {
  override def globalSettings = Seq(commands += sticker)
  override def trigger = allRequirements

  private[this] val stickerName = "sbt-sticker"

  private[this] val stickerDetail = "Show notification as a sticker while a command running."
  private[this] val stickerBriefHelp = ("sticker <command>", stickerDetail)

  lazy val sticker =
    Command("sticker", stickerBriefHelp, stickerDetail)(BasicCommands.otherCommandParser) { (state, trigger) =>
      def runNextCommand(state: State, isSuccess: Boolean): (State, Boolean) = {
        if (state.hasNext) {
          runNextCommand(state.runNext, isSuccess && state.hasSucceeded)
        } else {
          (state, isSuccess && state.hasSucceeded)
        }
      }

      notify(state, trigger) {
        val nextState = Command.process(trigger, state)
        if (nextState.isContinuous)
          (nextState, nextState.hasSucceeded)
        else
          runNextCommand(nextState, isSuccess = true)
      }
    }

  implicit class FollowingState(state: State) {
    def hasNext: Boolean = state.remainingCommands match {
      // if command is executed on login shell, nothing remain.
      // if command is executed on sbt shell, only "shell" command remain.
      case Nil | "shell" :: _ => false
      case _ => true
    }

    def runNext: State = {
      val nextState = Command.process(state.remainingCommands.head, state)
      nextState.copy(remainingCommands = state.remainingCommands.tail)
    }

    // XXX same process in state.fail
    def hasSucceeded: Boolean = {
      if(state.remainingCommands.isEmpty) {
        state.next match {
          case n: sbt.State.Return => n.result match {
            case ex: sbt.Exit if ex.code != 0 => false
            case _ => true
          }
          case _ => true
        }
      } else {
        state.remainingCommands.head == State.FailureWall || state.onFailure.isDefined
      }
    }

    def isContinuous: Boolean = state.get(Watched.ContinuousState).isDefined
  }

  private[this] def notify(state: State, trigger: String)(commands: => (State, Boolean)): State = {
    val startTime = System.currentTimeMillis()
    val identifier = startTime + trigger
    Growl("at %tT" format new java.util.Date(startTime))
      .name(stickerName)
      .title(trigger)
      .identifier(identifier)
      .sticker(flag = true)
      .image(StickerImage.getStartImagePath)
      .exec

    def close(isSuccess: Boolean): Unit = {
      val timeSec = (System.currentTimeMillis() - startTime) / 1000
      val message =
        if (isSuccess) {
          s"[SUCCESS] Total time: $timeSec s"
        } else {
          s"[FAILURE] Total time: $timeSec s"
        }
      Growl(message)
        .name(stickerName)
        .title(trigger)
        .identifier(identifier)
        .image(StickerImage.getResultImagePath(isSuccess))
        .exec
    }

    Try(commands) match {
      case Success((nextState, isSuccess)) =>
        close(isSuccess)
        nextState
      case Failure(e) =>
        e.printStackTrace()
        close(isSuccess = false)
        state.fail
    }
  }
}
