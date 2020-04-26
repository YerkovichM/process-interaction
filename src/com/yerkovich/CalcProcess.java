package com.yerkovich;

import java.util.Random;
import java.util.Scanner;

public class CalcProcess {
    public static final Random random = new Random();

    public static void main(String[] args) {
        long time = Long.parseLong(args[0]);
        Scanner scanner = new Scanner(System.in);
        long start = System.currentTimeMillis();
        while ((System.currentTimeMillis() - start) < time) {
            int x = scanner.nextInt();
            System.out.println(f(x));
        }
    }

    private static boolean f(int x) {
        try {
            Thread.sleep(random.nextInt(1_000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return random.nextBoolean();
    }
}
