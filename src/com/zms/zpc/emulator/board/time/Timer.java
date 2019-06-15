package com.zms.zpc.emulator.board.time;

import java.io.*;

/**
 * Created by 张小美 on 2019-06-15.
 * Copyright 2002-2016
 */
public class Timer implements Comparable {

    private long expireTime;
    public final TimerResponsive callback;
    private boolean enabled;
    private Clock myOwner;

    /**
     * Constructs a <code>Timer</code> which fires events on the specified
     * <code>TimerReponsive</code> object using the specified <code>Clock</code>
     * object as a time-source.
     * <p>
     * The constructed timer is initially disabled.
     *
     * @param target object on which to fire callbacks.
     * @param parent time-source used to test expiry.
     */
    public Timer(TimerResponsive target, Clock parent) {
        myOwner = parent;
        callback = target;
        enabled = false;
    }

    public void saveState(DataOutput output) throws IOException {
        output.writeLong(expireTime);
        output.writeBoolean(enabled);
    }

    public void loadState(DataInput input) throws IOException {
        setExpiry(input.readLong());
        setStatus(input.readBoolean());
    }

    public int getType() {
        return callback.getType();
    }

    /**
     * Returns <code>true</code> if this timer will expire at some point in the
     * future.
     *
     * @return <code>true</code> if this timer is enabled.
     */
    public synchronized boolean enabled() {
        return enabled;
    }

    /**
     * Disables this timer.  Following a call to <code>disable</code> the timer
     * cannot ever fire again unless a call is made to <code>setExpiry</code>
     */
    public synchronized void disable() {
        setStatus(false);
    }

    /**
     * Sets the expiry time for and enables this timer.
     * <p>
     * No restrictions are set on the value of the expiry time.  Times in the past
     * will fire a callback at the next check.  Times in the future will fire on
     * the first call to check after their expiry time has passed.  Time units are
     * decided by the implementation of <code>Clock</code> used by this timer.
     *
     * @param time absolute time of expiry for this timer.
     */
    public synchronized void setExpiry(long time) {
        expireTime = time;
        setStatus(true);
    }

    /**
     * Returns <code>true</code> and fires the targets callback method if this timer is enabled
     * and its expiry time is earlier than the supplied time.
     *
     * @param time value of time to check against.
     * @return <code>true</code> if timer had expired and callback was fired.
     */
    public synchronized boolean check(long time) {
        if (this.enabled && (time >= expireTime)) {
            disable();
            callback.callback();
            return true;
        } else
            return false;
    }

    private void setStatus(boolean status) {
        enabled = status;
        myOwner.update(this);
    }

    public long getExpiry() {
        return expireTime;
    }

    public int compareTo(Object o) {
        if (!(o instanceof Timer))
            return -1;

        if (getExpiry() - ((Timer) o).getExpiry() < 0)
            return -1;
        else if ((getExpiry() - ((Timer) o).getExpiry() == 0) && (callback == ((Timer) o).callback))
            return 0;
        else
            return 1;
    }

    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (int) (this.expireTime ^ (this.expireTime >>> 32));
        hash = 67 * hash + (this.enabled ? 1 : 0);
        return hash;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Timer))
            return false;

        Timer t = (Timer) o;

        return (t.enabled() == enabled()) && (t.getExpiry() == getExpiry()) && (t.callback == callback);
    }

}
