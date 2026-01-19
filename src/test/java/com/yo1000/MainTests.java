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

class JankenTests {

    @Test
    void play_acceptsInputAndPrintsResult() {
        // [0]=Rock-win     | [1]=Rock-lose     | [2]=Rock-draw
        // [3]=Paper-win    | [4]=Paper-lose    | [5]=Paper-draw
        // [6]=Scissors-win | [7]=Scissors-lose | [8]=Scissors-draw
        int[] winLoseFlags = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0};

        int hand = 1; // 1=Rock, 2=Paper, 3=Scissors

        // `i < 1000` is a safety check to prevent infinite loops caused by test-side bugs
        for (int i = 0; i < 1000 && Arrays.stream(winLoseFlags).anyMatch(value -> value != 1); i++) {
            if (i > 0) System.out.println("----------------------------------------");
            System.out.println("Round=" + (i + 1) + ", Hand=" + hand);

            String input = (hand + "\n").repeat(1000);

            InputStream in = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
            ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(outBytes, true, StandardCharsets.UTF_8);

            Janken sut = new Janken(in, out);
            Assertions.assertDoesNotThrow(sut::play);

            String output = outBytes.toString(StandardCharsets.UTF_8);

            Assertions.assertTrue(output.contains("1=Rock, 2=Paper, 3=Scissors... [1/2/3]:"));
            Assertions.assertTrue(output.contains("Chose by You: " + hand));
            Assertions.assertTrue(output.contains("Chose by COM:"));
            Assertions.assertTrue(output.contains("You win!") || output.contains("You lose."));

            Pattern patternYou = Pattern.compile("Chose by You:\\s*(" + hand + "=.+)", Pattern.MULTILINE);
            Pattern patternCOM = Pattern.compile("Chose by COM:\\s*([123]=.+)", Pattern.MULTILINE);

            Matcher matcherYou = patternYou.matcher(output);
            Matcher matcherCOM = patternCOM.matcher(output);

            boolean foundYou = matcherYou.find();
            boolean foundCOM = matcherCOM.find();

            Assertions.assertTrue(foundYou);
            Assertions.assertTrue(foundCOM);

            String choseByYou = matcherYou.group(1);
            String choseByCOM = matcherCOM.group(1);

            boolean won = false;
            boolean lost = false;
            int drawCount = 0;

            // Continue the loop until it is no longer a draw
            while (choseByYou.equals(choseByCOM)) {
                // Assert draw
                Assertions.assertEquals(choseByYou, choseByCOM);
                drawCount++;

                foundYou = matcherYou.find();
                foundCOM = matcherCOM.find();

                Assertions.assertEquals(foundYou, foundCOM);

                if (foundYou && foundCOM) {
                    choseByYou = matcherYou.group(1);
                    choseByCOM = matcherCOM.group(1);
                } else {
                    break;
                }
            }

            Assertions.assertEquals(foundYou, foundCOM);

            // Assert not draw (It must be decided by win or loss)
            Assertions.assertNotEquals(choseByYou, choseByCOM);

            Assertions.assertTrue(choseByYou.equals("1=Rock") || choseByYou.equals("2=Paper") || choseByYou.equals("3=Scissors"));
            Assertions.assertTrue(choseByCOM.equals("1=Rock") || choseByCOM.equals("2=Paper") || choseByCOM.equals("3=Scissors"));

            // Win or lose is displayed only once at the end.
            Pattern patternResult = Pattern.compile("(You win!|You lose\\.)", Pattern.MULTILINE);
            Matcher matcherResult = patternResult.matcher(output);
            Assertions.assertTrue(matcherResult.find());
            String result = matcherResult.group(1);
            Assertions.assertFalse(matcherResult.find());

            switch (choseByYou) {
                case "1=Rock" -> {
                    if (choseByCOM.equals("2=Paper")) {
                        lost = true;
                        Assertions.assertEquals("You lose.", result);
                    } else if (choseByCOM.equals("3=Scissors")) {
                        won = true;
                        Assertions.assertEquals("You win!", result);
                    } else {
                        Assertions.fail();
                    }
                }
                case "2=Paper" -> {
                    if (choseByCOM.equals("3=Scissors")) {
                        lost = true;
                        Assertions.assertEquals("You lose.", result);
                    } else if (choseByCOM.equals("1=Rock")) {
                        won = true;
                        Assertions.assertEquals("You win!", result);
                    } else {
                        Assertions.fail();
                    }
                }
                case "3=Scissors" -> {
                    if (choseByCOM.equals("1=Rock")) {
                        lost = true;
                        Assertions.assertEquals("You lose.", result);
                    } else if (choseByCOM.equals("2=Paper")) {
                        won = true;
                        Assertions.assertEquals("You win!", result);
                    } else {
                        Assertions.fail();
                    }
                }
                default -> Assertions.fail();
            }

            if (won) {
                winLoseFlags[(hand - 1) * 3] = 1;
            } else if (lost) {
                winLoseFlags[(hand - 1) * 3 + 1] = 1;
            }

            if (drawCount > 0) {
                winLoseFlags[(hand - 1) * 3 + 2] = 1;
            }

            if (winLoseFlags[(hand - 1) * 3] == 1
                    && winLoseFlags[(hand - 1) * 3 + 1] == 1
                    && winLoseFlags[(hand - 1) * 3 + 2] == 1) {
                hand++;
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
