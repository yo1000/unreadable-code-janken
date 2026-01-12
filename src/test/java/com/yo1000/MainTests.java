package com.yo1000;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JankenTests {

    @Test
    void play_acceptsInputAndPrintsResult() {
        // [0]=Rock-win     | [1]=Rock-lose     | [2]=Rock-draw
        // [3]=Paper-win    | [4]=Paper-lose    | [5]=Paper-draw
        // [6]=Scissors-win | [7]=Scissors-lose | [8]=Scissors-draw
        int[] winLoseFlags = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0};

        int hand = 1; // 1=Rock, 2=Paper, 3=Scissors

        for (int i = 0; i < 1000 && Arrays.stream(winLoseFlags).anyMatch(value -> value != 1); i++) {
            if (i > 0) System.out.println("----------------------------------------");
            System.out.println("Round=" + (i + 1) + ", Hand=" + hand);

            String input = (hand + "\n").repeat(1000);

            InputStream in = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
            ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(outBytes, true, StandardCharsets.UTF_8);

            Janken sut = new Janken(in, out);
            assertDoesNotThrow(sut::play);

            String output = outBytes.toString(StandardCharsets.UTF_8);

            Pattern patternYou = Pattern.compile("Chose by You:\\s*(" + hand + ".+)", Pattern.MULTILINE);
            Pattern patternCOM = Pattern.compile("Chose by COM:\\s*(.+)", Pattern.MULTILINE);

            Matcher matcherYou = patternYou.matcher(output);
            Matcher matcherCOM = patternCOM.matcher(output);
            Matcher matcherResult = patternCOM.matcher(output);

            boolean foundYou = matcherYou.find();
            boolean foundCOM = matcherCOM.find();
            boolean foundResult = matcherResult.find();

            if (!foundYou || !foundCOM || !foundResult) {
                System.out.println("foundYou: " + foundYou);
                System.out.println("foundCOM: " + foundCOM);
                System.out.println("foundResult: " + foundResult);

                continue;
            }

            assertTrue(output.contains("1=Rock, 2=Paper, 3=Scissors... [1/2/3]:"),
                    () -> "prompt not found. output=\n" + output);
            assertTrue(output.contains("Chose by You: " + hand),
                    () -> "player choice not found. output=\n" + output);
            assertTrue(output.contains("Chose by COM:"),
                    () -> "com choice not found. output=\n" + output);
            assertTrue(output.contains("You win!") || output.contains("You lose."),
                    () -> "result not found. output=\n" + output);

            if (output.contains("You win!")) {
                winLoseFlags[(hand - 1) * 3] = 1;
            } else if (output.contains("You lose.")) {
                winLoseFlags[(hand - 1) * 3 + 1] = 1;
            }

            String choseByYou = matcherYou.group(1);
            String choseByCOM = matcherCOM.group(1);

            if (choseByYou.equals(choseByCOM)) {
                winLoseFlags[(hand - 1) * 3 + 2] = 1;
            }

            if (winLoseFlags[(hand - 1) * 3] == 1
                    && winLoseFlags[(hand - 1) * 3 + 1] == 1
                    && winLoseFlags[(hand - 1) * 3 + 2] == 1) {
                hand++;
            }

            int drawCount = 0;

            while (matcherYou.find()) {
                choseByYou = matcherYou.group(1);
                drawCount++;
            }

            while (matcherCOM.find()) {
                choseByCOM = matcherCOM.group(1);
            }

            System.out.println("Draw=" + drawCount);
            System.out.println(choseByYou);
            System.out.println(choseByCOM);
            System.out.println("Flags=" + Arrays.stream(winLoseFlags).mapToObj(String::valueOf).collect(Collectors.joining()));
        }

        Assertions.assertArrayEquals(new int [] {1, 1, 1, 1, 1, 1, 1, 1, 1}, winLoseFlags,
                () -> "not covered win-lose patterns: " + Arrays.stream(winLoseFlags).mapToObj(String::valueOf).collect(Collectors.joining()));
    }
}
