package edu.stanford.nlp.time.temporal;

import edu.stanford.nlp.time.SUTime;

/**
 * Simple time (vague time that we don't really know what to do with)
 **/
public class SimpleTime extends Time {

    private static final long serialVersionUID = 1;

    private String label;

    public SimpleTime(String label) {
        this.label = label;
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

}
