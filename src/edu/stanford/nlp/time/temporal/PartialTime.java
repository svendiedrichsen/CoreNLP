package edu.stanford.nlp.time.temporal;

import edu.stanford.nlp.time.JodaTimeUtils;
import edu.stanford.nlp.time.SUTime;
import edu.stanford.nlp.util.Pair;
import org.joda.time.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import java.util.ArrayList;
import java.util.List;

// Partial time with Joda Time fields
public class PartialTime extends Time {

    private static final long serialVersionUID = 1;

    // There is typically some uncertainty/imprecision in the time
    protected Partial base; // For representing partial absolute time
    protected DateTimeZone dateTimeZone; // Datetime zone associated with this time

    // private static DateTimeFormatter isoDateFormatter =
    // ISODateTimeFormat.basicDate();
    // private static DateTimeFormatter isoDateTimeFormatter =
    // ISODateTimeFormat.basicDateTimeNoMillis();
    // private static DateTimeFormatter isoTimeFormatter =
    // ISODateTimeFormat.basicTTimeNoMillis();
    // private static DateTimeFormatter isoDateFormatter =
    // ISODateTimeFormat.date();
    // private static DateTimeFormatter isoDateTimeFormatter =
    // ISODateTimeFormat.dateTimeNoMillis();
    // private static DateTimeFormatter isoTimeFormatter =
    // ISODateTimeFormat.tTimeNoMillis();

    public PartialTime(Time t, Partial p) {
        super(t);
        if (t instanceof PartialTime) {
            this.dateTimeZone = ((PartialTime) t).dateTimeZone;
        }
        this.base = p;
    }

    public PartialTime(PartialTime pt) {
        super(pt);
        this.dateTimeZone = pt.dateTimeZone;
        this.base = pt.base;
    }

    // public PartialTime(Partial base, String mod) { this.base = base; this.mod
    // = mod; }
    public PartialTime(Partial base) {
        this.base = base;
    }

    public PartialTime(SUTime.StandardTemporalType temporalType, Partial base) {
        this.base = base;
        this.standardTemporalType = temporalType;
    }

    public PartialTime() {
    }

    @Override
    public PartialTime setTimeZone(DateTimeZone tz) {
        PartialTime tzPt = new PartialTime(this, base);
        tzPt.dateTimeZone = tz;
        return tzPt;
    }

    @Override
    public Instant getJodaTimeInstant() {
        return JodaTimeUtils.getInstant(base);
    }

    @Override
    public Partial getJodaTimePartial() {
        return base;
    }

