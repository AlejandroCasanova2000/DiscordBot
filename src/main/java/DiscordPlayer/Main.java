package DiscordPlayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.voice.AudioProvider;
import discord4j.voice.VoiceConnection;
import reactor.core.publisher.Mono;

import java.sql.SQLOutput;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    private static final Map<String, Command> commands = new HashMap<String, Command>();

    public static void main(final String[] args) {
        // Creates AudioPlayer instances and translates URLs to AudioTrack instances
        final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

// This is an optimization strategy that Discord4J can utilize.
// It is not important to understand
        playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);

// Allow playerManager to parse remote sources like YouTube links
        AudioSourceManagers.registerRemoteSources(playerManager);

// Create an AudioPlayer so Discord4J can receive audio data
        final AudioPlayer player = playerManager.createPlayer();

// We will be creating LavaPlayerAudioProvider in the next step
        AudioProvider provider = new LavaPlayerAudioProvider(player);
        commands.put("join", event -> {
            final Member member = event.getMember().orElse(null);
            if (member != null) {
                final VoiceState voiceState = member.getVoiceState().block();
                if (voiceState != null) {
                    final VoiceChannel channel = voiceState.getChannel().block();
                    if (channel != null) {
                        // join returns a VoiceConnection which would be required if we were
                        // adding disconnection features, but for now we are just ignoring it.
                        channel.join(spec ->  spec.setProvider(provider)).block();
                    }
                }
            }
        });
        final TrackScheduler scheduler = new TrackScheduler(player);
        commands.put("play", event -> {
            System.out.println("playin " + event.getMessage().getContent());
            final String content = event.getMessage().getContent().toString();
            final List<String> command = Arrays.asList(content.split(" "));
            System.out.println(command.get(1).toString());
            playerManager.loadItem(command.get(1), scheduler);

        });
        commands.put("skip", event -> {
            scheduler.skip();
        });
        final GatewayDiscordClient client = DiscordClientBuilder.create("ODkzNTI4NDk2ODQyODkxMjY0.YVcxQg.EXtrzU6MjcsFVVyhv6ZUOZNMv9E").build().login().block();
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
                        if (contentSplitted[0].startsWith('!' + entry.getKey())) {
                            System.out.println(event.getMessage().getContent());
                            entry.getValue().execute(event);
                            break;
                        }
                    }
                });
        client.onDisconnect().block();

    }

    static {
        commands.put("ping", event -> event.getMessage().getChannel().block().createMessage("Pong!").block());
    }
}