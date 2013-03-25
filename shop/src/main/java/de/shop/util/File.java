package de.shop.util;

import static de.shop.util.Constants.ERSTE_VERSION;
import static de.shop.util.Constants.KEINE_ID;
import static javax.persistence.TemporalType.TIMESTAMP;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Version;

import de.shop.util.FileHelper.MimeType;
import de.shop.util.FileHelper.MultimediaType;


@Entity
@Table(name = "file_tbl")   // "file" ist in Oracle kein gueltiger Tabellenname
public class File implements Serializable {
	private static final long serialVersionUID = 2632441781256478734L;

	public static final int FILENAME_LENGTH_MAX = 128;
	
	@Id
	@GeneratedValue
	@Column(nullable = false, updatable = false)
	private Long id = KEINE_ID;
	
	@Version
	@Basic(optional = false)
	private int version = ERSTE_VERSION;
	
	@Lob
	@Basic(optional = false)
	private byte[] bytes;  // Spaltentyp BLOB bei Oracle und LONGBLOB bei MySQL
	
	@Column(length = FILENAME_LENGTH_MAX, unique = true, nullable = false)
	private String filename;

	@Column(name = "mimetype_fk", nullable = false)
	private MimeType mimeType;
	
	@Column(name = "multimedia_type_fk", nullable = false)
	private MultimediaType multimediaType;
	
	@Column(nullable = false)
	@Temporal(TIMESTAMP)
	private Date erzeugt;

	@Column(nullable = false)
	@Temporal(TIMESTAMP)
	private Date aktualisiert;
	
	public File() {
		super();
	}
	
	public File(byte[] bytes, String dateiname, MimeType mimeType) {
		super();
		set(bytes, dateiname, mimeType);
	}

	@PrePersist
	protected void prePersist() {
		erzeugt = new Date();
		aktualisiert = new Date();
	}

	@PreUpdate
	protected void preUpdate() {
		aktualisiert = new Date();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

	public String getFilename() {
		return filename;
	}

	public void setDateiname(String filename) {
		this.filename = filename;
	}
	
	public MimeType getMimeType() {
		return mimeType;
	}

	public void setMimeType(MimeType mimeType) {
		this.mimeType = mimeType;
	}

	public MultimediaType getMultimediaType() {
		return multimediaType;
	}

	public void setMultimediaType(MultimediaType multimediaType) {
		this.multimediaType = multimediaType;
	}

	public Date getErzeugt() {
		return erzeugt == null ? null : (Date) erzeugt.clone();
	}

	public void setErzeugt(Date erzeugt) {
		this.erzeugt = erzeugt == null ? null : (Date) erzeugt.clone();
	}

	public Date getAktualisiert() {
		return aktualisiert == null ? null : (Date) aktualisiert.clone();
	}

	public void setAktualisiert(Date aktualisiert) {
		this.aktualisiert = aktualisiert == null ? null : (Date) aktualisiert.clone();
	}

	public final void set(byte[] bytes, String filename, MimeType mimeType) {
		this.bytes = bytes;
		this.filename = filename;
		this.mimeType = mimeType;
		this.multimediaType = mimeType.getMultimediaType();
	}

	@Override
	public String toString() {
		return "File [id=" + id + ", version=" + version
			   + ", filename=" + filename + ", multimediaType=" + multimediaType
			   + ", erzeugt=" + erzeugt + ", aktualisiert=" + aktualisiert + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((filename == null) ? 0 : filename.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final File other = (File) obj;
		if (filename == null) {
			if (other.filename != null) {
				return false;
			}
		}
		else if (!filename.equals(other.filename)) {
			return false;
		}
		return true;
	}
}
