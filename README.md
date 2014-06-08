TetrixGame
===

A tetrix game written in early years when I'm learning Java. Purly by java and swing. Features include:

* Multiplayer networking
* AI opponent
* Game recording
* Customizable n*m size tetrix block
* Variable motions (well... I do use sin&cos math to calculate tetrix moves)

How to Play
---

To launch the game:
```
cd jar
java -jar tetrixgame.jar
```

This is a graphic game but the enter screen is command line. Well, command line is not simple yet. To see full guide
```
<command> help
```

The game is network based, first you need to host a room
'''
<command> host 5
'''
The number can be 1~17. It's the room size.

Add some AI opponents
```
<command> add bot 4
```
AI opponents, which I call bots, will automatically search for the room you are hosting and join.

You can see what bots are doing
```
<command> show bots
```

A friend of you can join the game by
```
<command> join <ip> <port>
```

He/She can also see all the rooms are being hosted
```
<comands> show hosts
```

You will be able to see who are joining you
```
<command> show clients
```

And kick him/her out if you like
```
<command> kick <client_id>
```

Finally you can start the game
```
<command> start
```

Or if you want to try something more excited
```
<command> start 2 3 2
```
It means
```
<command> start [<speed> [<block_row> <block_column>]]
```

You can even record the game
```
<command> start > recording.file
```

And play the recording by
```
<command> demo recording.file
```

If you are tired, exit the game by
```
<command> exit
```

The game doesn't come along ith music and sound. The folder `audio` is just a place holder. Replace the `.wav/.mid` files with real ones but follow the same audio format. Put `audio/` foler and `.jar` in the same folder, and it'll work.

Have Fun ~!