# MusicDiscordBot4j

This project is a bot for Discord, written using Java, the [Discord4J](https://github.com/austinv11/Discord4J) library, and the [Lavaplayer](https://github.com/sedmelluq/lavaplayer) library for sound processing. It is a fully-fledged bot with the ability to manage playlists, get music from URLs, and perform other necessary functions for a clean and enjoyable user experience.

## Description

This Bot is designed package in a .exe file, to start one Discord bot. 

## Installation
Firs Download the code and add it to your Java IDE. Next you have to create a Discord application in https://discord.com/developers/applications, and get
a youtube API token for YouTube data in https://console.cloud.google.com/apis/library/youtube.googleapis.com.
Next step is creating an aplication.properties file in the root folder of the project. The properties file needs to have the following properties:
* discordToken=yourDiscordToken
* youtubeToken=YoutGoogleApiKeys
* prefix=desiredPrefix(example: prefix=!)

## Commands

This Bot contains some commands, most of which should be quite intuitive to the user.

All commands begin with a prefix (in properties file), which will not be shown with all the following commands, as it can be configured by users.

* `join` - This command is for making the bot enter in your voice channel (REMEMBER JOIN YOUR BOT TO THE CHANNEl) 

* `play` - Play a song or adds it to queue. Command can be invoked by play [url] or play [song name] Ex: !play https://wwww.youtube.com... or !play dont stop me now

* `queue` - Shows the list of songs in queue

* `pause` - Pause the song or resume it if its paused already

* `ping` - This command is for testing the bot. When you type this command, the bot answer. !ping  BOT: Pong!

* `skip`* - If a song is playing, the bot will skip it and play the next song in the queue.

* `clear` - Clear the queue
    
