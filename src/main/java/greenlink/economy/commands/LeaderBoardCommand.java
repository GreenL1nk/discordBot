package greenlink.economy.commands;

import global.commands.SlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * @author t.me/GreenL1nk
 * 23.01.2024
 */
public class LeaderBoardCommand extends SlashCommand {
    @Override
    public void execute(SlashCommandInteractionEvent event) {

    }

    @Override
    public String getName() {
        return "leaderboard";
    }

    @Override
    public String getDescription() {
        return "топ пользователей";
    }
}
