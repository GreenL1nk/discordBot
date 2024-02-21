package greenlink.economy;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import global.BotMain;
import greenlink.databse.DatabaseConnector;
import greenlink.economy.leaderboards.LeaderBoardType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author t.me/GreenL1nk
 * 19.01.2024
 */
public class EconomyManager {
    private static EconomyManager instance;
    Cache<Long, EconomyUser> cache;

    private EconomyManager() {
        cache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .removalListener(removalListener)
                .build();
    }

    @Nullable
    public EconomyUser getEconomyUser(User user) {
        if (user.isBot()) {
            BotMain.logger.debug("Был получен бот, скипаем economyUser");
            return null;
        }
        try {
            return cache.get(user.getIdLong(), () -> DatabaseConnector.getInstance().getEconomyUser(user.getIdLong()));
        } catch (ExecutionException e) {
            BotMain.logger.warn(e.getMessage());
            BotMain.logger.warn("Загрузка пользователя вызвало ошибку, создаём нового для " + user.getIdLong());
            return DatabaseConnector.getInstance().getEconomyUser(user.getIdLong());
        }
    }

    public Collection<EconomyUser> getEconomyUsers() {
        return cache.asMap().values();
    }

    RemovalListener<Long, EconomyUser> removalListener = notification -> {
        BotMain.logger.debug("User " + notification.getKey() + " removed from cache");
    };

    public ArrayList<EconomyUser> getUserTop(LeaderBoardType leaderBoardType) throws SQLException, ExecutionException {
        ArrayList<EconomyUser> economyUsers = new ArrayList<>();
        if (!DatabaseConnector.getInstance().useSQLdDB) return economyUsers;

        if (leaderBoardType == LeaderBoardType.ROB) {
            AtomicInteger loaded = new AtomicInteger();
            try (Connection conn = DatabaseConnector.getInstance().getConnection();
                 PreparedStatement selectStatement = conn.prepareStatement("SELECT *, (success_rob + fail_rob) AS sumTotal FROM user_stats ORDER BY sumTotal DESC")) {
                try (ResultSet result = selectStatement.executeQuery()) {

                    while (result.next()) {
                        long uuid = result.getLong("uuid");

                        if (loaded.get() < 20) {
                            economyUsers.add(cache.get(uuid, () -> {
                                loaded.getAndDecrement();
                                return getResultUser(uuid);
                            }));
                        }
                        else {
                            economyUsers.add(getResultUser(uuid));
                        }

                    }
                }
            }
        }

        if (leaderBoardType == LeaderBoardType.VOICE) {
            AtomicInteger loaded = new AtomicInteger();
            try (Connection conn = DatabaseConnector.getInstance().getConnection();
                 PreparedStatement selectStatement = conn.prepareStatement("SELECT * FROM user_stats ORDER BY voice_time DESC")) {
                try (ResultSet result = selectStatement.executeQuery()) {

                    while (result.next()) {
                        long uuid = result.getLong("uuid");

                        if (loaded.get() < 20) {
                            economyUsers.add(cache.get(uuid, () -> {
                                loaded.getAndDecrement();
                                return getResultUser(uuid);
                            }));
                        }
                        else {
                            economyUsers.add(getResultUser(uuid));
                        }

                    }
                }
            }
        }

        if (leaderBoardType == LeaderBoardType.MESSAGES) {
            AtomicInteger loaded = new AtomicInteger();
            try (Connection conn = DatabaseConnector.getInstance().getConnection();
                 PreparedStatement selectStatement = conn.prepareStatement("SELECT * FROM user_stats ORDER BY total_message DESC")) {
                try (ResultSet result = selectStatement.executeQuery()) {

                    while (result.next()) {
                        long uuid = result.getLong("uuid");

                        if (loaded.get() < 20) {
                            economyUsers.add(cache.get(uuid, () -> {
                                loaded.getAndDecrement();
                                return getResultUser(uuid);
                            }));
                        }
                        else {
                            economyUsers.add(getResultUser(uuid));
                        }

                    }
                }
            }
        }

        if (leaderBoardType == LeaderBoardType.LEVEL) {
            AtomicInteger loaded = new AtomicInteger();
            try (Connection conn = DatabaseConnector.getInstance().getConnection();
                 PreparedStatement selectStatement = conn.prepareStatement("SELECT * FROM users_economy ORDER BY level DESC")) {
                try (ResultSet result = selectStatement.executeQuery()) {

                    while (result.next()) {
                        long uuid = result.getLong("uuid");

                        if (loaded.get() < 20) {
                            economyUsers.add(cache.get(uuid, () -> {
                                loaded.getAndDecrement();
                                return getResultUser(uuid);
                            }));
                        }
                        else {
                            economyUsers.add(getResultUser(uuid));
                        }

                    }
                }
            }
        }

        if (leaderBoardType == LeaderBoardType.BALANCE) {
            AtomicInteger loaded = new AtomicInteger();
            try (Connection conn = DatabaseConnector.getInstance().getConnection();
                 PreparedStatement selectStatement = conn.prepareStatement("SELECT *, (bank + coins) AS sumTotal FROM users_economy ORDER BY sumTotal DESC")) {
                try (ResultSet result = selectStatement.executeQuery()) {

                    while (result.next()) {
                        long uuid = result.getLong("uuid");

                        if (loaded.get() < 20) {
                            economyUsers.add(cache.get(uuid, () -> {
                                loaded.getAndDecrement();
                                return getResultUser(uuid);
                            }));
                        }
                        else {
                            economyUsers.add(getResultUser(uuid));
                        }

                    }
                }
            }
        }

        return economyUsers;
    }

    private CacheRestAction<User> getUser(long uuid) {
        return BotMain.getInstance().getJda().retrieveUserById(uuid).useCache(false);
    }

    public RestAction<EconomyUser> getEconomyUser(long uuid) {
        return getUser(uuid).map(this::getEconomyUser);
    }

    private EconomyUser getResultUser(long uuid) {
        return DatabaseConnector.getInstance().getEconomyUser(uuid);
    }

    public static synchronized EconomyManager getInstance() {
        if (instance == null) {
            instance = new EconomyManager();
        }
        return instance;
    }
}