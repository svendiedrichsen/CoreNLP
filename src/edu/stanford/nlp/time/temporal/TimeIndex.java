package edu.stanford.nlp.time.temporal;

import edu.stanford.nlp.time.SUTime;
import edu.stanford.nlp.time.TimeExpression;
import edu.stanford.nlp.util.HashIndex;
import edu.stanford.nlp.util.Index;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Index of time id to temporal object
public class TimeIndex {

    private Index<TimeExpression> temporalExprIndex = new HashIndex<>();
    private Index<Temporal> temporalIndex = new HashIndex<>();
    private Index<Temporal> temporalFuncIndex = new HashIndex<>();

    private Time docDate;

    public TimeIndex() {
        addTemporal(SUTime.TIME_REF);
    }

    public void clear() {
        temporalExprIndex.clear();
        temporalIndex.clear();
        temporalFuncIndex.clear();
        // t0 is the document date (reserve)
        temporalExprIndex.add(null);
        addTemporal(SUTime.TIME_REF);
    }

    public int getNumberOfTemporals() {
        return temporalIndex.size();
    }

    public int getNumberOfTemporalExprs() {
        return temporalExprIndex.size();
    }

    public int getNumberOfTemporalFuncs() {
        return temporalFuncIndex.size();
    }

    private static final Pattern ID_PATTERN = Pattern.compile("([a-zA-Z]*)(\\d+)");

    public TimeExpression getTemporalExpr(String s) {
        Matcher m = ID_PATTERN.matcher(s);
        if (m.matches()) {
            String prefix = m.group(1);
            int id = Integer.parseInt(m.group(2));
            if ("t".equals(prefix) || prefix.isEmpty()) {
                return temporalExprIndex.get(id);
            }
        }
        return null;
    }

    public Temporal getTemporal(String s) {
        Matcher m = ID_PATTERN.matcher(s);
        if (m.matches()) {
            String prefix = m.group(1);
            int id = Integer.parseInt(m.group(2));
            if ("t".equals(prefix)) {
                TimeExpression te = temporalExprIndex.get(id);
                return (te != null) ? te.getTemporal() : null;
            } else if (prefix.isEmpty()) {
                return temporalIndex.get(id);
            }
        }
        return null;
    }

    public TimeExpression getTemporalExpr(int i) {
        return temporalExprIndex.get(i);
    }

    public Temporal getTemporal(int i) {
        return temporalIndex.get(i);
    }

    public Temporal getTemporalFunc(int i) {
        return temporalFuncIndex.get(i);
    }

    public boolean addTemporalExpr(TimeExpression t) {
        Temporal temp = t.getTemporal();
        if (temp != null) {
            addTemporal(temp);
        }
        return temporalExprIndex.add(t);
    }

    public boolean addTemporal(Temporal t) {
        return temporalIndex.add(t);
    }

    public boolean addTemporalFunc(Temporal t) {
        return temporalFuncIndex.add(t);
    }

    public int addToIndexTemporalExpr(TimeExpression t) {
        return temporalExprIndex.addToIndex(t);
    }

    public int addToIndexTemporal(Temporal t) {
        return temporalIndex.addToIndex(t);
    }

    public int addToIndexTemporalFunc(Temporal t) {
        return temporalFuncIndex.addToIndex(t);
    }

    public void setDocDate(Time docDate) {
        this.docDate = docDate;
    }

    public Time getDocDate() {
        return docDate;
    }
}
