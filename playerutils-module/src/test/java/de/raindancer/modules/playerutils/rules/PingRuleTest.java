package de.raindancer.modules.playerutils.rules;

import de.raindancer.core.moderation.players.PingHistory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class PingRuleTest {

    private final PingRule rule = new PingRule();

    @Test
    @DisplayName("lower is better, in bars and words")
    void grades() {
        assertThat(rule.grade(12).bars()).isEqualTo(5);
        assertThat(rule.grade(80).bars()).isEqualTo(4);
        assertThat(rule.grade(120).bars()).isEqualTo(3);
        assertThat(rule.grade(200).bars()).isEqualTo(2);
        assertThat(rule.grade(350).bars()).isEqualTo(1);
        assertThat(rule.grade(900).bars()).isZero();
        assertThat(rule.grade(-1).bars()).as("not measured yet").isZero();
    }

    @Test
    @DisplayName("the bar drawing has five cells, filled from the left")
    void drawing() {
        assertThat(rule.grade(80).drawn()).contains("▮▮▮▮").contains("▯");
    }

    private final UUID ada = UUID.randomUUID();
    private final AtomicLong now = new AtomicLong(1_000_000_000L);
    private final PingHistory history = new PingHistory(now::get);

    private void sampleFor(Duration length, int ms) {
        for (long t = 0; t < length.toMillis(); t += PingHistory.EVERY.toMillis()) {
            history.record(ada, "Ada", ms);
            now.addAndGet(PingHistory.EVERY.toMillis());
        }
    }

    private List<String> labels() {
        return rule.windows(window -> history.summary(ada, window)).stream().map(PingRule.Window::label).toList();
    }

    @Test
    @DisplayName("nothing measured shows no windows")
    void noWindows() {
        assertThat(labels()).isEmpty();
    }

    @Test
    @DisplayName("a window is shown only when it reaches further back than the one before")
    void windowsThatAddSomething() {
        sampleFor(Duration.ofSeconds(30), 100);
        assertThat(labels()).containsExactly("1 min");
        sampleFor(Duration.ofMinutes(5), 100);
        assertThat(labels()).containsExactly("1 min", "10 min");
        sampleFor(Duration.ofMinutes(30), 100);
        assertThat(labels()).containsExactly("1 min", "10 min", "1 h");
    }

    @Test
    @DisplayName("somebody who left half an hour ago has no last minutes, but still their hour")
    void afterLeaving() {
        sampleFor(Duration.ofMinutes(20), 300);
        now.addAndGet(Duration.ofMinutes(30).toMillis());
        assertThat(labels()).containsExactly("1 h");
    }

    @Test
    @DisplayName("the trend line colours each bar by its grade, a gap stays blank")
    void trend() {
        history.record(ada, "Ada", 30);
        now.addAndGet(Duration.ofSeconds(40).toMillis());
        history.record(ada, "Ada", 900);
        PingHistory.Summary s = history.summary(ada, Duration.ofMinutes(1)).orElseThrow();
        String drawn = rule.trend(s, 3);
        // 20 s columns from 20 s before the first sample: nothing, 30 ms, 900 ms
        assertThat(drawn).isEqualTo(" <green>▁<dark_red>█");
    }
}
