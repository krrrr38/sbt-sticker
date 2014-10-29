package com.krrrr38.sbt.sticker

import sbt._
import Keys._
import com.krrrr38.sbt.sticker.notify.Growl

object StickerPlugin extends AutoPlugin {
  override def globalSettings = Seq(commands += sticker)
  override def trigger = allRequirements

  private[this] val stickerName = "sbt-sticker"

  private[this] val stickerBriefHelp = ("sticker <command>", "sticker trigger")
  private[this] val stickerDetail = "Show notification as a sticker while a command running."

  lazy val sticker =
    Command("sticker", stickerBriefHelp, stickerDetail)(BasicCommands.otherCommandParser) { (state, args) =>
      var isSuccess = false
      val startTime = System.currentTimeMillis()
      val identifier = startTime + args
      try {
        Growl("at %tT" format new java.util.Date(startTime))
          .name(stickerName)
          .title(args)
          .identifier(identifier)
          .sticker(flag = true)
          .image(StickerImage.getStartImagePath)
          .exec
        val result = Command.process(args, state)
        result.onFailure.foreach(_ => isSuccess = true)
        result
      } finally {
        val timeSec = (System.currentTimeMillis() - startTime) / 1000
        val message =
          if(isSuccess) {
            s"[SUCCESS] Total time: $timeSec s"
          } else {
            s"[FAILURE] Total time: $timeSec s"
          }
        Growl(message)
          .name(stickerName)
          .title(args)
          .identifier(identifier)
          .image(StickerImage.getResultImagePath(isSuccess))
          .exec
      }
    }
}
