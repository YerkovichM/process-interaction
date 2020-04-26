package com.yerkovich;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

public class Main {
    private static final long PROGRAM_PROCESS_TIME = 3_000_000L;
    private static final long POLLING_TIME = 100;

    public static void main(String[] args) throws IOException, InterruptedException {
        long start = System.currentTimeMillis();

        Runtime.getRuntime().exec("javac -cp src src/com/yerkovich/CalcProcess.java").waitFor();
        Process processF = Runtime.getRuntime().exec("java -cp src com.yerkovich.CalcProcess " + PROGRAM_PROCESS_TIME);
        Process processG = Runtime.getRuntime().exec("java -cp src com.yerkovich.CalcProcess " + PROGRAM_PROCESS_TIME);

        Scanner inF = new Scanner(processF.getInputStream());
        Scanner inG = new Scanner(processG.getInputStream());

        PrintStream outF = new PrintStream(processF.getOutputStream(), true);
        PrintStream outG = new PrintStream(processG.getOutputStream(), true);

        Scanner input = new Scanner(System.in);

        ProcessResult processResultF = new ProcessResult();
        ProcessResult processResultG = new ProcessResult();

        new StoperThread(processF, processG).start();

        new ReaderThread(processResultF, inF).start();
        new ReaderThread(processResultG, inG).start();

        while ((System.currentTimeMillis() - start) < PROGRAM_PROCESS_TIME) {
            System.out.println("Enter x: ");
            int x = input.nextInt();

            outF.println(x);
            outG.println(x);

            boolean result;

            boolean isGReturned = false;
            boolean isFReturned = false;

            while (true) {
                if (processResultF.isCompleted()) {
                    boolean resultF = processResultF.getResult();
                    System.out.println("F returned: " + resultF);
                    if (resultF) {
                        result = true;
                        break;
                    }
                    isFReturned = true;
                }

                if (processResultG.isCompleted()) {
                    boolean resultG = processResultG.getResult();
                    System.out.println("G returned: " + resultG);
                    if (resultG) {
                        result = true;
                        break;
                    }
                    isGReturned = true;
                }

                if (isFReturned && isGReturned){
                    result = false;
                    break;
                }

                Thread.sleep(POLLING_TIME);
            }
            System.out.println("Result: " + result);
        }
    }

    private static class StoperThread extends Thread {
        Process[] processes;

        public StoperThread(Process... processes) {
            this.processes = processes;
        }

        @Override
        public void run() {
            Scanner input = new Scanner(System.in);
            long loopStart = System.currentTimeMillis();
            while (true) {
                if ((System.currentTimeMillis() - loopStart) > 30_000) {
                    System.out.println("Want to continue processing? (yes/no)");
                    String anwer = input.nextLine();
                    if ("no".equals(anwer.toLowerCase())) {
                        for (Process p : processes) {
                            stopProcess(p);
                        }
                        System.exit(1);
                    }
                    loopStart = System.currentTimeMillis();
                }
                try {
                    Thread.sleep(POLLING_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void stopProcess(Process process) {
            process.destroy();
            if (process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    private static class ReaderThread extends Thread {
        private ProcessResult processResult;
        private Scanner scanner;

        ReaderThread(ProcessResult processResult,  Scanner scanner) {
            this.processResult = processResult;
            this.scanner = scanner;
        }

        @Override
        public void run() {
            while (true){
                if(scanner.hasNextBoolean()){
                boolean result = scanner.nextBoolean();
                processResult.setResult(result);
                }
                try {
                    sleep(POLLING_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class ProcessResult{
        boolean result;
        boolean completed;

        public synchronized void setResult(boolean result){
            this.result = result;
            completed = true;
        }

        public synchronized boolean isCompleted(){
            return completed;
        }

        public synchronized boolean getResult(){
            completed = false;
            return result;
        }
    }
}
