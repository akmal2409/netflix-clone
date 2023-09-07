package io.github.akmal2409.hls.tag;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public interface TagValue {

  class SingleTagValue implements TagValue {

    private final String value;

    public SingleTagValue(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return Objects.toString(this.value);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      SingleTagValue that = (SingleTagValue) o;

      return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
      return value != null ? value.hashCode() : 0;
    }
  }

  class AttributeListTagValue implements TagValue {

    private final Map<String, String> attributes;

    public AttributeListTagValue(Map<String, String> attributes) {
      this.attributes = attributes;
    }

    public AttributeListTagValue add(String key, String value) {
      this.attributes.put(key, value);
      return this;
    }

    @Override
    public String toString() {
      return attributes.entrySet().
                 stream()
                 .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                 .collect(Collectors.joining(","));
    }

    @Override
    public boolean equals(Object o) {

      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      AttributeListTagValue that = (AttributeListTagValue) o;

      return Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
      return attributes != null ? attributes.hashCode() : 0;
    }
  }
}
