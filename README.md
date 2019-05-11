# AudioStreamer
I am planning to write a simple app to capture the audio from one computer and
stream it to another computer to be played. My use case is very simple: I have
a desktop PC hooked up to speakers and a laptop on the other side of the room,
and I'd like to be able to control what plays on the speakers using my laptop.
There is probably some existing program that I could use to accomplish this
task, but I thought it would be a fun little programming project.

## A problem

After reading these StackOverflow posts, I am starting to suspect that
capturing audio from the sound card is not possible, at least in Java and using
my current hardware:
* https://stackoverflow.com/q/2869898
* https://stackoverflow.com/q/44604880

For now, I think I'm going to make the program just play mp3 files and
temporarily copy the files themselves over to the remote machine. This approach
has the downside that I can't play songs from Apple Music or YouTube, but it's
better than nothing.

## Potentially helpful links
* https://stackoverflow.com/q/31738072
* https://stackoverflow.com/q/28122097
* https://docs.oracle.com/javase/tutorial/sound/sampled-overview.html
* https://stackoverflow.com/q/6045384
* https://openjfx.io/openjfx-docs/