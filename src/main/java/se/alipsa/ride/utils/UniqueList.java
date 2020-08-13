package se.alipsa.ride.utils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A naive implementation of a List containing only unique items
 * Works fine for small lists but for bigger lists something more advanced is needed due to
 * performance issues with contains methods used to guarantee uniqueness.
 *
 * @param <T>
 */
public class UniqueList<T> extends ArrayList<T> {

  static final long serialVersionUID = 1L;

  @Override
  public boolean add(T t) {
    if (!contains(t)) return super.add(t);
    return false;
  }

  @Override
  public void add(int index, T element) {
    if (!contains(element)) super.add(index, element);
  }

  @Override
  public boolean addAll(Collection<? extends T> c) {
    if (!containsAll(c)) return super.addAll(c);
    return false;
  }

  @Override
  public boolean addAll(int index, Collection<? extends T> c) {
    if (!containsAll(c)) return super.addAll(index, c);
    return false;
  }
}
