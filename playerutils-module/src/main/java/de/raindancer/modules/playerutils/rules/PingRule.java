package de.raindancer.modules.playerutils.rules;

import de.raindancer.core.moderation.players.PingHistory;
import de.raindancer.core.ui.text.Sparkline;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/** How good a ping is, in words, a colour and five bars — now and over the last minute, ten minutes and hour. */
public final class PingRule implements IPlayerUtilsRule {

    private static final List<Duration> WINDOWS = List.of(Duration.ofMinutes(1), Duration.ofMinutes(10), Duration.ofHours(1));

    /** Below this, ups and downs in a trend line are noise and drawn flat. */
    private static final int SMALLEST_SPAN_MS = 50;

    public record Window(String label, PingHistory.Summary summary) {
    }

    public record Grade(int milliseconds, int bars, String label, String colour) {

        public String drawn() {
            return "<" + colour + ">" + "▮".repeat(bars) + "<dark_gray>" + "▯".repeat(5 - bars);
        }
    }

    public Grade grade(int milliseconds) {
        if (milliseconds < 0) {
            return new Grade(milliseconds, 0, "not measured yet", "gray");
        }
        if (milliseconds < 50) {
            return new Grade(milliseconds, 5, "excellent", "green");
        }
        if (milliseconds < 100) {
            return new Grade(milliseconds, 4, "good", "green");
        }
        if (milliseconds < 150) {
            return new Grade(milliseconds, 3, "okay", "yellow");
        }
        if (milliseconds < 250) {
            return new Grade(milliseconds, 2, "laggy", "gold");
        }
        if (milliseconds < 400) {
            return new Grade(milliseconds, 1, "bad", "red");
        }
        return new Grade(milliseconds, 0, "dreadful", "dark_red");
    }

    /**
     * The windows worth showing: each one only when it reaches further back than the one before — a player who
     * joined 30 seconds ago has a last minute and nothing more.
     */
    public List<Window> windows(Function<Duration, Optional<PingHistory.Summary>> read) {
        List<Window> shown = new ArrayList<>();
        Duration before = Duration.ZERO;
        for (Duration window : WINDOWS) {
            Optional<PingHistory.Summary> summary = read.apply(window);
            if (summary.isPresent()) {
                PingHistory.Summary s = summary.get();
                if (before.isZero() || s.all().getFirst().at() < s.until() - before.toMillis()) {
                    shown.add(new Window(label(window), s));
                }
            }
            before = window;
        }
        return shown;
    }

    /** The window as {@code columns} bars, each coloured by how good its average was. */
    public String trend(PingHistory.Summary summary, int columns) {
        int[] values = summary.columns(columns);
        String bars = Sparkline.of(values, SMALLEST_SPAN_MS);
        StringBuilder drawn = new StringBuilder();
        String colour = null;
        for (int i = 0; i < values.length; i++) {
            if (values[i] >= 0) {
                String now = grade(values[i]).colour();
                if (!now.equals(colour)) {
                    drawn.append('<').append(now).append('>');
                    colour = now;
                }
            }
            drawn.append(bars.charAt(i));
        }
        return drawn.toString();
    }

    private static String label(Duration window) {
        return window.toHours() >= 1 ? window.toHours() + " h" : window.toMinutes() + " min";
    }

    @Override
    public String describe() {
        return "grading a ping";
    }
}
