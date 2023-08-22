package com.github.akmal2409.netflix.videoslicer.config.loader.utils;

import java.lang.reflect.Constructor;
import java.util.Optional;


public final class ReflectionUtils {

  private ReflectionUtils() {
    throw new IllegalStateException("Cannot instantiate utility class");
  }

  @SuppressWarnings("unchecked")
  public static <C> Optional<Constructor<C>> getDefaultConstructor(
      Class<C> clazz
  ) {
    final Constructor<C>[] constructors = (Constructor<C>[]) clazz.getDeclaredConstructors();

    for (Constructor<C> constructor: constructors) {
      if (constructor.getParameterCount() == 0) {
        return Optional.of(constructor);
      }
    }

    return Optional.empty();
  }
}
