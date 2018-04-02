package edu.stanford.nlp.time.temporal;

import edu.stanford.nlp.time.SUTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Partial;

// Time with a range (most times have a range...)
public class TimeWithRange extends Time {

    private Range range; // guess at range

    public TimeWithRange(TimeWithRange t, Range range) {
        super(t);
        this.range = range;
    }

    public TimeWithRange(Range range) {
        this.range = range;
    }

    @Override
    public TimeWithRange setTimeZone(DateTimeZone tz) {
        return new TimeWithRange(this, (Range) Temporal.setTimeZone(range, tz));
    }

    @Override
    public Duration getDuration() {
        if (range != null)
            return range.getDuration();
        else
            return null;
    }

    @Override
    public Range getRange(int flags, Duration granularity) {
        if (range != null) {
            return range.getRange(flags, granularity);
        } else {
            return null;
        }
    }

    @Override
    public Time add(Duration offset) {
        // TODO: Check logic
//      if (getTimeLabel() != null) {
        if (getStandardTemporalType() != null) {
            // Time has some meaning, keep as is
            return new RelativeTime(this, SUTime.TemporalOp.OFFSET_EXACT, offset);
        } else
            return new TimeWithRange(this, range.offset(offset, 0));
    }

    @Override
    public Time intersect(Time t) {
        if (t == null || t == SUTime.TIME_UNKNOWN)
            return this;
        if (t instanceof CompositePartialTime) {
            return t.intersect(this);
        } else if (t instanceof PartialTime) {
            return t.intersect(this);
        } else if (t instanceof GroundedTime) {
            return t.intersect(this);
        } else {
            return new TimeWithRange((Range) range.intersect(t));
        }
    }

    @Override
    public Time resolve(Time refTime, int flags) {
        CompositePartialTime cpt = makeComposite(new PartialTime(new Partial()), this);
        if (cpt != null) {
            return cpt.resolve(refTime, flags);
        }
        Range groundedRange = null;
        if (range != null) {
            groundedRange = range.resolve(refTime, flags).getRange();
        }
        return SUTime.createTemporal(standardTemporalType, timeLabel, new TimeWithRange(this, groundedRange));
        //return new TimeWithRange(this, groundedRange);
    }

    @Override
    public String toFormattedString(int flags) {
        if (getTimeLabel() != null) {
            return getTimeLabel();
        }
        if ((flags & SUTime.FORMAT_TIMEX3_VALUE) != 0) {
            flags |= SUTime.FORMAT_ISO;
        }
        return range.toFormattedString(flags);
    }

    private static final long serialVersionUID = 1;
}
