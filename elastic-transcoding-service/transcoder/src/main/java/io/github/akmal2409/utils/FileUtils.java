package io.github.akmal2409.utils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public final class FileUtils {

  private FileUtils() {
    throw new IllegalStateException("Cannot instantiate a utility class");
  }

  public static String getFileExtension(String fileName) {
    if (fileName == null || fileName.isBlank()) {
      throw new IllegalArgumentException("Input file name is empty");
    }

    final int lastDotIndex = fileName.lastIndexOf(".");

    if (lastDotIndex == fileName.length() - 1 || lastDotIndex == -1) {
      throw new NoFileExtensionException("No file extension is present");
    }

    return fileName.substring(lastDotIndex + 1);
  }

  public static void deleteDirectory(Path directory) throws IOException {
    Files.walkFileTree(directory, new FileVisitor<>() {
      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
          throws IOException {
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.TERMINATE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        Files.delete(dir);
        return FileVisitResult.CONTINUE;
      }
    });
  }
}
