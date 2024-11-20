package org.psu.testdriver;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.jbosslog.JBossLog;

/**
 * Utility class to load data from resource files
 */
@JBossLog
public class LocalResourceLoader {

	/**
	 * @param <T> The type of object to return
	 * @param fileName The file name to load from
	 * @param resourceClass The {@link Class} of data to return
	 * @return A List of <T>'s loaded from the resource file, null if an error occurs
	 */
	public static <T> List<T> loadResourceList(final String fileName, final Class<? extends T> resourceClass) {
		final ObjectMapper mapper = new ObjectMapper();
		final JavaType type = mapper.getTypeFactory().constructCollectionLikeType(List.class, resourceClass);
		return load(fileName, type);
	}

	/**
	 * @param <T> The type of object to return
	 * @param fileName The file name to load from
	 * @param resourceClass The {@link Class} of data to return
	 * @return A <T> object loaded from the resource file, null if an error occurs
	 */
	public static <T> T loadResource(final String fileName, final Class<? extends T> resourceClass) {
		final ObjectMapper mapper = new ObjectMapper();
		final JavaType type = mapper.getTypeFactory().constructType(resourceClass);
		return load(fileName, type);
	}

	private static <T> T load(final String fileName, final JavaType type) {
		final ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());

		try (final InputStream is = LocalResourceLoader.class.getResourceAsStream(fileName)) {
			try {
				return mapper.readValue(is, type);
			} catch (Exception e) {
				log.error(e);
			}
		} catch (IOException e) {
			log.error(e);
		}
		return null;
	}

}
