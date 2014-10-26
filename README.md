## sbt-sticker

Show a notification as a sticker while command running.

### About

This is a sbt plugin. While sbt command runnning, your notification is fixed like a sticker. When change the sticker, the command would be finished and it will be disappear soon automatically.

To show a sticker, just add `sticker` before basic commands in sbt console.

![](./images/howto.gif)

### Requirement

#### On Mac

Download [GrowlNotify](http://growl.info/downloads#generaldownloads).

#### Others

Not supported yet.

### Customize Images

sbt-sticker uses following images. (If not exist, sbt-sticker generate default files.)

- When command started
	- `~/.sbt/sticker/images/start.png`
- When command successed
	- `~/.sbt/sticker/images/success.png`
- When command failed
	- `~/.sbt/sticker/images/failure.png`

The default image file is from [http://www.scala-sbt.org/](http://www.scala-sbt.org/)
