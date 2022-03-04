# MusicDiscordBot4j

This project is a bot for Discord, written using Java, the [Discord4J](https://github.com/austinv11/Discord4J) library, and the [Lavaplayer](https://github.com/sedmelluq/lavaplayer) library for sound processing. It is a fully-fledged bot with the ability to manage playlists, get music from URLs, and perform other necessary functions for a clean and enjoyable user experience.

## Description

This Bot is designed package in a .exe file, to start one Discord bot. 

## Installation
Firs Download the code and add it to your Java IDE. Next you have to create a Discord application in https://discord.com/developers/applications, and get a youtube API token for YouTube data in https://console.cloud.google.com/apis/dashboard (The mencioned YT API is YouTube Data API v3). Also you have to create a Spotify app for play Spotify songs at https://developer.spotify.com/dashboard/applications. The next step is creating an aplication.properties file in the root folder of the project. 
The properties file needs to have the following properties:
* discordToken=yourDiscordBotToken
* youtubeToken=YourGoogleApiKeys
* prefix=desiredPrefix(example: prefix=!)
* spotifyID=clientIDofSpotifyApp
* spotifySecret=clientSecretOfSpotifyApp

Don't forget to invite the bot to your channel, you can do this in Discord Developer Portal -> Your Application -> Oauth2

## Commands

This Bot contains some commands, most of which should be quite intuitive to the user.

All commands begin with a prefix (in properties file), which will not be shown with all the following commands, as it can be configured by users.

* `play` - Play a song or adds it to queue. Command can be invoked by play [url] or play [song name] Ex: !play https://wwww.youtube.com... or !play dont stop me now.

* `play [YT Playlist]` -  For playing YT playlist, simply type !play https://www.youtube.com/playlist?list=... and the bot will add the entire playlist to the queue

* `play [Spotify Playlist]` - For playing Spotify playlist, invoke play command like !play https://open.spotify.com/playlist/... and bot will schedule the songs into queue

* `queue` - Shows the list of songs in queue

* `pause` - Pause the song or resume it if it's paused already

* `ping` - This command is for testing the bot. When you type this command, the bot answer. !ping  BOT: Pong!

* `skip` - If a song is playing, the bot will skip it and play the next song in the queue.

* `clear` - Clear the queue

* `loop` - The bot starts playing in loop the current song (skip command will play the same song). If you invoke loop and it was activated, it gets false and continue with queue.

* `disconnect` - The bot leaves the voice channel (it pauses the song)

* `join` - This command is for making the bot enter in your voice channel once it was disconnected by the above command (and resumes the last song)

## Creating .exe File to execute the bot (May not Work in the release)
For creating the .exe file (make sure the bot works fine by testing first in your IDE), you have to do a Maven install command. In IntelliJ for example, you have to go to the right contextual Maven menu, go to lifeCycle, intall. This step will create a target folder with the bot.exe file.
For stopping the bot, you have to go to your task manager and kill Java Platform SE Binary process.
If this feature doesn't work, just wait for this to be solved in following releases, you can still using the bot by executing it in your IDE.

## Coming Soon...
* If you want more features pls tell me and help to make this bot better!
    
