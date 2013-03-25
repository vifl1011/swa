package de.shop.util;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.logging.Logger;


@ApplicationScoped
@Named("mm")
public class FileHelper implements Serializable {
	private static final long serialVersionUID = 12904207356717310L;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	public enum MultimediaType {
		IMAGE,
		VIDEO,
		AUDIO;
	}
	
	public enum MimeType {
		GIF("image/gif"),
		JPEG("image/jpeg"),
		PJPEG("image/pjpeg"),
		PNG("image/png"),
		MP4("video/mp4"),
		WAV("audio/wav");
		
		private final String value;

		MimeType(String value) {
			this.value = value;
		}
		
		public static MimeType get(String value) {
			if (value == null) {
				return null;
			}
			
			switch (value) {
				case "image/gif":	return GIF;
				case "image/jpeg":	return JPEG;
				case "image/pjpeg":	return PJPEG;
				case "image/png":	return PNG;
				case "video/mp4":	return MP4;
				case "audio/wav":	return WAV;
				
				default:			return null;
			}
		}
		
		public String getExtension() {
			switch (this) {
				case GIF:	return "gif";
				case JPEG:	return "jpeg";
				case PJPEG:	return "jpeg";
				case PNG:	return "png";
				case MP4:	return "mp4";
				case WAV:	return "wav";
				default:	throw new IllegalStateException("Der MIME-Type " + this + " wird nicht unterstuetzt");
			}
		}
		
		public MultimediaType getMultimediaType() {
			if (value.startsWith("image/")) {
				return MultimediaType.IMAGE;
			}
			if (value.startsWith("video/")) {
				return MultimediaType.VIDEO;
			}
			if (value.startsWith("audio/")) {
				return MultimediaType.AUDIO;
			}
			
			throw new IllegalStateException("Der MultimediaType " + this + " wird nicht unterstuetzt");
		}
		
		@Override
		public String toString() {
			return value;
		}
	}
	
	// Zulaessige Extensionen fuer Upload mit einer Webseite
	private String extensionen;
	
	// Verzeichnis fuer hochgeladene Dateien
	private transient Path directory;

	@PostConstruct
	private void init() {
		// Bei .flv wird der Mime-Type weder bei RichFaces noch bei RESTEasy erkannt
		extensionen = "gif, jpg, jpeg, png, mp4, wav";
		LOGGER.infof("Extensionen fuer Datei-Upload: %s", extensionen);
		
		String appName = null;
		Context ctx = null;
		try {
			ctx = new InitialContext();  // InitialContext implementiert nicht das Interface Autoclosable
			appName = (String) ctx.lookup("java:app/AppName");				
		}
		catch (NamingException e) {
			LOGGER.warn("java:app/AppName ist nicht im JNDI-Namensraum vorhanden");
		}
		finally {
			try {
				ctx.close();
			}
			catch (NamingException e) {
				LOGGER.warn("JNDI-Kontext zum Nachschlagen von java:app/AppName kann nicht geschlossen werden");
			}
		}
		
		// Verzeichnis im Dateisystem des Betriebssystems
		directory = Paths.get(System.getenv("JBOSS_HOME"), "standalone", "deployments",
		                      appName + ".war", "resources", "filesDb");
		
		if (Files.exists(directory)) {
			LOGGER.infof("Verzeichnis fuer hochgeladene Dateien: %s", directory);
		}
		else {
			LOGGER.errorf("Kein Verzeichnis fuer hochgeladene Dateien: %s", directory);
		}
	}
	
	/**
	 * MIME-Type zu einer Datei als byte[] ermitteln
	 */
	public MimeType getMimeType(byte[] bytes) {
		if (bytes == null) {
			return null;
		}

		String mimeTypeStr = null;
		try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
			mimeTypeStr = URLConnection.guessContentTypeFromStream(inputStream);
		}
		catch (IOException e) {
			LOGGER.warn("Fehler beim Ermitteln des MIME-Types");
			return null;
		}
		
		LOGGER.tracef("MIME-Type: %s", mimeTypeStr);
		return MimeType.get(mimeTypeStr);
	}
	
	public String getFilename(Class<?> clazz, Object id, MimeType mimeType) {
		final String filename = clazz.getSimpleName() + "_" + id + "." + mimeType.getExtension();
		LOGGER.tracef("Dateiname: %s", filename);
		return filename;
	}
	
	public String getExtensionen() {
		return extensionen;
	}
	
	public void store(File file) {
		if (file == null) {
			return;
		}
		
		final String filename = file.getFilename();
		final Path absoluteFilename = directory.resolve(filename);
		
		// aktuelle Datei nicht ueberschreiben?
		if (Files.exists(absoluteFilename)) {
			long creationTime = 0L;
			try {
				creationTime = Files.getFileAttributeView(absoluteFilename, BasicFileAttributeView.class)
				                    .readAttributes()
									.creationTime()
									.toMillis();
			}
			catch (IOException e) {
				LOGGER.tracef("Fehler beim Lesen des Erzeugungsdatums der Datei %s: %s", filename, e.getMessage());
			}
			
			if (creationTime > file.getAktualisiert().getTime()) {
				LOGGER.tracef("Die Datei %s existiert bereits", filename);
				return;
			}
		}
		
		// byte[] als Datei abspeichern
		try (ByteArrayInputStream inputStream = new ByteArrayInputStream(file.getBytes())) {
			Files.copy(inputStream, absoluteFilename, REPLACE_EXISTING);
		}
		catch (IOException e) {
			LOGGER.warnf("Fehler beim Speichern der Datei %s: %s", absoluteFilename, e.getMessage());
		}
	}
}
