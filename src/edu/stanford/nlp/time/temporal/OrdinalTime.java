package edu.stanford.nlp.time.temporal;

import edu.stanford.nlp.time.SUTime;

import java.util.List;

/**
 * The nth temporal.
 * Example: The tenth week (of something, don't know yet)
 * The second friday
 */
public class OrdinalTime extends Time {

    private static final long serialVersionUID = 1;

    private Temporal base;
    private int n;

    public OrdinalTime(Temporal base, int n) {
        this.base = base;
        this.n = n;
    }

    public OrdinalTime(Temporal base, long n) {
        this.base = base;
        this.n = (int) n;
    }

    @Override
    public Time add(Duration offset) {
        return new RelativeTime(this, SUTime.TemporalOp.OFFSET_EXACT, offset);
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
        if (base != null) {
            String str = base.toFormattedString(flags);
            if (str != null) {
                return str + "-#" + n;
            }
        }
        return null;
    }

    @Override
    public Time intersect(Time t) {
        if (base instanceof PartialTime && t instanceof PartialTime) {
            return new OrdinalTime(base.intersect(t), n);
        } else {
            return new RelativeTime(t, SUTime.TemporalOp.INTERSECT, this);
        }
    }

    @Override
    public Time resolve(Time t, int flags) {
        if (t == null) return this; // No resolving to be done?
        if (base instanceof PartialTime) {
            PartialTime pt = (PartialTime) base.resolve(t, flags);
            List<Temporal> list = pt.toList();
            if (list != null && list.size() >= n) {
                return list.get(n - 1).getTime();
            }
        } else if (base instanceof Duration) {
            Duration d = ((Duration) base).multiplyBy(n - 1);
            Time temp = t.getRange().begin();
            return temp.offset(d, 0).reduceGranularityTo(d.getDuration());
        }
        return this;
    }

} // end static class OrdinalTim
