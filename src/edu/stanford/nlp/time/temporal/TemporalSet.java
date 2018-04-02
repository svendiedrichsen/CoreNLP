package edu.stanford.nlp.time.temporal;

import edu.stanford.nlp.time.SUTime;

/**
 * Exciting set of times
 */
public abstract class TemporalSet extends Temporal {

    private static final long serialVersionUID = 1;

    public TemporalSet() {
    }

    public TemporalSet(TemporalSet t) {
        super(t);
    }

    // public boolean includeTimexAltValue() { return true; }
    @Override
    public SUTime.TimexType getTimexType() {
        return SUTime.TimexType.SET;
    }

}
