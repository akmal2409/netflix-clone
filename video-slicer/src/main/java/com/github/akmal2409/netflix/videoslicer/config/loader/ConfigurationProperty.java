package com.github.akmal2409.netflix.videoslicer.config.loader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigurationProperty {

  /**
   * Configuration property key
   * Example: test.prop1
   */
  String key();

  /**
   * Description of the configuration property
   */
  String description();

  String defaultValue() default "";
}
