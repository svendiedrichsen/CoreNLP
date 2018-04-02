package edu.stanford.nlp.time.temporal;

import edu.stanford.nlp.time.JodaTimeUtils;
import edu.stanford.nlp.time.SUTime;
import edu.stanford.nlp.util.Pair;
import org.joda.time.*;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

// Composite time - like PartialTime but with more, approximate fields
public class CompositePartialTime extends PartialTime {

    private static final long serialVersionUID = 1;

    // Summer weekend morning in June
    Time tod; // Time of day
    Time dow; // Day of week
    Time poy; // Part of year

    // Duration duration; // Underspecified time (like day in June)

    public CompositePartialTime(PartialTime t, Time poy, Time dow, Time tod) {
        super(t);
        this.poy = poy;
        this.dow = dow;
        this.tod = tod;
    }

    public CompositePartialTime(PartialTime t, Partial p, Time poy, Time dow, Time tod) {
        this(t, poy, dow, tod);
        this.base = p;
    }

    @Override
    public Instant getJodaTimeInstant() {
        Partial p = base;
        if (tod != null) {
            Partial p2 = tod.getJodaTimePartial();
            if (p2 != null && JodaTimeUtils.isCompatible(p, p2)) {
                p = JodaTimeUtils.combine(p, p2);
            }
        }
        if (dow != null) {
            Partial p2 = dow.getJodaTimePartial();
            if (p2 != null && JodaTimeUtils.isCompatible(p, p2)) {
                p = JodaTimeUtils.combine(p, p2);
            }
        }
        if (poy != null) {
            Partial p2 = poy.getJodaTimePartial();
            if (p2 != null && JodaTimeUtils.isCompatible(p, p2)) {
                p = JodaTimeUtils.combine(p, p2);
            }
        }
        return JodaTimeUtils.getInstant(p);
    }

    @Override
    public Duration getDuration() {
/*      TimeLabel tl = getTimeLabel();
      if (tl != null) {
        return tl.getDuration();
      } */
        SUTime.StandardTemporalType tlt = getStandardTemporalType();
        if (tlt != null) {
            return tlt.getDuration();
        }

        Duration bd = (base != null) ? Duration.getDuration(JodaTimeUtils.getJodaTimePeriod(base)) : null;
        if (tod != null) {
            Duration d = tod.getDuration();
            return (bd.compareTo(d) < 0) ? bd : d;
        }
        if (dow != null) {
            Duration d = dow.getDuration();
            return (bd.compareTo(d) < 0) ? bd : d;
        }
        if (poy != null) {
            Duration d = poy.getDuration();
            return (bd.compareTo(d) < 0) ? bd : d;
        }
        return bd;
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

        Duration bd = null;
        if (base != null) {
            DateTimeFieldType mostGeneral = JodaTimeUtils.getMostGeneral(base);
            DurationFieldType df = mostGeneral.getRangeDurationType();
            if (df == null) {
                df = mostGeneral.getDurationType();
            }
            if (df != null) {
                bd = new DurationWithFields(new Period().withField(df, 1));
            }
        }

        if (poy != null) {
            Duration d = poy.getPeriod();
            return (bd.compareTo(d) > 0) ? bd : d;
        }
        if (dow != null) {
            Duration d = dow.getPeriod();
            return (bd.compareTo(d) > 0) ? bd : d;
        }
        if (tod != null) {
            Duration d = tod.getPeriod();
            return (bd.compareTo(d) > 0) ? bd : d;
        }
        return bd;
    }

    private static Range getIntersectedRange(CompositePartialTime cpt, Range r, Duration d) {
        Time beginTime = r.beginTime();
        Time endTime = r.endTime();
        if (beginTime != SUTime.TIME_UNKNOWN && endTime != SUTime.TIME_UNKNOWN) {
            Time t1 = cpt.intersect(r.beginTime());
            if (t1 instanceof PartialTime) {
                ((PartialTime) t1).withStandardFields();
            }
            Time t2 = cpt.intersect(r.endTime());
            if (t2 instanceof PartialTime) {
                ((PartialTime) t2).withStandardFields();
            }
            return new Range(t1, t2, d);
        } else if (beginTime != SUTime.TIME_UNKNOWN && endTime == SUTime.TIME_UNKNOWN) {
            Time t1 = cpt.intersect(r.beginTime());
            if (t1 instanceof PartialTime) {
                ((PartialTime) t1).withStandardFields();
            }
            Time t2 = t1.add(d);
            if (t2 instanceof PartialTime) {
                ((PartialTime) t2).withStandardFields();
            }
            return new Range(t1, t2, d);
        } else {
            throw new RuntimeException("Unsupported range: " + r);
        }
    }

    @Override
    public Range getRange(int flags, Duration granularity) {
        Duration d = getDuration();
        if (tod != null) {
            Range r = tod.getRange(flags, granularity);
            if (r != null) {
                CompositePartialTime cpt = new CompositePartialTime(this, poy, dow, null);
                return getIntersectedRange(cpt, r, d);
            } else {
                return super.getRange(flags, granularity);
            }
        }
        if (dow != null) {
            Range r = dow.getRange(flags, granularity);
            if (r != null) {
                CompositePartialTime cpt = new CompositePartialTime(this, poy, dow, null);
                return getIntersectedRange(cpt, r, d);
            } else {
                return super.getRange(flags, granularity);
            }
        }
        if (poy != null) {
            Range r = poy.getRange(flags, granularity);
            if (r != null) {
                CompositePartialTime cpt = new CompositePartialTime(this, poy, null, null);
                return getIntersectedRange(cpt, r, d);
            } else {
                return super.getRange(flags, granularity);
            }
        }
        return super.getRange(flags, granularity);
    }

