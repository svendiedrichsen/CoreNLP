package edu.stanford.nlp.time.temporal;

import edu.stanford.nlp.time.SUTime;

/**
 * Reference time (some kind of reference time).
 */
public class RefTime extends Time {

    private String label;

    public RefTime(String label) {
        this.label = label;
    }

    public RefTime(SUTime.StandardTemporalType timeType, String timeLabel, String label) {
        this.standardTemporalType = timeType;
        this.timeLabel = timeLabel;
        this.label = label;
    }

    @Override
    public boolean isRef() {
        return true;
    }

    @Override
    public String toFormattedString(int flags) {
        if (getTimeLabel() != null) {
            return getTimeLabel();
        }
        if ((flags & SUTime.FORMAT_ISO) != 0) {
            return null;
        } // TODO: is there iso standard?
        return label;
    }

    @Override
    public Time add(Duration offset) {
        return new RelativeTime(this, SUTime.TemporalOp.OFFSET_EXACT, offset);
    }

    @Override
    public Time offset(Duration offset, int offsetFlags) {
        if ((offsetFlags & SUTime.RELATIVE_OFFSET_INEXACT) != 0) {
            return new RelativeTime(this, SUTime.TemporalOp.OFFSET, offset);
        } else {
            return new RelativeTime(this, SUTime.TemporalOp.OFFSET_EXACT, offset);
        }
    }

    @Override
    public Time resolve(Time refTime, int flags) {
        if (this == SUTime.TIME_REF) {
            return refTime;
        } else if (this == SUTime.TIME_NOW && (flags & SUTime.RESOLVE_NOW) != 0) {
            return refTime;
        } else {
            return this;
        }
    }

    private static final long serialVersionUID = 1;
}
