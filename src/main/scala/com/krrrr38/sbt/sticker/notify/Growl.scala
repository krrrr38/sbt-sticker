package com.krrrr38.sbt.sticker.notify

import scala.language.postfixOps
import scala.sys.process.Process

object Growl {
  private val command = "growlnotify"

  private[this] lazy val binOpt = scala.util.Try(Process(Seq("which", command)).!!.trim).toOption

  def apply(message: String) = new Growl().message(message)

  def exec(options: Map[String, Any], flags: Set[String]): Unit =
    binOpt.fold(error) { bin: String =>
      val arguments = options.flatMap { case (option, value) => Seq(s"-$option", value.toString) }.toSeq ++ flags.map("-" + _)
      Process(bin, arguments.toSeq).! == 0
    }

  private[this] def error =
    Console.err.println(
      """
        |growlnotify is not installed
        |please install from here: http://growl.info/downloads#generaldownloads
      """.stripMargin
    )
}

/**
 * Usage: growlnotify [-hsvw] [-i ext] [-I filepath] [--image filepath]
 * [-a appname] [-p priority] [-H host] [-P password]
 * [-n name] [-N notename] [-m message] [-t] [--url url]
 * [title]
 * Options:
 * -h,--help       Display this help
 * -v,--version    Display version number
 * -n,--name       Set the name of the application that sends the notification
 *                 [Default: growlnotify]
 * -N --noteName   Set the note name of the notification that GrowlNotify sends
 * -s,--sticky     Make the notification sticky
 * -a,--appIcon    Specify an application name to take the icon from
 * -i,--icon       Specify a file type or extension to look up for the notification icon
 * -I,--iconpath   Specify a file whose icon will be the notification icon
 *    --image      Specify an image file to be used for the notification icon
 * -m,--message    Sets the message to be used instead of using stdin
 *                 Passing - as the argument means read from stdin
 * -p,--priority   Specify an int or named key (default is 0)
 * -d,--identifier Specify a notification identifier (used for coalescing)
 * -H,--host       Specify a hostname or IP address to which to send a remote notification.
 * -P,--password   Password used for remote notifications.
 * -w,--wait       Wait until the notification has been dismissed.
 * --url           Notification click will result in the URL being opened
 *
 * Display a notification using the title given on the command-line and the
 * message given in the standard input.
 *
 * Priority can be one of the following named keys: Very Low, Moderate, Normal,
 * High, Emergency. It can also be an int between -2 and 2.
 *
 * To be compatible with gNotify the following switch is accepted:
 * -t,--title      Does nothing. Any text following will be treated as the
 * title because that's the default argument behaviour
 *
 * @param options
 */
private[notify] class Growl(options: Map[String, Any] = Map.empty, flags: Set[String] = Set.empty) {
  private[this] def addOption(option: String)(value: Any) = new Growl(options + (option -> value), flags)
  private[this] def removeOption(option: String) = new Growl(options - option, flags)
  private[this] def addFlag(flag: String) = new Growl(options, flags + flag)
  private[this] def removeFlag(flag: String) = new Growl(options, flags - flag)

  def exec = Growl.exec(options, flags)

  def message(message: String) = addOption("m")(message)

  def name(name: String) = addOption("n")(name)
  def noteName(noteName: String) = addOption("N")(noteName)
  def sticker(flag: Boolean = true) = if(flag) addFlag("s") else removeFlag("s")
  def appIcon(icon: String) = addOption("a")(icon)
  def icon(icon: String) = addOption("i")(icon)
  def iconpath(path: String) = addOption("-iconpath")(path)
  def image(path: String) = addOption("-image")(path)
  def priority(priority: Int = 0) = addOption("p")(priority)
  def identifier(identifier: String) = addOption("d")(identifier)
  def host(host: String) = addOption("H")(host)
  def password(password: String) = addOption("P")(password)
  def wait(flag: Boolean = true) = if(flag) addFlag("w") else removeFlag("w")
  def url(url: String) = addOption("-url")(url)
  def title(title: String) = addOption("t")(title)
}
