package com.yo1000;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class Janken {
    private final InputStream in;
    private final PrintStream out;

    public Janken(InputStream in, PrintStream out) {
        this.in = in;
        this.out = out;
    }

    public void play() {
        String s="-1";
        long i=-1;
        while (s.equals(""+i)) {
            s="";
            out.print("1=Rock, 2=Paper, 3=Scissors... [1/2/3]: ");
            try {
                char c;
                while (!("1".equals(s) || "2".equals(s) || "3".equals(s))) {
                    s = "";
                    while ((c = (char) in.read()) != '\n'&& c!='\0') {
                        s = ""+c;
                    }
                    ;
                }

                out.println();
                out.println("Chose by You: " + ("1".equals(s) ? "1=Rock" : "2".equals(s) ? "2=Paper" : "3=Scissors"));
//            String c = s.next();
//            System.out.println(c);

                i = (System.currentTimeMillis()) % 3+1;

                out.print("Chose by COM:");
                out.println("1".equals(""+i) ? " 1=Rock" : "2".equals(""+i)? " 2=Paper":" 3=Scissors");

//        System.out.println("[" +s+"]");
//        System.out.println("["+i+"]");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
//        System.out.println("Shoot!");
//        Scanner s = new Scanner(System.in);
//        s.nextLine();

        }
        out.println();
        if((Integer.parseInt(s) - Integer.parseInt(""+i)+3)%3 ==1){
            out.println("You win!");
        }else out.println("You lose.");

}
}
