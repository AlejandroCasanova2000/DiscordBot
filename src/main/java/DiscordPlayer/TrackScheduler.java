package DiscordPlayer;

import Queue.Queue;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.EventListener;

public class TrackScheduler implements AudioLoadResultHandler {
    private final AudioPlayer player;
    private Queue<AudioTrack> scheduledList;

    public TrackScheduler(final AudioPlayer player) {
        this.player = player;
        player.addListener(new AudioEventAdapter() {
            @Override
            public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
                skip();
            }
        });
        this.scheduledList = new Queue<AudioTrack>();
    }

    @Override
    public void trackLoaded(final AudioTrack track) {
        // LavaPlayer found an audio source for us to play
        if (player.getPlayingTrack() == null) {
            scheduledList.push(track);
            player.playTrack(scheduledList.pop());
        } else {
            System.out.println("added to queue");
            scheduledList.push(track);
        }
    }

    public void skip() {
        player.stopTrack();
        player.playTrack(scheduledList.pop());
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
}
