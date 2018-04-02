package edu.stanford.nlp.time.temporal;

import edu.stanford.nlp.time.JodaTimeUtils;
import edu.stanford.nlp.time.SUTime;
import edu.stanford.nlp.util.FuzzyInterval;
import org.joda.time.*;

/**
 * A Duration represents a period of time (without endpoints).
 * <br>
 * We have 3 types of durations:
 * <ol>
 * <li> DurationWithFields - corresponds to JodaTime Period,
 * where we have fields like hours, weeks, etc </li>
 * <li> DurationWithMillis -
 * corresponds to JodaTime Duration, where the duration is specified in millis
 * this gets rid of certain ambiguities such as a month with can be 28, 30, or
 * 31 days </li>
 * <li>InexactDuration - duration that is under determined (like a few
 * days)</li>
 * </ol>
 */
public abstract class Duration extends Temporal implements FuzzyInterval.FuzzyComparable<Duration> {

    private static final long serialVersionUID = 1;

    public Duration() {
    }

    public Duration(Duration d) {
        super(d);
    }

    public static Duration getDuration(ReadablePeriod p) {
        return new DurationWithFields(p);
    }

    public static Duration getDuration(org.joda.time.Duration d) {
        return new DurationWithMillis(d);
    }

    public static Duration getInexactDuration(ReadablePeriod p) {
        return new InexactDuration(p);
    }

    public static Duration getInexactDuration(org.joda.time.Duration d) {
        return new InexactDuration(d.toPeriod());
    }

    // Returns the inexact version of the duration
    public InexactDuration makeInexact() {
        return new InexactDuration(getJodaTimePeriod());
    }

    public DateTimeFieldType[] getDateTimeFields() {
        return null;
    }

    @Override
    public boolean isGrounded() {
        return false;
    }

    @Override
    public Time getTime() {
        return null;
    } // There is no time associated with a duration?

    public Time toTime(Time refTime) {
        return toTime(refTime, 0);
    }

    public Time toTime(Time refTime, int flags) {
        // if ((flags & (DUR_RESOLVE_FROM_AS_REF | DUR_RESOLVE_TO_AS_REF)) == 0)
        {
            Partial p = refTime.getJodaTimePartial();
            if (p != null) {
                // For durations that have corresponding date time fields
                // this = current time without more specific fields than the duration
                DateTimeFieldType[] dtFieldTypes = getDateTimeFields();
                if (dtFieldTypes != null) {
                    Time t = null;
                    for (DateTimeFieldType dtft : dtFieldTypes) {
                        if (p.isSupported(dtft)) {
                            t = new PartialTime(JodaTimeUtils.discardMoreSpecificFields(p, dtft));
                        }
                    }
                    if (t == null) {
                        Instant instant = refTime.getJodaTimeInstant();
                        if (instant != null) {
                            for (DateTimeFieldType dtft : dtFieldTypes) {
                                if (instant.isSupported(dtft)) {
                                    Partial p2 = JodaTimeUtils.getPartial(instant, p.with(dtft, 1));
                                    t = new PartialTime(JodaTimeUtils.discardMoreSpecificFields(p2, dtft));
                                }
                            }
                        }
                    }
                    if (t != null) {
                        if ((flags & SUTime.RESOLVE_TO_PAST) != 0) {
                            // Check if this time is in the past, if not, subtract duration
                            if (t.compareTo(refTime) >= 0) {
                                return t.subtract(this);
                            }
                        } else if ((flags & SUTime.RESOLVE_TO_FUTURE) != 0) {
                            // Check if this time is in the future, if not, subtract
                            // duration
                            if (t.compareTo(refTime) <= 0) {
                                return t.add(this);
                            }
                        }
                    }
                    return t;
                }
            }
        }
        Time minTime = refTime.subtract(this);
        Time maxTime = refTime.add(this);
        Range likelyRange = null;
        if ((flags & (SUTime.DUR_RESOLVE_FROM_AS_REF | SUTime.RESOLVE_TO_FUTURE)) != 0) {
            likelyRange = new Range(refTime, maxTime, this);
        } else if ((flags & (SUTime.DUR_RESOLVE_TO_AS_REF | SUTime.RESOLVE_TO_PAST)) != 0) {
            likelyRange = new Range(minTime, refTime, this);
        } else {
            Duration halfDuration = this.divideBy(2);
            likelyRange = new Range(refTime.subtract(halfDuration), refTime.add(halfDuration), this);
        }
        return new TimeWithRange(likelyRange);
//      if ((flags & (RESOLVE_TO_FUTURE | RESOLVE_TO_PAST)) != 0) {
//        return new TimeWithRange(likelyRange);
//      }
//      Range r = new Range(minTime, maxTime, this.multiplyBy(2));
//      return new InexactTime(new TimeWithRange(likelyRange), this, r);
    }

