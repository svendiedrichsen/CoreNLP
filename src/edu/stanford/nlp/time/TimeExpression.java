package edu.stanford.nlp.time;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.tokensregex.Env;
import edu.stanford.nlp.ling.tokensregex.MatchedExpression;
import edu.stanford.nlp.ling.tokensregex.types.Expressions;
import edu.stanford.nlp.pipeline.CoreMapAggregator;
import edu.stanford.nlp.time.temporal.Temporal;
import edu.stanford.nlp.time.temporal.TimeIndex;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.ErasureUtils;
import edu.stanford.nlp.util.Interval;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Time Expression.
 *
 * @author Angel Chang
 */
public class TimeExpression extends MatchedExpression {

  /**
   * The CoreMap key for storing a SUTime.TimeIndex (for looking up Timex Id).
   */
  public static class TimeIndexAnnotation implements CoreAnnotation<TimeIndex> {
    @Override
    public Class<TimeIndex> getType() {
      return TimeIndex.class;
    }
  }

  // todo [cdm 2016]: Rename this class!
  /**
   * The CoreMap key for storing a TimeExpression annotation.
   */
  public static class Annotation implements CoreAnnotation<TimeExpression> {
    @Override
    public Class<TimeExpression> getType() {
      return TimeExpression.class;
    }
  }

  /**
   * The CoreMap key for storing a nested annotations.
   */
  public static class ChildrenAnnotation implements CoreAnnotation<List<? extends CoreMap>> {
    @Override
    public Class<List<? extends CoreMap>> getType() {
      return ErasureUtils.<Class<List<? extends CoreMap>>> uncheckedCast(List.class);
    }
  }

  //int tid;     // Time ID
  Temporal origTemporal;  // todo [2013]: never read. Can delete? (Set in TimeExpressionExtractorImpl)
  //int anchorTimeId = -1;

  public TimeExpression(MatchedExpression expr)
  {
    super(expr);
  }

  public TimeExpression(Interval<Integer> charOffsets, Interval<Integer> tokenOffsets,
                        Function<CoreMap, Temporal> temporalFunc, double priority, double weight)
  {
    super(charOffsets, tokenOffsets, getSingleAnnotationExtractor(temporalFunc), priority, weight);
  }

  protected static final Function<MatchedExpression, TimeExpression> TimeExpressionConverter = in -> {
    if (in == null) return null;
    if (in instanceof TimeExpression) return (TimeExpression) in;
    TimeExpression newExpr = new TimeExpression(in);
    if (newExpr.getAnnotation().get(Annotation.class) == in) {
      newExpr.getAnnotation().set(Annotation.class, newExpr);
    }
    return newExpr;
  };

  private static SingleAnnotationExtractor getSingleAnnotationExtractor(final Function<CoreMap, Temporal> temporalFunc)
  {
    SingleAnnotationExtractor extractFunc = new SingleAnnotationExtractor();
    extractFunc.valueExtractor = in -> {
      Temporal t = temporalFunc.apply(in);
      return new Expressions.PrimitiveValue<>("Temporal", t);
    };
    extractFunc.tokensAnnotationField = CoreAnnotations.NumerizedTokensAnnotation.class;
    extractFunc.resultAnnotationField = Collections.singletonList((Class) TimeExpression.Annotation.class);
    extractFunc.resultNestedAnnotationField = TimeExpression.ChildrenAnnotation.class;
    extractFunc.resultAnnotationExtractor = TimeExpressionConverter;
    extractFunc.tokensAggregator = CoreMapAggregator.DEFAULT_NUMERIC_TOKENS_AGGREGATOR;
    return extractFunc;
  }

  public boolean addMod()
  {
    Temporal t = getTemporal();
    if (t != null) {
      if (t != SUTime.TIME_NONE_OK) {
        setTemporal(t);
        return true;
      } else {
        return false;
      }
    } else {
      return true;
    }
  }

  @Override
  public boolean extractAnnotation(Env env, CoreMap sourceAnnotation)
  {
    boolean okay = super.extractAnnotation(env, sourceAnnotation);
            //super.extractAnnotation(sourceAnnotation, CoreAnnotations.NumerizedTokensAnnotation.class,
            //CoreMapAttributeAggregator.DEFAULT_NUMERIC_TOKENS_AGGREGATORS,
            //TimeExpression.Annotation.class, TimeExpression.ChildrenAnnotation.class);
    if (okay) {
      return addMod();
    } else {
      return false;
    }
  }

  @Override
  public boolean extractAnnotation(Env env, List<? extends CoreMap> source)
  {
    boolean okay = super.extractAnnotation(env, source);
            //super.extractAnnotation(source, CoreMapAttributeAggregator.getDefaultAggregators(),
            //TimeExpression.Annotation.class, TimeExpression.ChildrenAnnotation.class);
    if (okay) {
      return addMod();
    } else {
      return false;
    }
  }

 /* public int getTid() {
    return tid;
  }*/

  public Temporal getTemporal() {
    if (value != null && value.get() instanceof Temporal) {
      return (Temporal) value.get();
    }
    return null;
  }

  public void setTemporal(Temporal temporal) {
    this.value = new Expressions.PrimitiveValue<>("Temporal", temporal);
  }

/*  public String toString()
  {
    return text;
  } */

/*  public Timex getTimex(SUTime.TimeIndex timeIndex) {
    Timex timex = temporal.getTimex(timeIndex);
    timex.text = text;
    timex.xml = timex
    assert(timex.tid == tid);
  } */


}
