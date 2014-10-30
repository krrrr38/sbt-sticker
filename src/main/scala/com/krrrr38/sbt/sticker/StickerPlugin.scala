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

      notify(trigger) {
        val nextState = Command.process(trigger, state)
        runNextCommand(nextState, isSuccess = true)
      }
    }

  implicit class ContinuousState(state: State) {
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

    // same process in state.fail
    def hasSucceeded: Boolean = state.remainingCommands.isEmpty || state.onFailure.isDefined
  }

  private[this] def notify(trigger: String)(commands: => (State, Boolean)): State = {
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
