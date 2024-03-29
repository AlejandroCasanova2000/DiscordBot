package DiscordPlayer;

import com.google.api.services.youtube.model.PlaylistItem;
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
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.voice.AudioProvider;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Main {
    private static boolean joined = false;
    private static final Map<String, Command> commands = new HashMap<String, Command>();
    static final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    static final AudioPlayer player = playerManager.createPlayer();
    static final TrackScheduler scheduler = new TrackScheduler(player);
    static final AudioProvider provider = new LavaPlayerAudioProvider(player);

    public static void main(final String[] args) throws IOException {
        InputStream input = new FileInputStream("application.properties");
        Properties properties = new Properties();
        properties.load(input);
        String discordToken = properties.getProperty("discordBotToken");
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
                        joined = true;
                        if (scheduler.getPlayer().getPlayingTrack() != null) {
                            scheduler.pause(true);
                        }
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
            if (!joined) {
                commands.get("join").execute(event);
            }
            final String content = event.getMessage().getContent().toString();
            final List<String> command = Arrays.asList(content.split(" " ));
            scheduler.setEvent(event);
            if (command.get(1).startsWith("https://www.youtube.com/watch?v=")) {
                System.out.println(command.get(1).toString());
                playerManager.loadItem(command.get(1), scheduler);
            } else if(command.get(1).startsWith("https://www.youtube.com/playlist?list=")) {
                String playlistId = command.get(1).split("list=")[1];
                List<PlaylistItem> playlist = YoutubeSearch.getVideosFromPlaylist(playlistId);
                scheduler.setFromPlaylist(true);
                event.getMessage().getChannel().block().createMessage("**Now Scheduling **" + command.get(1)).block();
                for(int i = 0; i < playlist.size(); i++) {
                    scheduler.setEvent(event);
                    playerManager.loadItem("https://www.youtube.com/watch?v=" +
                            playlist.get(i).getSnippet().getResourceId().getVideoId(), scheduler);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                scheduler.setFromPlaylist(false);
            } else if(command.get(1).startsWith("https://open.spotify.com/playlist/")) {
                String playlistURL = command.get(1).split("playlist/")[1];
                playlistURL = playlistURL.replace("?", "&&").split("&&")[0];
                try {
                    List<String> tracks = SpotifySearch.getPlaylistTrackNames(playlistURL);
                    event.getMessage().getChannel().block().createMessage("**Now Scheduling **" + command.get(1)).block();
                    scheduler.setFromPlaylist(true);
                    for (String track : tracks) {
                        String url = "https://www.youtube.com/watch?v=";
                        SearchResult result = YoutubeSearch.getVideoInfo(track);
                        playerManager.loadItem(url + result.getId().getVideoId(), scheduler);
                        Thread.sleep(10);
                    }
                    Thread.sleep(500);
                    scheduler.setFromPlaylist(false);
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (SpotifyWebApiException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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
            scheduler.setEvent(event);
            scheduler.skip();
        });
        commands.put("queue", event -> {
            scheduler.setEvent(event);
            scheduler.showQueue();
        });
        commands.put("clear", event -> {
            scheduler.setEvent(event);
            scheduler.clearQueue();
        });
        commands.put("ping", event -> event.getMessage().getChannel().block().createMessage("Pong!").block());
        commands.put("pause", event -> {
            scheduler.setEvent(event);
            scheduler.pause(false);
        });
        commands.put("loop", event -> {
            scheduler.setEvent(event);
            if (scheduler.isLoop()) {
                scheduler.setLoop(false);
                event.getMessage().getChannel().block().createMessage("**Continuing with queue...**").block();
            } else {
                event.getMessage().getChannel().block().createMessage("**Now playing in loop -> **" +
                        scheduler.getPlayer().getPlayingTrack().getInfo().title).block();
                scheduler.setLoop(true);
            }
        });
        commands.put("disconnect", event -> {
            VoiceChannel channel = event.getMember().orElse(null).getVoiceState().block().getChannel().block();
            scheduler.pause(true);
            joined = false;
            channel.sendDisconnectVoiceState().block();
        });
    }
}