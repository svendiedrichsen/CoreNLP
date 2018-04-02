package edu.stanford.nlp.time.temporal;

import org.joda.time.Period;
import org.joda.time.ReadableDuration;

/**
 * Duration specified in terms of milliseconds.
 */
public class DurationWithMillis extends Duration {

    private static final long serialVersionUID = 1;

    private final ReadableDuration base;

    public DurationWithMillis(long ms) {
        this.base = new org.joda.time.Duration(ms);
    }

    public DurationWithMillis(ReadableDuration base) {
        this.base = base;
    }

    public DurationWithMillis(Duration d, ReadableDuration base) {
        super(d);
        this.base = base;
    }

    @Override
    public Duration multiplyBy(int m) {
        if (m == 1) {
            return this;
        } else {
            long ms = base.getMillis();
            return new DurationWithMillis(ms * m);
        }
    }

    @Override
    public Duration divideBy(int m) {
        if (m == 1) {
            return this;
        } else {
            long ms = base.getMillis();
            return new DurationWithMillis(ms / m);
        }
    }

    @Override
    public Period getJodaTimePeriod() {
        return base.toPeriod();
    }

    @Override
    public org.joda.time.Duration getJodaTimeDuration() {
        return base.toDuration();
    }

    @Override
    public Duration add(Duration d) {
        if (d instanceof DurationWithMillis) {
            return new DurationWithMillis(this, base.toDuration().plus(((DurationWithMillis) d).base));
        } else if (d instanceof DurationWithFields) {
            return ((DurationWithFields) d).add(this);
        } else {
            throw new UnsupportedOperationException("Unknown duration type in add: " + d.getClass());
        }
    }

}
