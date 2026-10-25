package de.raindancer.modules.playerutils.rules;

import de.raindancer.core.moderation.players.PingHistory;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Comparator;
import java.util.List;

/** What Player Utils' screens count and say. */
public final class ScreenLineRule implements IPlayerUtilsRule {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final String[] PLACES = {"<gold>", "<gray>", "<#cd7f32>", "<white>", "<white>"};

    /** One online player's latest ping; negative when not measured yet. */
    public record Reading(String name, int ms) {
    }

    private final PingRule pings;

    public ScreenLineRule(PingRule pings) {
        this.pings = pings;
    }

    /**
     * @param lastMinute null until a reading of the last minute exists
     * @param trend      the longest window as coloured bars, or null
     */
    public String ping(PingRule.Grade now, PingHistory.Summary lastMinute, String trend) {
        String head = "<gold><bold>Your ping</bold></gold>";
        if (now.milliseconds() < 0) {
            return head + "\n<gray>Not measured yet";
        }
        StringBuilder text = new StringBuilder(head).append("\n<gray>Now <white>").append(now.milliseconds())
                .append(" ms ").append(now.drawn())
                .append("\n<gray>Connection <").append(now.colour()).append(">").append(now.label());
        if (lastMinute != null) {
            text.append("\n<gray>Last minute <white>").append(lastMinute.average()).append(" ms <dark_gray>· <gray>worst <white>")
                    .append(lastMinute.max()).append(" ms <dark_gray>· <gray>jitter <white>").append(lastMinute.jitter())
                    .append(" ms");
        }
        if (trend != null && !trend.isEmpty()) {
            text.append("\n").append(trend);
        }
        return text.toString();
    }

    public String board(List<Reading> online) {
        String head = "<gold><bold>Ping board</bold></gold>";
        List<Reading> measured = online.stream().filter(reading -> reading.ms() >= 0)
                .sorted(Comparator.comparingInt(Reading::ms).thenComparing(reading -> reading.name().toLowerCase()))
                .toList();
        if (measured.isEmpty()) {
            return head + "\n<gray>Nothing measured yet";
        }
        StringBuilder text = new StringBuilder(head);
        for (int index = 0; index < Math.min(PLACES.length, measured.size()); index++) {
            Reading reading = measured.get(index);
            text.append("\n").append(PLACES[index]).append(index + 1).append(". <white>")
                    .append(MINI.escapeTags(reading.name())).append(" <").append(pings.grade(reading.ms()).colour())
                    .append(">").append(reading.ms()).append(" ms");
        }
        long average = Math.round(measured.stream().mapToInt(Reading::ms).average().orElse(0));
        return text.append("\n<gray>Average <white>").append(average).append(" ms <dark_gray>(")
                .append(measured.size()).append(measured.size() == 1 ? " player)" : " players)").toString();
    }

    @Override
    public String describe() {
        return "what Player Utils' screens count and say";
    }
}
