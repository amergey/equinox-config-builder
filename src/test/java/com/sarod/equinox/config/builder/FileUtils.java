package com.sarod.equinox.config.builder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;

public class FileUtils {

	public static void unzip(InputStream inputStream, File targetDirectory) throws IOException {
		ZipInputStream zipStream = new ZipInputStream(inputStream);
		try {
			ZipEntry zipEntry = null;
			while ((zipEntry = zipStream.getNextEntry()) != null) {
				if (!zipEntry.isDirectory()) {
					final File file = new File(targetDirectory, zipEntry.getName());
					Files.createParentDirs(file);
					Files.write(ByteStreams.toByteArray(zipStream), file);
				}
			}
		} finally {
			Closeables.closeQuietly(zipStream);
		}
	}

}