    @Override
    public Time intersect(Time t) {
        if (t == null || t == SUTime.TIME_UNKNOWN)
            return this;
        if (base == null)
            return t;
        if (t instanceof PartialTime) {
            Pair<PartialTime, PartialTime> compatible = getCompatible(this, (PartialTime) t);
            if (compatible == null) {
                return null;
            }
            Partial p = JodaTimeUtils.combine(compatible.first.base, compatible.second.base);
            if (t instanceof CompositePartialTime) {
                CompositePartialTime cpt = (CompositePartialTime) t;
                Time ntod = Time.intersect(tod, cpt.tod);
                Time ndow = Time.intersect(dow, cpt.dow);
                Time npoy = Time.intersect(poy, cpt.poy);
                if (ntod == null && (tod != null || cpt.tod != null))
                    return null;
                if (ndow == null && (dow != null || cpt.dow != null))
                    return null;
                if (npoy == null && (poy != null || cpt.poy != null))
                    return null;
                return new CompositePartialTime(this, p, npoy, ndow, ntod);
            } else {
                return new CompositePartialTime(this, p, poy, dow, tod);
            }
        } else {
            return super.intersect(t);
        }
    }

    @Override
    protected PartialTime addSupported(Period p, int scalar) {
        return new CompositePartialTime(this, base.withPeriodAdded(p, 1), poy, dow, tod);
    }

    @Override
    protected PartialTime addUnsupported(Period p, int scalar) {
        return new CompositePartialTime(this, JodaTimeUtils.addForce(base, p, scalar), poy, dow, tod);
    }

    @Override
    public PartialTime reduceGranularityTo(Duration granularity) {
        Partial p = JodaTimeUtils.discardMoreSpecificFields(base,
                JodaTimeUtils.getMostSpecific(granularity.getJodaTimePeriod()));
        return new CompositePartialTime(this, p,
                poy.reduceGranularityTo(granularity),
                dow.reduceGranularityTo(granularity),
                tod.reduceGranularityTo(granularity));
    }

    @Override
    public Time resolve(Time ref, int flags) {
        if (ref == null || ref == SUTime.TIME_UNKNOWN || ref == SUTime.TIME_REF) {
            return this;
        }
        if (this == SUTime.TIME_REF) {
            return ref;
        }
        if (this == SUTime.TIME_UNKNOWN) {
            return this;
        }
        Partial partialRef = ref.getJodaTimePartial();
        if (partialRef == null) {
            throw new UnsupportedOperationException("Cannot resolve if reftime is of class: " + ref.getClass());
        }
        DateTimeFieldType mgf = null;
        if (poy != null)
            mgf = JodaTimeUtils.QuarterOfYear;
        else if (dow != null)
            mgf = DateTimeFieldType.dayOfWeek();
        else if (tod != null)
            mgf = DateTimeFieldType.halfdayOfDay();
        Partial p = (base != null) ? JodaTimeUtils.combineMoreGeneralFields(base, partialRef, mgf) : partialRef;
        if (p.isSupported(DateTimeFieldType.dayOfWeek())) {
            p = JodaTimeUtils.resolveDowToDay(p, partialRef);
        } else if (dow != null) {
            p = JodaTimeUtils.resolveWeek(p, partialRef);
        }
        if (p == base) {
            return this;
        } else {
            return new CompositePartialTime(this, p, poy, dow, tod);
        }
    }

    @Override
    public DateTimeFormatter getFormatter(int flags) {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        boolean hasDate = appendDateFormats(builder, flags);
        if (poy != null) {
            if (!JodaTimeUtils.hasField(base, DateTimeFieldType.monthOfYear())) {
                // Assume poy is compatible with whatever was built and
                // poy.toISOString() does the correct thing
                builder.appendLiteral("-");
                builder.appendLiteral(poy.toISOString());
                hasDate = true;
            }
        }
        if (dow != null) {
            if (!JodaTimeUtils.hasField(base, DateTimeFieldType.monthOfYear()) && !JodaTimeUtils.hasField(base, DateTimeFieldType.dayOfWeek())) {
                builder.appendLiteral("-");
                builder.appendLiteral(dow.toISOString());
                hasDate = true;
            }
        }
        if (hasTime()) {
            if (!hasDate) {
                builder.clear();
            }
            appendTimeFormats(builder, flags);
        } else if (tod != null) {
            if (!hasDate) {
                builder.clear();
            }
            // Assume tod is compatible with whatever was built and
            // tod.toISOString() does the correct thing
            builder.appendLiteral("T");
            builder.appendLiteral(tod.toISOString());
        }
        return builder.toFormatter();
    }

    @Override
    public SUTime.TimexType getTimexType() {
        if (tod != null) return SUTime.TimexType.TIME;
        return super.getTimexType();
    }

}
