package com.github.akmal2409.netflix.videoslicer.coordinator.util;

import java.util.Comparator;

public final class ArrayUtils {

  private ArrayUtils() {
    throw new IllegalStateException("Cannot instantiate a utility class");
  }

  public static <T> int binarySearchCeil(final T[] elements,
      final T target,
      final Comparator<T> comparator,
      final int start, final int end) {
    if (start < 0 || start >= end || end > elements.length) {
      throw new ArrayIndexOutOfBoundsException(
          "Cannot apply binary search on wrong interval [" + start + ", " + end + "]");
    }

    if (comparator.compare(target, elements[end - 1]) > 0) {
      return end - 1;
    }

    int lo = start;
    int hi = end - 1;
    int mid;
    int comparison;

    while (lo < hi) {
      mid = lo + ((hi - lo)>>1);

      comparison = comparator.compare(target, elements[mid]);

      if (comparison > 0) {
        lo = mid + 1;
      } else {
        hi = mid;
      }
    }

    return lo;
  }

  public static <T> int binarySearchFloor(final T[] elements,
      final T target,
      final Comparator<T> comparator,
      final int start, final int end) {
    if (start < 0 || start >= end || end > elements.length) {
      throw new ArrayIndexOutOfBoundsException(
          "Cannot apply binary search on wrong interval [" + start + ", " + end + "]");
    }

    if (comparator.compare(target, elements[start]) < 0) {
      return -1;
    }

    int lo = start;
    int hi = end - 1;
    int mid;
    int comparison;

    while (lo < hi) {
      mid = lo + ((hi - lo) >> 1);

      comparison = comparator.compare(target, elements[mid]);

      if (comparison < 0) {
        hi = mid - 1;
      } else if (comparator.compare(target, elements[mid + 1]) < 0) {
        lo = mid;
        break;
      } else {
        lo = mid + 1;
      }
    }

    return lo;
  }
}
