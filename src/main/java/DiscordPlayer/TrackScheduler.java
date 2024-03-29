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
import discord4j.core.object.entity.channel.MessageChannel;

public class TrackScheduler implements AudioLoadResultHandler {
    private final AudioPlayer player;
    private boolean loop;
    private Queue<AudioTrack> scheduledList;
    private MessageCreateEvent event;
    private boolean isFromPlaylist;
    private MessageChannel channel;
    private AudioTrack lastPlayed;

    public TrackScheduler(final AudioPlayer player) {
        this.player = player;
        player.addListener(new AudioEventAdapter() {
            @Override
            public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
                trackLoaded(null);
            }
        });
        this.scheduledList = new Queue<AudioTrack>();
        this.isFromPlaylist = false;
        this.loop = false;
    }

    @Override
    public void trackLoaded(final AudioTrack track) {
        System.out.println(track);
        // LavaPlayer found an audio source for us to play
        if (player.getPlayingTrack() == null) {
            System.out.println("entramos");
            if (track != null) scheduledList.push(track);
            AudioTrack nowPlaying;
            if (!isLoop()) {
                nowPlaying = scheduledList.pop();
            } else {
                nowPlaying = lastPlayed;
            }
            if (nowPlaying != null) {
                sendMessage("**Now Playing -> **" + nowPlaying.getInfo().title);
                System.out.println("reproducimos");
                lastPlayed = nowPlaying.makeClone();
                player.playTrack(nowPlaying);
            } else {
                System.out.println("no reproducimos");
            }
        } else {
            System.out.println("added to queue");
            if (!isFromPlaylist()) {
                event.getMessage().getChannel().block()
                        .createMessage("**" + track.getInfo().title + " --> Addeded to Queue in "
                                + (scheduledList.getSize() + 1) + "º position")
                        .block();
            }
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

    public boolean isFromPlaylist() {
        return isFromPlaylist;
    }

    public void setFromPlaylist(boolean fromPlaylist) {
        isFromPlaylist = fromPlaylist;
    }

    public MessageCreateEvent getEvent() {
        return event;
    }

    public void setEvent(MessageCreateEvent event) {
        this.event = event;
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    public boolean isLoop() {
        return loop;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public void skip() {
        if (scheduledList.getSize() > 0 || isLoop()) player.stopTrack();
        else {
            sendMessage("**No Songs in Queue**");
        }
    }

    private void sendMessage(String message) {
        /*
         * Sometimes, for one reason the getChannel().block() method gets stucked,
         * for solving this, we have the channel attb, if the block method gets stucked
         * (IllegalStateException), we send the message by the channel.
         * If it doesn't get stucked, we update the attb channel.
         */
        try {
            event.getMessage().getChannel().block()
                    .createMessage(message)
                    .block();
            channel = event.getMessage().getChannel().block();
        } catch (IllegalStateException e) {
            channel.createMessage(message).subscribe();
        }
    }

    public void showQueue() {
        if (scheduledList.getSize() == 0) {
            sendMessage("**There are not Items in Queue**");
        } else {
            StringBuilder sb = new StringBuilder();
            Node<AudioTrack> puntero = scheduledList.getFirstNode();
            sb.append("**Items left in Queue**\n");
            for (int i = 0; i < scheduledList.getSize(); i++) {
                sb.append((i + 1) + ")  ---> " + puntero.getContent().getInfo().title + "\n");
                puntero = puntero.getNext();
            }
            sendMessage(sb.toString());
        }
    }

    public void clearQueue() {
        scheduledList = new Queue<AudioTrack>();
        sendMessage("**Queue cleared!!!**");
    }

    public void pause(boolean isForDisconnect) {
        if (!player.isPaused()) {
            if (!isForDisconnect) sendMessage("**Paused**");
            player.setPaused(true);
        } else {
            if (!isForDisconnect) sendMessage("**Resumed**");
            player.setPaused(false);
        }
    }
}
