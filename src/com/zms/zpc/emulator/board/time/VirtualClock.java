package com.zms.zpc.emulator.board.time;

import com.zms.zpc.emulator.*;
import com.zms.zpc.emulator.board.*;
import com.zms.zpc.emulator.debug.DummyDebugger;
import com.zms.zpc.support.BaseObj;

import java.io.*;
import java.util.*;
import java.util.logging.*;

/**
 * Created by 张小美 on 17/八月/12.
 * Copyright 2002-2016
 */
public class VirtualClock extends BaseObj implements Runnable, Clock {

    public MotherBoard mb;

    public VirtualClock(MotherBoard mb) {
        this.mb = mb;
        Thread thread = new Thread(this, this.getClass().getName() + " working thread");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        try {
            PC pc = mb.pc;
            SimpleInterruptController pic = mb.pic;
            long start = System.currentTimeMillis();
            double m = 0x001800b0 / 24.0 / 3600;
            PCState state;
            long c1 = 0;
            long c2;
            while ((state = pc.getState()) != null) {
                if (state == PCState.Running) {
                    c2 = Math.round((System.currentTimeMillis() - start) / m);
                    if (c2 > c1) {
                        pic.setIRQ(0, 0);
                        pic.setIRQ(0, 1);
                    }
                    c1 = c2;
                }
                Thread.sleep(20);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            DummyDebugger.getInstance().onMessage(WARN, "%s exited!\n", Thread.currentThread().getName());
        }
    }

    public static long IPS = 25000000;// Option.ips.intValue(25000000);
    private static final boolean DEBUG = false;
    private static final DummyDebugger LOGGING = DummyDebugger.getLogger(VirtualClock.class.getName());

    private PriorityQueue<Timer> timers;
    private volatile boolean ticksEnabled;
    private long ticksOffset;
    private long ticksStatic;
    private long totalTicks = 0; // emulated cycles, monotonically increasing
    private long totalEmulatedNanos = 0; // emulated nanos, monotonically increasing
    private static final boolean REAL_TIME = true;// !Option.deterministic.isSet(); //sync clock with real clock by default

    //required for tracking real time
    private static final long MIN_SLEEP_NANOS = 10000000L; // 10 milli-seconds
    private long nanosToSleep = 0;
    private long nextRateCheckTicks = 0;
    private long lastRealNanos;
    private long lastTotalTicks;
    private static final long RATE_CHECK_INTERVAL = 2 * 1000000;

    public VirtualClock() {
        timers = new PriorityQueue<Timer>(20);
        ticksEnabled = false;
        ticksOffset = 0;
        ticksStatic = 0;
    }

    public void saveState(DataOutput output) throws IOException {
        output.writeBoolean(ticksEnabled);
        output.writeLong(ticksOffset);
        output.writeLong(getTime());
    }

    public void loadState(DataInput input, PC pc) throws IOException {
        ticksEnabled = input.readBoolean();
        ticksOffset = input.readLong();
        ticksStatic = input.readLong();
    }

    public synchronized Timer newTimer(TimerResponsive object) {
        Timer tempTimer = new Timer(object, this);
        return tempTimer;
    }

    private boolean process() {
        Timer tempTimer;
        tempTimer = timers.peek();
        if ((tempTimer == null) || !tempTimer.check(getTime()))
            return false;
        else
            return true;
    }

    public synchronized void update(Timer object) {
        timers.remove(object);
        if (object.enabled()) {
            timers.offer(object);
        }
    }

    public long getTime() {
        if (ticksEnabled) {
            return this.getRealTime() + ticksOffset;
        } else {
            return ticksStatic;
        }
    }

    public long getIPS() {
        return IPS;
    }

    private long getRealTime() {
        return getEmulatedNanos();
    }

    public long getRealMillis() {
        return getEmulatedNanos() / 1000000;
    }

    public long getEmulatedMicros() {
        return getEmulatedNanos() / 1000;
    }

    public long getEmulatedNanos() {
        if (REAL_TIME)
            return totalEmulatedNanos + convertTicksToNanos(totalTicks - lastTotalTicks);
        return (long) (((double) totalTicks) * 1000000000 / IPS);
    }

    public long getTickRate() {
        return 1000000000L; // nano seconds
    }

    public long getTicks() {
        return totalTicks;
    }

    public void pause() {
        if (ticksEnabled) {
            ticksStatic = getTime();
            ticksEnabled = false;
        }
    }

    public void resume() {
        if (!ticksEnabled) {
            ticksOffset = ticksStatic - getRealTime();
            ticksEnabled = true;
            lastRealNanos = System.nanoTime();
            lastTotalTicks = getTicks();
            nextRateCheckTicks = lastTotalTicks + RATE_CHECK_INTERVAL;
        }
    }

    public void reset() {
        this.pause();
        ticksOffset = 0;
        ticksStatic = 0;
    }

    public String toString() {
        return "Virtual Clock";
    }

    public void updateNowAndProcess(boolean sleep) {
        if (REAL_TIME) {
            Timer tempTimer;
            synchronized (this) {
                tempTimer = timers.peek();
            }
            long expiry = tempTimer.getExpiry();
            long now = getEmulatedNanos();
            long nanoDelay = expiry - now;
            if (nanoDelay > 0) {
                nanosToSleep += nanoDelay;
                if (nanosToSleep > MIN_SLEEP_NANOS) // don't waste time with loads of tiny sleeps (eg. mixer)
                {
                    try {
                        if (DEBUG)
                            System.out.printf("Halt: sleep for %d millis %d nanos...\n", (nanosToSleep) / 1000000L, nanosToSleep % 1000000);
                        if (nanosToSleep > 100000000)
                            nanosToSleep = 100000000L;
                        Thread.sleep(nanosToSleep / 1000000L, (int) (nanosToSleep % 1000000));

                    } catch (InterruptedException ex) {
                        DummyDebugger.getLogger(VirtualClock.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    nanosToSleep = 0;
                }
                totalTicks += convertNanosToTicks(nanoDelay) + 1; // only place where ticks gets out of sync with number of instructions
            }
            if (!tempTimer.check(getEmulatedNanos()))
                throw new IllegalStateException("Should have forced interrupt!");
        } else {
            Timer tempTimer;
            synchronized (this) {
                tempTimer = timers.peek();
            }
            long expiry = tempTimer.getExpiry();
            if (sleep)
                try {
                    long toSleep = Math.min((expiry - getTime()) / 1000000, 100);
                    //                    System.out.printf("Sleeping for %x millis", toSleep);
                    Thread.sleep(toSleep);
                } catch (InterruptedException ex) {
                    DummyDebugger.getLogger(VirtualClock.class.getName()).log(Level.SEVERE, null, ex);
                }
            // cast time difference to microseconds, then convert to cycles
            totalTicks = (long) ((double) expiry * IPS / getTickRate());//totalTicks += ((expiry - getEmulatedNanos())/1000)*1000 * IPS / getTickRate();
            if (totalTicks < 0) {
                System.out.println(printTimerQueue());
                throw new IllegalStateException("Time cannot be negative! expiry=" + expiry + ", tick rate=" + getTickRate() + ", IPS=" + IPS);
            }
            if ((expiry * IPS) % getTickRate() != 0)
                totalTicks++;
            if (!tempTimer.check(getTime()))
                throw new IllegalStateException("Should have forced interrupt!");
        }
    }

    public void updateAndProcess(int instructions) {
        update(instructions);
        process();
    }

    public long convertNanosToTicks(long nanos) {
        return (long) (((double) nanos) * IPS / 1000000000);
        //        return nanos * IPS / 1000000000L;
    }

    public long convertTicksToNanos(long ticks) {
        return (long) (((double) ticks) * 1000000000 / IPS);
        //        return ticks * 1000000000L / IPS;
    }

    public void update(int instructions) {
        totalTicks += instructions;
        if ((REAL_TIME) && (totalTicks > nextRateCheckTicks)) {
            long realNanosDelta = System.nanoTime() - lastRealNanos;
            long emulatedNanosDelta = convertTicksToNanos(totalTicks - lastTotalTicks);
            nextRateCheckTicks += RATE_CHECK_INTERVAL;
            if (Math.abs(emulatedNanosDelta - realNanosDelta) > 100000) {
                totalEmulatedNanos += emulatedNanosDelta;
                lastRealNanos += realNanosDelta;
                lastTotalTicks = totalTicks;
                changeTimeRate(((double) realNanosDelta / emulatedNanosDelta));
            }
        }
    }

    private void changeTimeRate(double factor) {
        if (DEBUG)
            System.out.printf("Changing speed from %.1fMHz to ", ((float) (IPS / 100000)) / 10);
        if (factor > 1.02)
            factor = 1.02;
        else if (factor < 0.98)
            factor = 0.98;
        IPS /= factor;
        if (DEBUG) {
            System.out.printf("%.1fMHz.\n", ((float) (IPS / 100000)) / 10);
            System.out.printf("Clock: IPS:%d time:%d next Exp:%d\n", IPS, getEmulatedNanos(), nextExpiry());
            System.out.println(printTimerQueue());
        }
    }

    public long nextExpiry() {
        Timer tempTimer;
        tempTimer = timers.peek();
        if (tempTimer == null)
            return Long.MAX_VALUE;
        return tempTimer.getExpiry();
    }

    public long ticksToNanos(long ticks) {
        return (long) ((double) ticks * 1000000000 / getIPS());
    }

    public String printTimerQueue() {
        StringBuilder b = new StringBuilder();
        int n = timers.size();
        List<Timer> all = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            Timer t = timers.poll();
            all.add(t);
            b.append(String.format("Timer class: %70s expiry %020d\n", t.callback.getClass(), t.getExpiry()));
        }
        timers.addAll(all);
        return b.toString();
    }

    // Only used to force interupts at certain times
    public void setNextPitExpiry(long ticks) {
        Timer pit = timers.poll();
        PriorityQueue<Timer> tmp = new PriorityQueue<Timer>(timers.size());
        while (!(pit.callback instanceof IntervalTimer.TimerChannel)) {
            tmp.add(pit);
            if (timers.isEmpty())
                throw new IllegalStateException("PIT timer not set!");
            pit = timers.poll();
        }
        pit.setExpiry(ticksToNanos(ticks));
        timers.addAll(tmp);
    }

}
