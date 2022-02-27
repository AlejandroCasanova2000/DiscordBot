package DiscordPlayer;

import Queue.Queue;
import Queue.Node;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class TrackScheduler implements AudioLoadResultHandler {
    private final AudioPlayer player;
    private Queue<AudioTrack> scheduledList;
    private MessageCreateEvent event;

    public TrackScheduler(final AudioPlayer player) {
        this.player = player;
        player.addListener(new AudioEventAdapter() {
            @Override
            public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
                trackLoaded(null);
            }
        });
        this.scheduledList = new Queue<AudioTrack>();
    }

    @Override
    public void trackLoaded(final AudioTrack track) {
        System.out.println(track);
        // LavaPlayer found an audio source for us to play
        if (player.getPlayingTrack() == null) {
            if(track != null) scheduledList.push(track);
            AudioTrack nowPlaying = scheduledList.pop();
            event.getMessage().getChannel().block()
                    .createMessage("**Now Playing -> **" + nowPlaying.getInfo().title)
                    .block();
            if(nowPlaying != null) player.playTrack(nowPlaying);
        } else {
            System.out.println("added to queue");
            event.getMessage().getChannel().block()
                    .createMessage("**" + track.getInfo().title + "--> Addeded to Queue in "
                            + (scheduledList.getSize() + 1) + "º position")
                    .block();
            scheduledList.push(track);
        }
    }

    @Override
    public void playlistLoaded(final AudioPlaylist playlist) {
        // LavaPlayer found multiple AudioTracks from some playlist
    }

    @Override
    public void noMatches() {
        // LavaPlayer did not find any audio to extract
    }

    @Override
    public void loadFailed(final FriendlyException exception) {
        // LavaPlayer could not parse an audio source for some reason
    }

    public MessageCreateEvent getEvent() {
        return event;
    }

    public void setEvent(MessageCreateEvent event) {
        this.event = event;
    }

    public void skip() {
        player.stopTrack();
    }

    public void showQueue() {
        if(scheduledList.getSize() == 0) {
            event.getMessage().getChannel().block().createMessage("**There are not Items in Queue**").block();
        } else {
            StringBuilder sb = new StringBuilder();
            Node<AudioTrack> puntero = scheduledList.getFirstNode();
            sb.append("**Items left in Queue**\n");
            for(int i = 0; i < scheduledList.getSize(); i++) {
                sb.append((i + 1) + ")  ---> " + puntero.getContent().getInfo().title + "\n");
                puntero = puntero.getNext();
            }
            event.getMessage().getChannel().block().createMessage(sb.toString()).block();
        }
    }

    public void clearQueue() {
        scheduledList = new Queue<AudioTrack>();
    }

    public void pause() {
        if (!player.isPaused()) {
            event.getMessage().getChannel().block().createMessage("**Paused**").block();
            player.setPaused(true);
        }
        else {
            event.getMessage().getChannel().block().createMessage("**Resumed**").block();
            player.setPaused(false);
        }
    }
}
