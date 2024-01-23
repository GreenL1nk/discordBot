package greenlink.economy.commands;

import global.BotMain;
import global.commands.SlashCommand;
import global.config.Config;
import greenlink.economy.EconomyManager;
import greenlink.economy.EconomyUser;
import greenlink.economy.jobs.Job;
import greenlink.economy.jobs.JobsManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;

/**
 * @author t.me/GreenL1nk
 * 23.01.2024
 */
public class WorkCommand extends SlashCommand {
    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if (member == null) return;
        if (event.getGuild() == null) return;
        if (!memberCanPerform(member, event)) return;

        EconomyUser economyUser = EconomyManager.getInstance().getEconomyUser(member.getIdLong());
        if (!economyUser.getUserCooldown().canWork()) {
            event.deferReply(true).setContent(String.format("**Будет доступна:** <t:<%d>:R>", economyUser.getUserCooldown().getWorkEpochTimeCD())).queue();
            return;
        }

        economyUser.getUserCooldown().setWorkLastTime(System.currentTimeMillis());
        Job randomJob = JobsManager.getInstance().getRandomJob();
        int value = randomJob.getRandomValue();

        economyUser.addCoins(value);

        //TODO сейв времени в бд

        String message = randomJob.message()
                .replaceAll("<user>", String.format("<@%d>", member.getIdLong()))
                .replaceAll("<value>", String.format("%d%s", value, Config.getInstance().getCoinIcon()));

        event.deferReply().addEmbeds(getEmbedBuilder(member, economyUser, message)).queue();
    }

    @NotNull
    private MessageEmbed getEmbedBuilder(Member member, EconomyUser economyUser, String message) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.decode("#a48ea2"));
        embedBuilder.setThumbnail(member.getUser().getAvatarUrl());

        embedBuilder.addField(
                Config.getInstance().getJobIcon() + " **Работа**",
                message,
                false
        );

        embedBuilder.addField(
                "**Следующая работа будет доступна:**",
                String.format("<t:%d:R>", economyUser.getUserCooldown().getWorkEpochTimeCD()),
                false
        );

        embedBuilder.setTimestamp(Instant.now());
        return embedBuilder.build();
    }

    @Override
    public String getName() {
        return "work";
    }

    @Override
    public String getDescription() {
        return "Выйти на работу";
    }
}
