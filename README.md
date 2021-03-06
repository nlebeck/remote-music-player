# Remote Music Player

This program is a music player that can be controlled remotely via a web
interface. My use case is very simple: I have a desktop PC hooked up to a TV
and speakers, and I'd like to be able to control what plays on the speakers
using my laptop or cell phone. There is probably some existing program that I
could use to accomplish this task, but I thought it would be a fun little
programming project.

## How to Use

The program is a Maven project in the `remote-music-player` subdirectory. You
can launch the music player by running the following command from that
directory:

    mvn clean javafx:run

In order to run the music player and play music successfully, you'll need to
edit `config.xml` to change the `baseDir` element to contain the path of the
top-level directory holding your music files.

The music player provides a web interface that you can use to remotely control
playback. It uses the ports specified in `config.xml`. To open the web
interface, use a web browser to connect to the HTTP port listed in the config
file. For example, if the HTTP port is set to `8080` on a computer with IP
address `192.168.0.1`, then you can access the web interface by navigating a
web browser to `http://192.168.0.1:8080`.

## Local Music Browser (work-in-progress)

I have started working on an interface for controlling the music player from
the local computer using an Xbox controller. With this feature, the music
player could be controlled either from another device or from the computer
itself. I'm using the [JXInput][JXInput] library to read controller input. My
plan is to implement this feature gradually without breaking any existing
functionality.  When I'm done implementing it, I'll update this README to
reflect its presence.

## Using JavaFX with Maven

The JavaFX documentation provides some
[quick-start instructions][JavaFX-Maven-quickstart] for using JavaFX with Maven
but doesn't explain the example in depth, so it took me a little while to
figure out which bits of the example are necessary. Below are some notes about
what I discovered.

It looks like in order to have your program successfully reference JavaFX
classes, your `pom.xml` file needs to have the `javafx-maven-plugin` plugin
with a `mainClass` configuration element set to your program's main class, and
you need to launch the program using `mvn javafx:run`. If you just try to run
`mvn package` and then run the program with `java`, you'll get a
`NoClassDefFoundError`.

Furthermore, in order to use the JavaFX `MediaPlayer` class to play media, you
need to create and use it inside of a class that extends JavaFX's `Application`
rather than an ordinary command-line program with a `main()` method.
Attempting the latter will give you an `IllegalStateException` saying "Toolkit
not initialized." However, you do not have to actually create and show a
`Scene` inside of the `Application`'s `start()` method in order to play media.

I would like to make an executable JAR file, but I can't figure out how to make
one that works properly with JavaFX. It seems like there is a way to do so with
a modular JavaFX project and jlink, but I can't get it to work. For now, I've
created a batch script that just calls `mvn javafx:run` to launch the program
on Windows.

## Project history

Originally, I wanted to write an app that would capture audio from one computer
and stream it to another computer (so that I could capture audio on my laptop
and stream it to my desktop). However, after reading these StackOverflow posts,
I began to suspect that capturing audio from the sound card is not possible, at
least in Java and using my current hardware:
* https://stackoverflow.com/q/2869898
* https://stackoverflow.com/q/44604880

As a result, I switched gears to make a program that plays locally stored mp3
files, with file selection and playback controlled by a web interface. This
approach has the downside that I can't play songs from Apple Music or YouTube,
but it has the advantage of supporting a wider range of client devices, like a
smartphone or tablet.

## Helpful links

Playing sound files with Java:
* https://stackoverflow.com/q/6045384
* https://openjfx.io/openjfx-docs/

Running a Java HTTP server with embedded Jetty:
* https://www.eclipse.org/jetty/documentation/current/advanced-embedding.html
* https://www.eclipse.org/jetty/documentation/current/maven-and-jetty.html

Using WebSockets:
* https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API
* https://github.com/TooTallNate/Java-WebSocket
* https://stackoverflow.com/questions/37857272/invalidstateerror-on-websocket-object

Using JSON:
* https://developer.mozilla.org/en-US/docs/web/javascript/reference/global_objects/json
* https://github.com/google/gson

Styling a web page with CSS:
* https://developer.mozilla.org/en-US/docs/Learn/CSS

Running code on the JavaFX Application Thread:
* https://noblecodemonkeys.com/switching-to-the-gui-thread-in-javafx/

Capturing device audio (old project plan):
* https://stackoverflow.com/q/31738072
* https://stackoverflow.com/q/28122097
* https://docs.oracle.com/javase/tutorial/sound/sampled-overview.html

[JavaFX-Maven-quickstart]: https://openjfx.io/openjfx-docs/#maven
[JXInput]: https://github.com/StrikerX3/JXInput
