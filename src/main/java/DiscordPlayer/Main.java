package DiscordPlayer;

import com.google.api.services.youtube.model.SearchResult;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.voice.AudioProvider;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Main {
    private static final Map<String, Command> commands = new HashMap<String, Command>();
    static final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    static final AudioPlayer player = playerManager.createPlayer();
    static final TrackScheduler scheduler = new TrackScheduler(player);
    static final AudioProvider provider = new LavaPlayerAudioProvider(player);

    public static void main(final String[] args) throws IOException {
        InputStream input = new FileInputStream("application.properties");
        Properties properties = new Properties();
        properties.load(input);
        String discordToken = properties.getProperty("discordToken");
        String prefix = properties.getProperty("prefix");
        // Creates AudioPlayer instances and translates URLs to AudioTrack instances

// This is an optimization strategy that Discord4J can utilize.
// It is not important to understand
        playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);

// Allow playerManager to parse remote sources like YouTube links
        AudioSourceManagers.registerRemoteSources(playerManager);

// Create an AudioPlayer so Discord4J can receive audio data

// We will be creating LavaPlayerAudioProvider in the next step
        commands.put("join", event -> {
            final Member member = event.getMember().orElse(null);
            if (member != null) {
                final VoiceState voiceState = member.getVoiceState().block();
                if (voiceState != null) {
                    final VoiceChannel channel = voiceState.getChannel().block();
                    if (channel != null) {
                        // join returns a VoiceConnection which would be required if we were
                        // adding disconnection features, but for now we are just ignoring it.
                        channel.join(spec -> spec.setProvider(provider)).block();
                    }
                }
            }
        });

        final GatewayDiscordClient client = DiscordClientBuilder.create(discordToken).build().login().block();
        client.getEventDispatcher().on(MessageCreateEvent.class)
                // subscribe is like block, in that it will *request* for action
                // to be done, but instead of blocking the thread, waiting for it
                // to finish, it will just execute the results asynchronously.
                .subscribe(event -> {
                    // 3.1 Message.getContent() is a String
                    final String content = event.getMessage().getContent();
                    String[] contentSplitted = content.split(" ");
                    for (final Map.Entry<String, Command> entry : commands.entrySet()) {
                        // We will be using ! as our "prefix" to any command in the system.
                        if (contentSplitted[0].startsWith(prefix + entry.getKey())) {
                            System.out.println(event.getMessage().getContent());
                            try {
                                entry.getValue().execute(event);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                });
        client.onDisconnect().block();
    }

    static {
        commands.put("play", event -> {
            final String content = event.getMessage().getContent().toString();
            final List<String> command = Arrays.asList(content.split(" " ));
            if (command.get(1).startsWith("https://www.youtube.com/watch?v=")) {
                System.out.println(command.get(1).toString());
                scheduler.setEvent(event);
                playerManager.loadItem(command.get(1), scheduler);
            } else {
                String url = "https://www.youtube.com/watch?v=";
                StringBuilder sb = new StringBuilder();
                for(int i = 0; i < command.size(); i++) {
                    if(i != 0) {
                        sb.append(command.get(i) + " ");
                    }
                }
                SearchResult video = YoutubeSearch.getVideoInfo(sb.toString());
                scheduler.setEvent(event);
                playerManager.loadItem(url + video.getId().getVideoId(), scheduler);
            }
        });
        commands.put("skip", event -> {
            scheduler.skip();
        });
        commands.put("queue", event -> {
            scheduler.showQueue();
        });
        commands.put("clear", event -> {
            scheduler.clearQueue();
        });
        commands.put("ping", event -> event.getMessage().getChannel().block().createMessage("Pong!").block());
        commands.put("pause", event -> {
            scheduler.setEvent(event);
            scheduler.pause();
        });
    }
}