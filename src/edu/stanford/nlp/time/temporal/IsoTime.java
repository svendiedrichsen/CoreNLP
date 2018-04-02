package edu.stanford.nlp.time.temporal;

import edu.stanford.nlp.time.JodaTimeUtils;
import edu.stanford.nlp.time.SUTime;
import org.joda.time.DateTimeFieldType;

// Helper time class
public class IsoTime extends PartialTime {

    private static final long serialVersionUID = 1;

    public int hour = -1;
    public int minute = -1;
    public int second = -1;
    public int millis = -1;
    public int halfday = SUTime.HALFDAY_UNKNOWN; // 0 = am, 1 = pm

    public IsoTime(int h, int m, int s) {
        this(h, m, s, -1, -1);
    }

    // TODO: Added for reading types from file
    public IsoTime(Number h, Number m, Number s) {
        this(h, m, s, null, null);
    }

    public IsoTime(int h, int m, int s, int ms, int halfday) {
        this.hour = h;
        this.minute = m;
        this.second = s;
        this.millis = ms;
        this.halfday = halfday;
        // Some error checks
        second += millis / 1000;
        millis = millis % 1000;
        minute += second / 60;
        second = second % 60;
        hour += hour / 60;
        minute = minute % 60;
        // Error checks done
        initBase();
    }

    // TODO: Added for reading types from file
    public IsoTime(Number h, Number m, Number s, Number ms, Number halfday) {
        this(
                (h != null) ? h.intValue() : -1,
                (m != null) ? m.intValue() : -1,
                (s != null) ? s.intValue() : -1,
                (ms != null) ? ms.intValue() : -1,
                (halfday != null) ? halfday.intValue() : -1);
    }

    public IsoTime(String h, String m, String s) {
        this(h, m, s, null);
    }

    public IsoTime(String h, String m, String s, String ms) {
        if (h != null) {
            hour = Integer.parseInt(h);
        }
        if (m != null) {
            minute = Integer.parseInt(m);
        }
        if (s != null) {
            second = Integer.parseInt(s);
        }
        if (ms != null) {
            millis = Integer.parseInt(s);
        }
        initBase();
    }

    @Override
    public boolean hasTime() {
        return true;
    }

    private void initBase() {
        if (hour >= 0) {
            if (hour < 24) {
                base = JodaTimeUtils.setField(base, DateTimeFieldType.hourOfDay(), hour);
            } else {
                base = JodaTimeUtils.setField(base, DateTimeFieldType.clockhourOfDay(), hour);
            }
        }
        if (minute >= 0)
            base = JodaTimeUtils.setField(base, DateTimeFieldType.minuteOfHour(), minute);
        if (second >= 0)
            base = JodaTimeUtils.setField(base, DateTimeFieldType.secondOfMinute(), second);
        if (millis >= 0)
            base = JodaTimeUtils.setField(base, DateTimeFieldType.millisOfSecond(), millis);
        if (halfday >= 0) {
            base = JodaTimeUtils.setField(base, DateTimeFieldType.halfdayOfDay(), halfday);
        }
    }
}
