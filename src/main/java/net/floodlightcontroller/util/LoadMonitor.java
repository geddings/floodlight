package net.floodlightcontroller.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class LoadMonitor implements Runnable {

    public enum LoadLevel {
        OK,
        HIGH,
        VERYHIGH,
    }

    public LoadLevel getLoadLevel() {
        return loadlevel ;
    }

    public double getLoad() {
        return load ;
    }

    public static final int LOADMONITOR_SAMPLING_INTERVAL = 1000; // in milisec
    public static final double THRESHOLD_HIGH = 0.85;
    public static final double THRESHOLD_VERYHIGH = 0.95;
    public static final int MAX_LOADED_ITERATIONS = 5;
    public static final int MAX_LOAD_HISTORY = 5;

    protected volatile double load;
    protected volatile LoadLevel loadlevel;
    protected int itersLoaded;

    protected boolean isLinux;
    protected int numcores;
    protected int jiffyNanos;
    protected long[] lastNanos;
    protected long[] lastIdle;

    public LoadMonitor() {
        loadlevel = LoadLevel.OK;
        load = 0.0;
        itersLoaded = 0;

        lastNanos = new long[MAX_LOAD_HISTORY];
        lastIdle = new long[MAX_LOAD_HISTORY];
        for (int i=0 ; i<MAX_LOAD_HISTORY ; i++) {
            lastNanos[i] = 0L;
            lastIdle[i] = 0L;
        }

        isLinux = System.getProperty("os.name").equals("Linux");
        numcores = 1;
        jiffyNanos = 10 * 1000 * 1000;
        if (isLinux) {
            try {
                numcores = Integer.parseInt(this.runcmd("/usr/bin/nproc"));
                jiffyNanos = (1000 * 1000 * 1000) / Integer.parseInt(this.runcmd("/usr/bin/getconf CLK_TCK"));
            }
            catch (NumberFormatException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
            if (!isLinux) return;

        long currNanos = System.nanoTime();
        long currIdle = this.readIdle();
        for (int i=0 ; i < (MAX_LOAD_HISTORY - 1) ; i++) {
            lastNanos[i] = lastNanos[i+1];
            lastIdle[i] = lastIdle[i+1];
        }
        lastNanos[MAX_LOAD_HISTORY - 1] = currNanos;
        lastIdle[MAX_LOAD_HISTORY - 1] = currIdle;

        if (itersLoaded >= MAX_LOADED_ITERATIONS) {
            loadlevel = LoadLevel.OK;
            itersLoaded = 0;
            return;
        }

        long nanos = lastNanos[MAX_LOAD_HISTORY - 1] - lastNanos[0];
        long idle = lastIdle[MAX_LOAD_HISTORY - 1] - lastIdle[0];
        load =  1.0 - ( (double)(idle * jiffyNanos) / (double)(nanos * numcores));

        if (load > THRESHOLD_VERYHIGH) {
            loadlevel = LoadLevel.VERYHIGH;
            itersLoaded += 1;
            return;
        }
        if (load > THRESHOLD_HIGH) {
            loadlevel = LoadLevel.HIGH;
            itersLoaded += 1;
            return;
        }
        loadlevel = LoadLevel.OK;
        itersLoaded = 0;
        return;
    }

    protected String runcmd(String cmd) {
        String line;
        StringBuilder ret = new StringBuilder();
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader input =
                new BufferedReader(
                new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                ret.append(line);
            }
            input.close();
            p.waitFor();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        return ret.toString();
    }

    protected long readIdle() {
        long idle = 0;

        try {
            FileInputStream fs = new FileInputStream("/proc/stat");
            BufferedReader reader = new BufferedReader(new InputStreamReader(fs));
            idle = Long.parseLong(reader.readLine().split("\\s+")[4]);
            reader.close();
            fs.close();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }

        return idle;
    }

    public ScheduledFuture<?> startMonitoring(ScheduledExecutorService scheduler)
    {
        ScheduledFuture<?> monitorTask =
                scheduler.scheduleAtFixedRate(this,
                        0, LOADMONITOR_SAMPLING_INTERVAL, TimeUnit.MILLISECONDS);
        return monitorTask;
    }

    public ScheduledFuture<?> printMonitoring(ScheduledExecutorService scheduler)
    {
        final LoadMonitor mon = this;
        ScheduledFuture<?> monitorTask =
                scheduler.scheduleAtFixedRate(new Runnable() {
                    public void run() {
                        System.out.println(mon.getLoad());
                    }
                },
                LOADMONITOR_SAMPLING_INTERVAL/2, LOADMONITOR_SAMPLING_INTERVAL, TimeUnit.MILLISECONDS);
        return monitorTask;
    }

    /*
     * For testing
     */
    public static void main(String[] args) {
            final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);
            final LoadMonitor monitor = new LoadMonitor();
            final ScheduledFuture<?> monitorTask = monitor.startMonitoring(scheduler);
            final ScheduledFuture<?> printTask = monitor.printMonitoring(scheduler);

            scheduler.schedule(new Runnable() {
            public void run() {
                    monitorTask.cancel(true);
                    printTask.cancel(true);
                }
        }, 5*60, TimeUnit.SECONDS);
    }

}
