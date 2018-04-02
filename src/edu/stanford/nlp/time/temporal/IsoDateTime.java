package edu.stanford.nlp.time.temporal;

import edu.stanford.nlp.time.JodaTimeUtils;

public class IsoDateTime extends PartialTime {

    private static final long serialVersionUID = 1;

    private final IsoDate date;
    private final IsoTime time;

    public IsoDateTime(IsoDate date, IsoTime time) {
        this.date = date;
        this.time = time;
        base = JodaTimeUtils.combine(date.base, time.base);
    }

    @Override
    public boolean hasTime() {
        return (time != null);
    }

    /*    public String toISOString()
        {
          return date.toISOString() + time.toISOString();
        }  */

}
