package greenlink.music;

import global.BotMain;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * @author t.me/GreenL1nk
 * 23.01.2024
 */
public class MusicBotListener extends ListenerAdapter {

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        if (event.getEntity().getIdLong() == BotMain.getInstance().getJda().getSelfUser().getIdLong()) {
            GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
            musicManager.trackScheduler.stop();
            musicManager.audioPlayer = null;
            musicManager.trackScheduler = null;
            musicManager.sendHandler = null;
            musicManager.executorService.shutdownNow();
            musicManager.executorService = null;
        }
    }
}
