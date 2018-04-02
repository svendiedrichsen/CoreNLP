package edu.stanford.nlp.time.temporal;

import edu.stanford.nlp.time.SUTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.Partial;

/**
 * Inexact time, not sure when this is, but have some guesses.
 */
public class InexactTime extends Time {

    private static final long serialVersionUID = 1;

    private Time base; // best guess
    private Duration duration; // how long the time lasts
    private Range range; // guess at range in which the time occurs

    public InexactTime(Partial partial) {
        this.base = new PartialTime(partial);
        this.range = base.getRange();
        this.approx = true;
    }

    public InexactTime(Time base, Duration duration, Range range) {
        this.base = base;
        this.duration = duration;
        this.range = range;
        this.approx = true;
    }

    public InexactTime(Time base, Range range) {
        this.base = base;
        this.range = range;
        this.approx = true;
    }

    public InexactTime(InexactTime t, Time base, Duration duration, Range range) {
        super(t);
        this.base = base;
        this.duration = duration;
        this.range = range;
        this.approx = true;
    }

    public InexactTime(Range range) {
        this.base = range.mid();
        this.range = range;
        this.approx = true;
    }

    @Override
    public int compareTo(Time t) {
        if (this.base != null) return (this.base.compareTo(t));
        if (this.range != null) {
            if (this.range.begin() != null && this.range.begin().compareTo(t) > 0) return 1;
            else if (this.range.end() != null && this.range.end().compareTo(t) < 0) return -1;
            else return this.range.getTime().compareTo(t);
        }
        return 0;
    }

    @Override
    public InexactTime setTimeZone(DateTimeZone tz) {
        return new InexactTime(this,
                (Time) Temporal.setTimeZone(base, tz), duration,
                (Range) Temporal.setTimeZone(range, tz));
    }

    @Override
    public Time getTime() {
        return this;
    }

    @Override
    public Duration getDuration() {
        if (duration != null)
            return duration;
        if (range != null)
            return range.getDuration();
        else if (base != null)
            return base.getDuration();
        else
            return null;
    }

    @Override
    public Range getRange(int flags, Duration granularity) {
        if (range != null) {
            return range.getRange(flags, granularity);
        } else if (base != null) {
            return base.getRange(flags, granularity);
        } else
            return null;
    }

    @Override
    public Time add(Duration offset) {
        //if (getTimeLabel() != null) {
        if (getStandardTemporalType() != null) {
            // Time has some meaning, keep as is
            return new RelativeTime(this, SUTime.TemporalOp.OFFSET_EXACT, offset);
        } else {
            // Some other time, who know what it means
            // Try to do offset
            return new InexactTime(this, (Time) SUTime.TemporalOp.OFFSET_EXACT.apply(base, offset), duration, (Range) SUTime.TemporalOp.OFFSET_EXACT.apply(range, offset));
        }
    }

    @Override
    public Time resolve(Time refTime, int flags) {
        CompositePartialTime cpt = makeComposite(new PartialTime(this, new Partial()), this);
        if (cpt != null) {
            return cpt.resolve(refTime, flags);
        }
        Time groundedBase = null;
        if (base == SUTime.TIME_REF) {
            groundedBase = refTime;
        } else if (base != null) {
            groundedBase = base.resolve(refTime, flags).getTime();
        }
        Range groundedRange = null;
        if (range != null) {
            groundedRange = range.resolve(refTime, flags).getRange();
        }
      /*    if (groundedRange == range && groundedBase == base) {
            return this;
          } */
        return SUTime.createTemporal(standardTemporalType, timeLabel, mod, new InexactTime(groundedBase, duration, groundedRange));
        //return new InexactTime(groundedBase, duration, groundedRange);
    }

    @Override
    public Instant getJodaTimeInstant() {
        Instant p = null;
        if (base != null) {
            p = base.getJodaTimeInstant();
        }
        if (p == null && range != null) {
            p = range.mid().getJodaTimeInstant();
        }
        return p;
    }

    @Override
    public Partial getJodaTimePartial() {
        Partial p = null;
        if (base != null) {
            p = base.getJodaTimePartial();
        }
        if (p == null && range != null && range.mid() != null) {
            p = range.mid().getJodaTimePartial();
        }
        return p;
    }

    @Override
    public String toFormattedString(int flags) {
        if (getTimeLabel() != null) {
            return getTimeLabel();
        }

        if ((flags & SUTime.FORMAT_ISO) != 0) {
            return null;
        } // TODO: is there iso standard?
        if ((flags & SUTime.FORMAT_TIMEX3_VALUE) != 0) {
            return null;
        } // TODO: is there timex3 standard?
        StringBuilder sb = new StringBuilder();
        sb.append("~(");
        if (base != null) {
            sb.append(base.toFormattedString(flags));
        }
        if (duration != null) {
            sb.append(":");
            sb.append(duration.toFormattedString(flags));
        }
        if (range != null) {
            sb.append(" IN ");
            sb.append(range.toFormattedString(flags));
        }
        sb.append(")");
        return sb.toString();
    }

}
