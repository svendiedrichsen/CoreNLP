package edu.stanford.nlp.time.temporal;

import edu.stanford.nlp.time.JodaTimeUtils;
import edu.stanford.nlp.time.SUTime;
import org.joda.time.*;

public class GroundedTime extends Time {

    private static final long serialVersionUID = 1;

    // Represents an absolute time
    private ReadableInstant base;

    public GroundedTime(Time p, ReadableInstant base) {
        super(p);
        this.base = base;
    }

    public GroundedTime(ReadableInstant base) {
        this.base = base;
    }

    @Override
    public GroundedTime setTimeZone(DateTimeZone tz) {
        MutableDateTime tzBase = base.toInstant().toMutableDateTime();
        tzBase.setZone(tz);           // TODO: setZoneRetainFields?
        return new GroundedTime(this, tzBase);
    }

    @Override
    public boolean hasTime() {
        return true;
    }

    @Override
    public boolean isGrounded() {
        return true;
    }

    @Override
    public Duration getDuration() {
        return SUTime.DURATION_NONE;
    }

    @Override
    public Range getRange(int flags, Duration granularity) {
        return new Range(this, this);
    }

    @Override
    public String toFormattedString(int flags) {
        return base.toString();
    }

    @Override
    public Time resolve(Time refTime, int flags) {
        return this;
    }

    @Override
    public Time add(Duration offset) {
        Period p = offset.getJodaTimePeriod();
        GroundedTime g = new GroundedTime(base.toInstant().withDurationAdded(p.toDurationFrom(base), 1));
        g.approx = this.approx;
        g.mod = this.mod;
        return g;
    }

    @Override
    public Time intersect(Time t) {
        if (t.getRange().contains(this.getRange())) {
            return this;
        } else {
            return null;
        }
    }

    @Override
    public Temporal intersect(Temporal other) {
        if (other == null)
            return this;
        if (other == SUTime.TIME_UNKNOWN)
            return this;
        if (other.getRange().contains(this.getRange())) {
            return this;
        } else {
            return null;
        }
    }

    @Override
    public Instant getJodaTimeInstant() {
        return base.toInstant();
    }

    @Override
    public Partial getJodaTimePartial() {
        return JodaTimeUtils.getPartial(base.toInstant(), JodaTimeUtils.EMPTY_ISO_PARTIAL);
    }

}
