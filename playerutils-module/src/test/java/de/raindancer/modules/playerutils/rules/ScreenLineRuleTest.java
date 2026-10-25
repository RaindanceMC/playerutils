package de.raindancer.modules.playerutils.rules;

import de.raindancer.core.moderation.players.PingHistory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("What Player Utils' screens say")
class ScreenLineRuleTest {

    private final PingRule pings = new PingRule();
    private final ScreenLineRule rule = new ScreenLineRule(pings);

    private static PingHistory.Summary minute() {
        return new PingHistory.Summary("Sky", 0, 60_000, List.of(new PingHistory.Sample(1, 40)), 40, 1, 30, 45, 44, 70, 80, 6);
    }

    @Test
    @DisplayName("your ping: now with bars and grade, the last minute's average, worst and jitter, the trend")
    void ping() {
        assertThat(rule.ping(pings.grade(42), minute(), "<green>▁▂")).isEqualTo(
                "<gold><bold>Your ping</bold></gold>\n<gray>Now <white>42 ms <green>▮▮▮▮▮<dark_gray>\n"
                        + "<gray>Connection <green>excellent\n"
                        + "<gray>Last minute <white>45 ms <dark_gray>· <gray>worst <white>80 ms <dark_gray>· <gray>jitter <white>6 ms\n"
                        + "<green>▁▂");
    }

    @Test
    @DisplayName("without a minute of readings there is only the number; not measured says so")
    void young() {
        assertThat(rule.ping(pings.grade(120), null, null)).isEqualTo(
                "<gold><bold>Your ping</bold></gold>\n<gray>Now <white>120 ms <yellow>▮▮▮<dark_gray>▯▯\n<gray>Connection <yellow>okay");
        assertThat(rule.ping(pings.grade(-1), null, null)).isEqualTo(
                "<gold><bold>Your ping</bold></gold>\n<gray>Not measured yet");
    }

    @Test
    @DisplayName("the board lists the best connections first, coloured by place, with the average of everybody")
    void board() {
        assertThat(rule.board(List.of())).isEqualTo("<gold><bold>Ping board</bold></gold>\n<gray>Nothing measured yet");
        String text = rule.board(List.of(new ScreenLineRule.Reading("Slow", 300), new ScreenLineRule.Reading("Fast", 20),
                new ScreenLineRule.Reading("Mid", 90), new ScreenLineRule.Reading("Unknown", -1)));
        assertThat(text).isEqualTo("<gold><bold>Ping board</bold></gold>\n<gold>1. <white>Fast <green>20 ms\n"
                + "<gray>2. <white>Mid <green>90 ms\n<#cd7f32>3. <white>Slow <red>300 ms\n"
                + "<gray>Average <white>137 ms <dark_gray>(3 players)");
    }

    @Test
    @DisplayName("five at most, names cannot inject tags")
    void many() {
        var all = IntStream.range(0, 9).mapToObj(n -> new ScreenLineRule.Reading("P" + n, 10 + n)).toList();
        assertThat(rule.board(all).lines().count()).isEqualTo(1 + 5 + 1);
        assertThat(rule.board(List.of(new ScreenLineRule.Reading("<red>x", 10)))).contains("\\<red>x");
    }
}
