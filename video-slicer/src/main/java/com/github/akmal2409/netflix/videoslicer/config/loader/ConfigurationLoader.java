package com.github.akmal2409.netflix.videoslicer.config.loader;

import com.github.akmal2409.netflix.videoslicer.config.loader.utils.ReflectionUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Loads configuration from multiple {@link ConfigurationSource} that are sorted based on their
 * priority. C - class requires to have fields annotated with {@link ConfigurationProperty}
 */
public class ConfigurationLoader<C> {

  private final Collection<ConfigurationSource> sources;
  private final Class<C> configClass;
  private final Collection<String> configurationKeys; // stores the keys parsed from Configuration class (C type)

  public ConfigurationLoader(List<ConfigurationSource> sources,
      Class<C> configClass) {
    final var copy = new ArrayList<>(sources);

    copy.sort(Comparator.naturalOrder());
    this.sources = Collections.unmodifiableCollection(copy);
    this.configClass = configClass;
    this.configurationKeys = extractConfigurationKeys();
  }

  /**
   * Loads the properties in order of priority (from lowest to highest).
   */
  public C load() {
    final var properties = new HashMap<String, Object>();

    for (ConfigurationSource source : sources) {
      final var specificConfiguration = source.load(configurationKeys);
      properties.putAll(specificConfiguration);
    }

    return mapToConfigClassInstance(properties);
  }

  private C mapToConfigClassInstance(Map<String, Object> properties) {
    final C configInstance = newConfigClassInstance();

    traverseConfigurationPropFields(field -> {
      final String propKey = field.getAnnotation(ConfigurationProperty.class).key();
      if (properties.containsKey(propKey)) {
        try {
          field.set(configInstance, properties.get(propKey));
        } catch (IllegalAccessException e) {
          throw new ConfigurationLoadingException("Cannot set key on class " + propKey, e);
        }
      }
    });

    return configInstance;
  }

  private C newConfigClassInstance() {
    final Constructor<C> constructor = ReflectionUtils.getDefaultConstructor(configClass)
                                           .orElseThrow(() -> new IllegalArgumentException(
                                               String.format(
                                                   "Provided configuration class %s does not have no argument constructor",
                                                   configClass.getName())
                                           ));
    C configInstance;

    try {
      constructor.setAccessible(true);
      configInstance = constructor.newInstance();
    } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
      throw new ConfigurationLoadingException(
          String.format("Cannot construct an instance of %s. You might want to check permissions",
              configClass.getName())
          , e);
    } finally {
      constructor.setAccessible(false);
    }

    return configInstance;
  }

  private Collection<String> extractConfigurationKeys() {
    final Collection<String> keys = new HashSet<>();

    traverseConfigurationPropFields(field -> {
      keys.add(field.getAnnotation(ConfigurationProperty.class).key());
    });

    return keys;
  }

  /**
   * Traverses the fields on the configuration class that are annotated using
   * {@link ConfigurationProperty} and applies consumer function on the {@link Field}. Note actions
   * such as setting field accessible must be reverted at the end!
   *
   * @param processFieldConsumer post-process field.
   */
  private void traverseConfigurationPropFields(
      Consumer<Field> processFieldConsumer
  ) {
    final Field[] fields = configClass.getDeclaredFields();

    for (Field field : fields) {
      field.setAccessible(true);
      if (field.isAnnotationPresent(ConfigurationProperty.class)) {
        processFieldConsumer.accept(field);
      }
      field.setAccessible(false);
    }
  }
}
