package com.github.krrrr38

import sbt._

object StickerImage {
  type Path = String

  private[this] val defaultFile = "sbt-logo-white.png"

  private lazy val success: Path = getImagePath("success.png")
  private lazy val failure: Path = getImagePath("failure.png")

  def getStartImagePath = getImagePath("start.png")
  def getResultImagePath(isSuccess: Boolean) = if(isSuccess) success else failure

  private[this] def getImagePath(fileName: String): Path = {
    val customImage = file(System.getProperty("user.home")) / ".sbt" / "sticker" / "images" / fileName
    if (!customImage.exists()) {
      IO.createDirectory(customImage.getParentFile)
      IO.transfer(getClass.getClassLoader.getResourceAsStream(defaultFile), customImage)
    }
    customImage.getAbsolutePath
  }
}
