package greenlink.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author t.me/GreenL1nk
 * 12.01.2024
 */
public class PlayerManager {

    private static PlayerManager instance;

    private final Map<Long, GuildMusicManager> musicManagers;
    private final AudioPlayerManager audioPlayerManager;

    public PlayerManager() {
        this.musicManagers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
    }

    public GuildMusicManager getMusicManager(Guild guild) {
        return musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager);

            guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());

            return guildMusicManager;
        });
    }

    public void loadAndPlay(TextChannel channel, String trackUrl, boolean fromUrl) {
        GuildMusicManager musicManager = this.getMusicManager(channel.getGuild());

        this.audioPlayerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                musicManager.trackScheduler.queue(track);

                channel.sendMessage("Adding to queue: `")
                        .addContent(track.getInfo().title)
                        .addContent("` by `")
                        .addContent(track.getInfo().author)
                        .addContent("`")
                        .queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                List<AudioTrack> tracks = playlist.getTracks();
                if (fromUrl) {
                    channel.sendMessage("Adding to queue: `")
                            .addContent(String.valueOf(tracks.size()))
                            .addContent("` tracks from playlist `")
                            .addContent(playlist.getName())
                            .addContent("`")
                            .queue();

                    tracks.forEach(musicManager.trackScheduler::queue);
                }
                else {
                    AudioTrack track = tracks.get(0);
                    channel.sendMessage("Adding to queue: `")
                            .addContent(track.getInfo().title)
                            .addContent("` by `")
                            .addContent(track.getInfo().author)
                            .addContent("`")
                            .queue();
                    musicManager.trackScheduler.queue(track);
                }
            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException exception) {

            }
        });
    }

    public static PlayerManager getInstance() {
        if (instance == null) {
            instance = new PlayerManager();
        }
        return instance;
    }

}