    @Override
    public Duration getDuration() {
        return this;
    }

    @Override
    public Range getRange(int flags, Duration granularity) {
        return new Range(null, null, this);
    } // Unanchored range

    @Override
    public SUTime.TimexType getTimexType() {
        return SUTime.TimexType.DURATION;
    }

    public abstract Period getJodaTimePeriod();

    public abstract org.joda.time.Duration getJodaTimeDuration();

    @Override
    public String toFormattedString(int flags) {
        if (getTimeLabel() != null) {
            return getTimeLabel();
        }
        Period p = getJodaTimePeriod();
        String s = (p != null) ? p.toString() : "PXX";
        if ((flags & (SUTime.FORMAT_ISO | SUTime.FORMAT_TIMEX3_VALUE)) == 0) {
            String m = getMod();
            if (m != null) {
                try {
                    SUTime.TimexMod tm = SUTime.TimexMod.valueOf(m);
                    if (tm.getSymbol() != null) {
                        s = tm.getSymbol() + s;
                    }
                } catch (Exception ex) {
                }
            }
        }
        return s;
    }

    @Override
    public Duration getPeriod() {
  /*    TimeLabel tl = getTimeLabel();
      if (tl != null) {
        return tl.getPeriod();
      } */
        SUTime.StandardTemporalType tlt = getStandardTemporalType();
        if (tlt != null) {
            return tlt.getPeriod();
        }
        return this;
    }

    // Rough approximate ordering of durations
    @Override
    public int compareTo(Duration d) {
        org.joda.time.Duration d1 = getJodaTimeDuration();
        org.joda.time.Duration d2 = d.getJodaTimeDuration();
        if (d1 == null && d2 == null) {
            return 0;
        } else if (d1 == null) {
            return 1;
        } else if (d2 == null) {
            return -1;
        }

        int cmp = d1.compareTo(d2);
        if (cmp == 0) {
            if (d.isApprox() && !this.isApprox()) {
                // Put exact in front of approx
                return -1;
            } else if (!d.isApprox() && this.isApprox()) {
                return 1;
            } else {
                return 0;
            }
        } else {
            return cmp;
        }
    }

    @Override
    public boolean isComparable(Duration d) {
        // TODO: When is two durations comparable?
        return true;
    }

    // Operations with durations
    public abstract Duration add(Duration d);

    public abstract Duration multiplyBy(int m);

    public abstract Duration divideBy(int m);

    public Duration subtract(Duration d) {
        return add(d.multiplyBy(-1));
    }

    @Override
    public Duration resolve(Time refTime, int flags) {
        return this;
    }

    @Override
    public Temporal intersect(Temporal t) {
        if (t == null)
            return this;
        if (t == SUTime.TIME_UNKNOWN || t == SUTime.DURATION_UNKNOWN)
            return this;
        if (t instanceof Time) {
            RelativeTime rt = new RelativeTime((Time) t, SUTime.TemporalOp.INTERSECT, this);
            rt = (RelativeTime) rt.addMod(this.getMod());
            return rt;
        } else if (t instanceof Range) {
            // return new TemporalSet(t, TemporalOp.INTERSECT, this);
        } else if (t instanceof Duration) {
            Duration d = (Duration) t;
            return intersect(d);
        }
        return null;
    }

    public Duration intersect(Duration d) {
        if (d == null || d == SUTime.DURATION_UNKNOWN)
            return this;
        int cmp = compareTo(d);
        if (cmp < 0) {
            return this;
        } else {
            return d;
        }
    }

    public static Duration min(Duration d1, Duration d2) {
        if (d2 == null)
            return d1;
        if (d1 == null)
            return d2;
        if (d1.isComparable(d2)) {
            int c = d1.compareTo(d2);
            return (c < 0) ? d1 : d2;
        }
        return d1;
    }

    public static Duration max(Duration d1, Duration d2) {
        if (d1 == null)
            return d2;
        if (d2 == null)
            return d1;
        if (d1.isComparable(d2)) {
            int c = d1.compareTo(d2);
            return (c >= 0) ? d1 : d2;
        }
        return d2;
    }

}
