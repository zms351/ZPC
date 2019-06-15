package com.zms.zpc.emulator.board.time;

/**
 * Created by 张小美 on 2019-06-15.
 * Copyright 2002-2016
 */
public interface Clock {

    public void update(int instructions);

    public void updateAndProcess(int instructions);

    public void updateNowAndProcess(boolean sleep);

    public long getTicks();

    public long getEmulatedNanos();

    public long getEmulatedMicros();

    public long getRealMillis();

    /**
     * @return tick rate per second
     */
    public long getTickRate();

    public long getIPS();

    /**
     * Constructs a new <code>Timer</code> which will fire <code>callback</code>
     * on the given object when the timer expires.
     *
     * @param object callback object
     * @return <code>Timer</code> instance
     */
    public Timer newTimer(TimerResponsive object);

    /**
     * Update the internal state of this clock to account for the change in
     * state of the supplied child <code>Timer</code>.
     *
     * @param object timer whose state has changed
     */
    void update(Timer object);

    /**
     * Pauses this clock instance.  Does nothing if this clock is already paused.
     */
    public void pause();

    /**
     * Resumes this clock instance.  Does nothing if this clock is already running.
     */
    public void resume();

}
