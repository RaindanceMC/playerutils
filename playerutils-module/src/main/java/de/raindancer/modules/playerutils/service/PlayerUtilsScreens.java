package de.raindancer.modules.playerutils.service;

import de.raindancer.core.moderation.players.PingHistory;
import de.raindancer.core.moderation.vanish.Vanish;
import de.raindancer.core.ui.hologram.ScreenKind;
import de.raindancer.core.ui.hologram.Screens;
import de.raindancer.modules.playerutils.PlayerUtilsSettings;
import de.raindancer.modules.playerutils.rules.PingRule;
import de.raindancer.modules.playerutils.rules.ScreenLineRule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Player Utils' screens for Core's {@code /screen}, drawn from Core's ping history (sampled every few seconds, safe to
 * read off the server thread): everybody's own ping, and the server's best connections.
 */
public final class PlayerUtilsScreens implements IPlayerUtilsService {

    public static final List<String> KINDS = List.of("playerutils-ping", "playerutils-ping-board");
    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final int TREND_COLUMNS = 20;

    private final Server server;
    private final Vanish vanish;
    private final PingHistory history;
    private final PingRule pings;
    private final ScreenLineRule rule;

    public PlayerUtilsScreens(Server server, Vanish vanish, PingHistory history, PingRule pings) {
        this.server = server;
        this.vanish = vanish;
        this.history = history;
        this.pings = pings;
        this.rule = new ScreenLineRule(pings);
    }

    @Override
    public void settings(PlayerUtilsSettings settings) {
        // The screens read live state and no setting.
    }

    @Override
    public String describe() {
        return "the screens for /screen: everybody's own ping and the ping board";
    }

    public void register(Screens screens, Plugin owner) {
        screens.register(owner, ScreenKind.personal("playerutils-ping", "Your ping",
                "Everybody's own ping now, over the last minute, and its trend.", Material.COMPASS, this::ping));
        screens.register(owner, ScreenKind.everyone("playerutils-ping-board", "Ping board",
                "The best connections on this server right now, and everybody's average.", Material.CLOCK, this::board));
    }

    private Component ping(ScreenKind.Viewer viewer) {
        Optional<PingHistory.Summary> minute = history.summary(viewer.id(), Duration.ofMinutes(1));
        int latest = minute.map(PingHistory.Summary::latest).orElseGet(() ->
                history.summary(viewer.id(), PingHistory.KEPT).map(PingHistory.Summary::latest).orElse(-1));
        List<PingRule.Window> windows = pings.windows(window -> history.summary(viewer.id(), window));
        String trend = windows.isEmpty() ? null : pings.trend(windows.getLast().summary(), TREND_COLUMNS);
        return MINI.deserialize(rule.ping(pings.grade(latest), minute.orElse(null), trend));
    }

    private Component board() {
        List<ScreenLineRule.Reading> online = server.getOnlinePlayers().stream()
                .filter(player -> !vanish.isVanished(player.getUniqueId()))
                .map(player -> new ScreenLineRule.Reading(player.getName(),
                        history.summary(player.getUniqueId(), Duration.ofMinutes(1))
                                .map(PingHistory.Summary::latest).orElse(-1)))
                .toList();
        return MINI.deserialize(rule.board(online));
    }
}
