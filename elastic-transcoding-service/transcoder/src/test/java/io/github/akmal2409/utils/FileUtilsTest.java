package io.github.akmal2409.utils;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FileUtilsTest {


  @Test
  @DisplayName("getFileExtension() throws IllegalArgumentException if string is empty or null")
  void getFileExtensionThrowsExceptionIfStringInvalid() {
    final var empty = "";
    final var withSpaces = "   ";
    final String nullString = null;


    assertThatThrownBy(() -> FileUtils.getFileExtension(empty))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> FileUtils.getFileExtension(withSpaces))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> FileUtils.getFileExtension(nullString))
        .isInstanceOf(IllegalArgumentException.class);
  }


  @Test
  @DisplayName("getFileExtension() throws NoFileExtension exception if string has no file extension")
  void getFileExtensionThrowsExceptionIfNoExtension() {
    final var noExt = "filename";

    final var noExtEndsWithADot = "filename.";

    assertThatThrownBy(() -> FileUtils.getFileExtension(noExt))
        .isInstanceOf(NoFileExtensionException.class);

    assertThatThrownBy(() -> FileUtils.getFileExtension(noExtEndsWithADot))
        .isInstanceOf(NoFileExtensionException.class);
  }


  @Test
  @DisplayName("getFileExtension() extracts file extension from a valid file name")
  void getFileExtensionExtractsWhenValid() {
    final var filename = "filename.exe";
    final var expectedExtension = "exe";

    assertThat(FileUtils.getFileExtension(filename))
        .isEqualTo(expectedExtension);
  }
}