    @Override
    public boolean hasTime() {
        if (base == null)
            return false;
        DateTimeFieldType sdft = JodaTimeUtils.getMostSpecific(base);
        if (sdft != null && JodaTimeUtils.isMoreGeneral(DateTimeFieldType.dayOfMonth(), sdft, base.getChronology())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public SUTime.TimexType getTimexType() {
        if (base == null) return null;
        return super.getTimexType();
    }

    protected boolean appendDateFormats(DateTimeFormatterBuilder builder, int flags) {
        boolean alwaysPad = ((flags & SUTime.FORMAT_PAD_UNKNOWN) != 0);
        boolean hasDate = true;
        boolean isISO = ((flags & SUTime.FORMAT_ISO) != 0);
        boolean isTimex3 = ((flags & SUTime.FORMAT_TIMEX3_VALUE) != 0);
        // ERA
        if (JodaTimeUtils.hasField(base, DateTimeFieldType.era())) {
            int era = base.get(DateTimeFieldType.era());
            if (era == 0) {
                builder.appendLiteral('-');
            } else if (era == 1) {
                builder.appendLiteral('+');
            }
        }
        // YEAR
        if (JodaTimeUtils.hasField(base, DateTimeFieldType.centuryOfEra()) || JodaTimeUtils.hasField(base, JodaTimeUtils.DecadeOfCentury)
                || JodaTimeUtils.hasField(base, DateTimeFieldType.yearOfCentury())) {
            if (JodaTimeUtils.hasField(base, DateTimeFieldType.centuryOfEra())) {
                builder.appendCenturyOfEra(2, 2);
            } else {
                builder.appendLiteral(SUTime.PAD_FIELD_UNKNOWN2);
            }
            if (JodaTimeUtils.hasField(base, JodaTimeUtils.DecadeOfCentury)) {
                builder.appendDecimal(JodaTimeUtils.DecadeOfCentury, 1, 1);
                builder.appendLiteral(SUTime.PAD_FIELD_UNKNOWN);
            } else if (JodaTimeUtils.hasField(base, DateTimeFieldType.yearOfCentury())) {
                builder.appendYearOfCentury(2, 2);
            } else {
                builder.appendLiteral(SUTime.PAD_FIELD_UNKNOWN2);
            }
        } else if (JodaTimeUtils.hasField(base, DateTimeFieldType.year())) {
            builder.appendYear(4, 4);
        } else if (JodaTimeUtils.hasField(base, DateTimeFieldType.weekyear())) {
            builder.appendWeekyear(4, 4);
        } else {
            builder.appendLiteral(SUTime.PAD_FIELD_UNKNOWN4);
            hasDate = false;
        }
        // Decide whether to include HALF, QUARTER, MONTH/DAY, or WEEK/WEEKDAY
        boolean appendHalf = false;
        boolean appendQuarter = false;
        boolean appendMonthDay = false;
        boolean appendWeekDay = false;
        if (isISO || isTimex3) {
            if (JodaTimeUtils.hasField(base, DateTimeFieldType.monthOfYear()) && JodaTimeUtils.hasField(base, DateTimeFieldType.dayOfMonth())) {
                appendMonthDay = true;
            } else if (JodaTimeUtils.hasField(base, DateTimeFieldType.weekOfWeekyear()) || JodaTimeUtils.hasField(base, DateTimeFieldType.dayOfWeek())) {
                appendWeekDay = true;
            } else if (JodaTimeUtils.hasField(base, DateTimeFieldType.monthOfYear()) || JodaTimeUtils.hasField(base, DateTimeFieldType.dayOfMonth())) {
                appendMonthDay = true;
            } else if (JodaTimeUtils.hasField(base, JodaTimeUtils.QuarterOfYear)) {
                if (!isISO) appendQuarter = true;
            } else if (JodaTimeUtils.hasField(base, JodaTimeUtils.HalfYearOfYear)) {
                if (!isISO) appendHalf = true;
            }
        } else {
            appendHalf = true;
            appendQuarter = true;
            appendMonthDay = true;
            appendWeekDay = true;
        }

        // Half - Not ISO standard
        if (appendHalf && JodaTimeUtils.hasField(base, JodaTimeUtils.HalfYearOfYear)) {
            builder.appendLiteral("-H");
            builder.appendDecimal(JodaTimeUtils.HalfYearOfYear, 1, 1);
        }
        // Quarter  - Not ISO standard
        if (appendQuarter && JodaTimeUtils.hasField(base, JodaTimeUtils.QuarterOfYear)) {
            builder.appendLiteral("-Q");
            builder.appendDecimal(JodaTimeUtils.QuarterOfYear, 1, 1);
        }
        // MONTH
        if (appendMonthDay && (JodaTimeUtils.hasField(base, DateTimeFieldType.monthOfYear()) || JodaTimeUtils.hasField(base, DateTimeFieldType.dayOfMonth()))) {
            hasDate = true;
            builder.appendLiteral('-');
            if (JodaTimeUtils.hasField(base, DateTimeFieldType.monthOfYear())) {
                builder.appendMonthOfYear(2);
            } else {
                builder.appendLiteral(SUTime.PAD_FIELD_UNKNOWN2);
            }
            // Don't indicate day of month if not specified
            if (JodaTimeUtils.hasField(base, DateTimeFieldType.dayOfMonth())) {
                builder.appendLiteral('-');
                builder.appendDayOfMonth(2);
            } else if (alwaysPad) {
                builder.appendLiteral(SUTime.PAD_FIELD_UNKNOWN2);
            }
        }
        if (appendWeekDay && (JodaTimeUtils.hasField(base, DateTimeFieldType.weekOfWeekyear()) || JodaTimeUtils.hasField(base, DateTimeFieldType.dayOfWeek()))) {
            hasDate = true;
            builder.appendLiteral("-W");
            if (JodaTimeUtils.hasField(base, DateTimeFieldType.weekOfWeekyear())) {
                builder.appendWeekOfWeekyear(2);
            } else {
                builder.appendLiteral(SUTime.PAD_FIELD_UNKNOWN2);
            }
            // Don't indicate the day of the week if not specified
            if (JodaTimeUtils.hasField(base, DateTimeFieldType.dayOfWeek())) {
                builder.appendLiteral("-");
                builder.appendDayOfWeek(1);
            }
        }
        return hasDate;
    }

    protected boolean appendTimeFormats(DateTimeFormatterBuilder builder, int flags) {
        boolean alwaysPad = ((flags & SUTime.FORMAT_PAD_UNKNOWN) != 0);
        boolean hasTime = hasTime();
        DateTimeFieldType sdft = JodaTimeUtils.getMostSpecific(base);
        if (hasTime) {
            builder.appendLiteral("T");
            if (JodaTimeUtils.hasField(base, DateTimeFieldType.hourOfDay())) {
                builder.appendHourOfDay(2);
            } else if (JodaTimeUtils.hasField(base, DateTimeFieldType.clockhourOfDay())) {
                builder.appendClockhourOfDay(2);
            } else {
                builder.appendLiteral(SUTime.PAD_FIELD_UNKNOWN2);
            }
            if (JodaTimeUtils.hasField(base, DateTimeFieldType.minuteOfHour())) {
                builder.appendLiteral(":");
                builder.appendMinuteOfHour(2);
            } else if (alwaysPad || JodaTimeUtils.isMoreGeneral(DateTimeFieldType.minuteOfHour(), sdft, base.getChronology())) {
                builder.appendLiteral(":");
                builder.appendLiteral(SUTime.PAD_FIELD_UNKNOWN2);
            }
            if (JodaTimeUtils.hasField(base, DateTimeFieldType.secondOfMinute())) {
                builder.appendLiteral(":");
                builder.appendSecondOfMinute(2);
            } else if (alwaysPad || JodaTimeUtils.isMoreGeneral(DateTimeFieldType.secondOfMinute(), sdft, base.getChronology())) {
                builder.appendLiteral(":");
                builder.appendLiteral(SUTime.PAD_FIELD_UNKNOWN2);
            }
            if (JodaTimeUtils.hasField(base, DateTimeFieldType.millisOfSecond())) {
                builder.appendLiteral(".");
                builder.appendMillisOfSecond(3);
            }
            // builder.append(isoTimeFormatter);
        }
        return hasTime;
    }

    protected DateTimeFormatter getFormatter(int flags) {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        boolean hasDate = appendDateFormats(builder, flags);
        boolean hasTime = hasTime();
        if (hasTime) {
            if (!hasDate) {
                builder.clear();
            }
            appendTimeFormats(builder, flags);
        }
        return builder.toFormatter();
    }

    @Override
    public boolean isGrounded() {
        return false;
    }

    // TODO: compute duration/range => uncertainty of this time
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
        return Duration.getDuration(JodaTimeUtils.getJodaTimePeriod(base));
    }

    @Override
    public Range getRange(int flags, Duration inputGranularity) {
        Duration d = getDuration();
        if (d != null) {
            int padType = (flags & SUTime.RANGE_FLAGS_PAD_MASK);
            Time start = this;
            Duration granularity = inputGranularity;
            switch (padType) {
                case SUTime.RANGE_FLAGS_PAD_NONE:
                    // The most basic range
                    start = this;
                    break;
                case SUTime.RANGE_FLAGS_PAD_AUTO:
                    // More complex range
                    if (hasTime()) {
                        granularity = SUTime.MILLIS;
                    } else {
                        granularity = SUTime.DAY;
                    }
                    start = padMoreSpecificFields(granularity);
                    break;
                case SUTime.RANGE_FLAGS_PAD_FINEST:
                    granularity = SUTime.MILLIS;
                    start = padMoreSpecificFields(granularity);
                    break;
                case SUTime.RANGE_FLAGS_PAD_SPECIFIED:
                    start = padMoreSpecificFields(granularity);
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported pad type for getRange: " + flags);
            }
            if (start instanceof PartialTime) {
                ((PartialTime) start).withStandardFields();
            }
            Time end = start.add(d);
            if (granularity != null) {
                end = end.subtract(granularity);
            }
            return new Range(start, end, d);
        } else {
            return new Range(this, this);
        }
    }

    protected void withStandardFields() {
        if (base.isSupported(DateTimeFieldType.dayOfWeek())) {
            base = JodaTimeUtils.resolveDowToDay(base);
        } else if (base.isSupported(DateTimeFieldType.monthOfYear()) && base.isSupported(DateTimeFieldType.dayOfMonth())) {
            if (base.isSupported(DateTimeFieldType.weekOfWeekyear())) {
                base = base.without(DateTimeFieldType.weekOfWeekyear());
            }
            if (base.isSupported(DateTimeFieldType.dayOfWeek())) {
                base = base.without(DateTimeFieldType.dayOfWeek());
            }
        }
    }

    @Override
    public PartialTime reduceGranularityTo(Duration granularity) {
        Partial pbase = base;
        if (JodaTimeUtils.hasField(granularity.getJodaTimePeriod(), DurationFieldType.weeks())) {
            // Make sure the partial time has weeks in it
            if (!JodaTimeUtils.hasField(pbase, DateTimeFieldType.weekOfWeekyear())) {
                // Add week year to it
                pbase = JodaTimeUtils.resolveWeek(pbase);
            }
        }
        Partial p = JodaTimeUtils.discardMoreSpecificFields(pbase,
                JodaTimeUtils.getMostSpecific(granularity.getJodaTimePeriod()));
        return new PartialTime(this, p);
    }

    public PartialTime padMoreSpecificFields(Duration granularity) {
        Period period = null;
        if (granularity != null) {
            period = granularity.getJodaTimePeriod();
        }
        Partial p = JodaTimeUtils.padMoreSpecificFields(base, period);
        return new PartialTime(this, p);
    }

    @Override
    public String toFormattedString(int flags) {
        if (getTimeLabel() != null) {
            return getTimeLabel();
        }
        String s; // Initialized below
        if (base != null) {
            // String s = ISODateTimeFormat.basicDateTime().print(base);
            // return s.replace('\ufffd', 'X');
            DateTimeFormatter formatter = getFormatter(flags);
            s = formatter.print(base);
        } else {
            s = "XXXX-XX-XX";
        }
        if (dateTimeZone != null) {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("Z");
            formatter = formatter.withZone(dateTimeZone);
            s = s + formatter.print(0);
        }
        return s;
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

        Partial p = (base != null) ? JodaTimeUtils.combineMoreGeneralFields(base, partialRef) : partialRef;
        p = JodaTimeUtils.resolveDowToDay(p, partialRef);

        Time resolved;
        if (p == base) {
            resolved = this;
        } else {
            resolved = new PartialTime(this, p);
            // log.info("Resolved " + this + " to " + resolved + ", ref=" + ref);
        }

        Duration resolvedGranularity = resolved.getGranularity();
        Duration refGranularity = ref.getGranularity();
        // log.info("refGranularity is " + refGranularity);
        // log.info("resolvedGranularity is " + resolvedGranularity);
        if (resolvedGranularity != null && refGranularity != null && resolvedGranularity.compareTo(refGranularity) >= 0) {
            if ((flags & SUTime.RESOLVE_TO_PAST) != 0) {
                if (resolved.compareTo(ref) > 0) {
                    Time t = (Time) this.prev();
                    if (t != null) {
                        resolved = (Time) t.resolve(ref, 0);
                    }
                }
                // log.info("Resolved " + this + " to past " + resolved + ", ref=" + ref);
            } else if ((flags & SUTime.RESOLVE_TO_FUTURE) != 0) {
                if (resolved.compareTo(ref) < 0) {
                    Time t = (Time) this.next();
                    if (t != null) {
                        resolved = (Time) t.resolve(ref, 0);
                    }
                }
                // log.info("Resolved " + this + " to future " + resolved + ", ref=" + ref);
            } else if ((flags & SUTime.RESOLVE_TO_CLOSEST) != 0) {
                if (resolved.compareTo(ref) > 0) {
                    Time t = (Time) this.prev();
                    if (t != null) {
                        Time resolved2 = (Time) t.resolve(ref, 0);
                        resolved = Time.closest(ref, resolved, resolved2);
                    }
                }
                if (resolved.compareTo(ref) < 0) {
                    Time t = (Time) this.next();
                    if (t != null) {
                        Time resolved2 = (Time) t.resolve(ref, 0);
                        resolved = Time.closest(ref, resolved, resolved2);
                    }
                }
                // log.info("Resolved " + this + " to closest " + resolved + ", ref=" + ref);
            }
        }

        return resolved;
    }

    public boolean isCompatible(PartialTime time) {
        return JodaTimeUtils.isCompatible(base, time.base);
    }

    public static Pair<PartialTime, PartialTime> getCompatible(PartialTime t1, PartialTime t2) {
        // Incompatible timezones
        if (t1.dateTimeZone != null && t2.dateTimeZone != null &&
                !t1.dateTimeZone.equals(t2.dateTimeZone))
            return null;
        if (t1.isCompatible(t2)) return Pair.makePair(t1, t2);
        if (t1.getUncertaintyGranularity() != null && t2.getUncertaintyGranularity() == null) {
            if (t1.getUncertaintyGranularity().compareTo(t2.getDuration()) > 0) {
                // Drop the uncertain fields from t1
                Duration d = t1.getUncertaintyGranularity();
                PartialTime t1b = t1.reduceGranularityTo(d);
                if (t1b.isCompatible(t2)) return Pair.makePair(t1b, t2);
            }
        } else if (t1.getUncertaintyGranularity() == null && t2.getUncertaintyGranularity() != null) {
            if (t2.getUncertaintyGranularity().compareTo(t1.getDuration()) > 0) {
                // Drop the uncertain fields from t2
                Duration d = t2.getUncertaintyGranularity();
                PartialTime t2b = t2.reduceGranularityTo(d);
                if (t1.isCompatible(t2b)) return Pair.makePair(t1, t2b);
            }
        } else if (t1.getUncertaintyGranularity() != null && t2.getUncertaintyGranularity() != null) {
            Duration d1 = Duration.max(t1.getUncertaintyGranularity(), t2.getDuration());
            Duration d2 = Duration.max(t2.getUncertaintyGranularity(), t1.getDuration());
            PartialTime t1b = t1.reduceGranularityTo(d1);
            PartialTime t2b = t2.reduceGranularityTo(d2);
            if (t1b.isCompatible(t2b)) return Pair.makePair(t1b, t2b);
        }
        return null;
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
        if (base == null) {
            return null;
        }
        DateTimeFieldType mostGeneral = JodaTimeUtils.getMostGeneral(base);
        DurationFieldType df = mostGeneral.getRangeDurationType();
        // if (df == null) {
        // df = mostGeneral.getDurationType();
        // }
        if (df != null) {
            try {
                return new DurationWithFields(new Period().withField(df, 1));
            } catch (Exception ex) {
                // TODO: Do something intelligent here
            }
        }
        return null;
    }

    public List<Temporal> toList() {
        if (JodaTimeUtils.hasField(base, DateTimeFieldType.year())
                && JodaTimeUtils.hasField(base, DateTimeFieldType.monthOfYear())
                && JodaTimeUtils.hasField(base, DateTimeFieldType.dayOfWeek())) {
            List<Temporal> list = new ArrayList<>();
            Partial pt = new Partial();
            pt = JodaTimeUtils.setField(pt, DateTimeFieldType.year(), base.get(DateTimeFieldType.year()));
            pt = JodaTimeUtils.setField(pt, DateTimeFieldType.monthOfYear(), base.get(DateTimeFieldType.monthOfYear()));
            pt = JodaTimeUtils.setField(pt, DateTimeFieldType.dayOfMonth(), 1);

            Partial candidate = JodaTimeUtils.resolveDowToDay(base, pt);
            if (candidate.get(DateTimeFieldType.monthOfYear()) != base.get(DateTimeFieldType.monthOfYear())) {
                pt = JodaTimeUtils.setField(pt, DateTimeFieldType.dayOfMonth(), 8);
                candidate = JodaTimeUtils.resolveDowToDay(base, pt);
                if (candidate.get(DateTimeFieldType.monthOfYear()) != base.get(DateTimeFieldType.monthOfYear())) {
                    // give up
                    return null;
                }
            }
            try {
                while (candidate.get(DateTimeFieldType.monthOfYear()) == base.get(DateTimeFieldType.monthOfYear())) {
                    list.add(new PartialTime(this, candidate));
                    pt = JodaTimeUtils.setField(pt, DateTimeFieldType.dayOfMonth(), pt.get(DateTimeFieldType.dayOfMonth()) + 7);
                    candidate = JodaTimeUtils.resolveDowToDay(base, pt);
                }
            } catch (IllegalFieldValueException ex) {
            }
            return list;
        } else {
            return null;
        }
    }

    @Override
    public Time intersect(Time t) {
        if (t == null || t == SUTime.TIME_UNKNOWN)
            return this;
        if (base == null) {
            if (dateTimeZone != null) {
                return (Time) t.setTimeZone(dateTimeZone);
            } else {
                return t;
            }
        }
        if (t instanceof CompositePartialTime) {
            return t.intersect(this);
        } else if (t instanceof PartialTime) {
            Pair<PartialTime, PartialTime> compatible = getCompatible(this, (PartialTime) t);
            if (compatible == null) {
                return null;
            }
            Partial p = JodaTimeUtils.combine(compatible.first.base, compatible.second.base);
            // Take timezone if there is one
            DateTimeZone dtz = (dateTimeZone != null) ? dateTimeZone : ((PartialTime) t).dateTimeZone;
            PartialTime res = new PartialTime(p);
            if (dtz != null) return res.setTimeZone(dtz);
            else return res;
        } else if (t instanceof OrdinalTime) {
            Temporal temp = t.resolve(this);
            if (temp instanceof PartialTime) return (Time) temp;
            else return t.intersect(this);
        } else if (t instanceof GroundedTime) {
            return t.intersect(this);
        } else if (t instanceof RelativeTime) {
            return t.intersect(this);
        } else {
            Time cpt = makeComposite(this, t);
            if (cpt != null) {
                return cpt;
            }
            if (t instanceof InexactTime) {
                return t.intersect(this);
            }
        }
        return null;
        // return new RelativeTime(this, TemporalOp.INTERSECT, t);
    }

    /*public Temporal intersect(Temporal t) {
      if (t == null)
        return this;
      if (t == TIME_UNKNOWN || t == DURATION_UNKNOWN)
        return this;
      if (base == null)
        return t;
      if (t instanceof Time) {
        return intersect((Time) t);
      } else if (t instanceof Range) {
        return t.intersect(this);
      } else if (t instanceof Duration) {
        return new RelativeTime(this, TemporalOp.INTERSECT, t);
      }
      return null;
    }        */

    protected PartialTime addSupported(Period p, int scalar) {
        return new PartialTime(base.withPeriodAdded(p, scalar));
    }

    protected PartialTime addUnsupported(Period p, int scalar) {
        return new PartialTime(this, JodaTimeUtils.addForce(base, p, scalar));
    }

    @Override
    public Time add(Duration offset) {
        if (base == null) {
            return this;
        }
        Period per = offset.getJodaTimePeriod();
        PartialTime p = addSupported(per, 1);
        Period unsupported = JodaTimeUtils.getUnsupportedDurationPeriod(p.base, per);
        Time t = p;
        if (unsupported != null) {
            if (/*unsupported.size() == 1 && */JodaTimeUtils.hasField(unsupported, DurationFieldType.weeks()) && JodaTimeUtils.hasField(p.base, DateTimeFieldType.year())
                    && JodaTimeUtils.hasField(p.base, DateTimeFieldType.monthOfYear()) && JodaTimeUtils.hasField(p.base, DateTimeFieldType.dayOfMonth())) {
                // What if there are other unsupported fields...
                t = p.addUnsupported(per, 1);
            } else {
                if (JodaTimeUtils.hasField(unsupported, DurationFieldType.months()) && unsupported.getMonths() % 3 == 0 && JodaTimeUtils.hasField(p.base, JodaTimeUtils.QuarterOfYear)) {
                    Partial p2 = p.base.withFieldAddWrapped(JodaTimeUtils.Quarters, unsupported.getMonths() / 3);
                    p = new PartialTime(p, p2);
                    unsupported = unsupported.withMonths(0);
                }
                if (JodaTimeUtils.hasField(unsupported, DurationFieldType.months()) && unsupported.getMonths() % 6 == 0 && JodaTimeUtils.hasField(p.base, JodaTimeUtils.HalfYearOfYear)) {
                    Partial p2 = p.base.withFieldAddWrapped(JodaTimeUtils.HalfYears, unsupported.getMonths() / 6);
                    p = new PartialTime(p, p2);
                    unsupported = unsupported.withMonths(0);
                }
                if (JodaTimeUtils.hasField(unsupported, DurationFieldType.years()) && unsupported.getYears() % 10 == 0 && JodaTimeUtils.hasField(p.base, JodaTimeUtils.DecadeOfCentury)) {
                    Partial p2 = p.base.withFieldAddWrapped(JodaTimeUtils.Decades, unsupported.getYears() / 10);
                    p = new PartialTime(p, p2);
                    unsupported = unsupported.withYears(0);
                }
                if (JodaTimeUtils.hasField(unsupported, DurationFieldType.years()) && unsupported.getYears() % 100 == 0
                        && JodaTimeUtils.hasField(p.base, DateTimeFieldType.centuryOfEra())) {
                    Partial p2 = p.base.withField(DateTimeFieldType.centuryOfEra(), p.base.get(DateTimeFieldType.centuryOfEra()) + unsupported.getYears() / 100);
                    p = new PartialTime(p, p2);
                    unsupported = unsupported.withYears(0);
                }
//          if (unsupported.getDays() != 0 && !JodaTimeUtils.hasField(p.base, DateTimeFieldType.dayOfYear()) && !JodaTimeUtils.hasField(p.base, DateTimeFieldType.dayOfMonth())
//              && !JodaTimeUtils.hasField(p.base, DateTimeFieldType.dayOfWeek()) && JodaTimeUtils.hasField(p.base, DateTimeFieldType.monthOfYear())) {
//            if (p.getGranularity().compareTo(DAY) <= 0) {
//              // We are granular enough for this
//              Partial p2 = p.base.with(DateTimeFieldType.dayOfMonth(), unsupported.getDays());
//              p = new PartialTime(p, p2);
//              unsupported = unsupported.withDays(0);
//            }
//          }
                if (!unsupported.equals(Period.ZERO)) {
                    t = new RelativeTime(p, new DurationWithFields(unsupported));
                    t.approx = this.approx;
                    t.mod = this.mod;
                } else {
                    t = p;
                }
            }
        }
        return t;
    }

}
