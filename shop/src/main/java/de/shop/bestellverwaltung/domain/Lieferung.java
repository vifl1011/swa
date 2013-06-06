package de.shop.bestellverwaltung.domain;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.jboss.resteasy.annotations.providers.jaxb.Formatted;

import de.shop.util.Constants;
import de.shop.util.IdGroup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * The persistent class for the lieferung database table.
 * 
 */
@XmlRootElement
@Formatted
@Entity
@Table(name = "lieferung")
@NamedQueries({
		@NamedQuery(name = Lieferung.FIND_LIEFERUNG_BY_ID, 
			query = "SELECT l FROM Lieferung as l "
				+ "WHERE l.id = :" + Lieferung.PARAM_LIEFERUNG_ID),
		@NamedQuery(name = Lieferung.FIND_LIEFERUNGEN, 
			query = "SELECT l FROM Lieferung as l") })

public class Lieferung implements Serializable {
	/**
	 * Generated Serial Version UID
	 */
	private static final long serialVersionUID = -1829542637904569969L;
	
	public static final String PREFIX = "Lieferung.";
	public static final String FIND_LIEFERUNG_BY_ID = "findLieferungById";
	public static final String FIND_LIEFERUNGEN = "findLieferungen";

	public static final String PARAM_LIEFERUNG_ID = "lieferungId";
	
	private static final String DATE_PATTERNS = "yyyy-MM-dd";

	@Id
	@GeneratedValue
	@Column(name = "id", nullable = false, updatable = false, precision = Constants.LONG_ANZ_ZIFFERN)
	@Min(value = Constants.MIN_ID, message = "{bestellverwaltung.lieferung.id.min", groups = IdGroup.class)
	private Long id = Constants.KEINE_ID;

	@Version
	@Basic(optional = false)
	@Column(name = "version")
	private int version = Constants.ERSTE_VERSION;
	
	@JsonIgnore
	private Date aktualisiert;

	@NotNull
	private String art;

	@NotNull
	@JsonIgnore
	private Date erzeugt;

	@JsonIgnore
	private Date versanddatum;

	// bi-directional many-to-one association to Bestellposition
	@OneToMany(mappedBy = "lieferung", fetch = FetchType.LAZY)
	@JsonIgnore
	private List<Bestellposition> bestellpositionenFL;	

	@PrePersist
	private void prePersist() {
		this.erzeugt = new Date();
		this.aktualisiert = new Date();
	}

	@PreUpdate
	public void preUpdate() {
		this.aktualisiert = new Date();
	}

	public Lieferung() {
		super();
	}

	public Lieferung(String art) {
		this();
		this.id = null;
		this.aktualisiert = null;
		this.art = art;
		this.erzeugt = new Date();
		this.versanddatum = new Date();
	}

	public void addBestellposition(Bestellposition bestellPosition) {
		if (bestellPosition != null)
			bestellpositionenFL.add(bestellPosition);
	}

	// Getter-/Setter
	// ...................................	
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getAktualisiert() {
		return this.aktualisiert;
	}

	public void setAktualisiert(Date aktualisiert) {
		this.aktualisiert = aktualisiert;
	}
	
	public String getAktualisiertStr() {
		if (aktualisiert == null)
			return null;
		
		return new SimpleDateFormat(DATE_PATTERNS, Locale.getDefault()).format(aktualisiert);
	}
	
	public void setAktualisiertStr(String date) throws ParseException {
			aktualisiert = new SimpleDateFormat(DATE_PATTERNS, Locale.getDefault()).parse(date);
	}

	public String getArt() {
		return this.art;
	}

	public void setArt(String art) {
		this.art = art;
	}

	public Date getErzeugt() {
		return (Date) this.erzeugt;
	}

	public void setErzeugt(Date erzeugt) {
		this.erzeugt = erzeugt;
	}
	
	public String getErzeugtStr() {
		if (erzeugt == null)
			return null;
		
		return new SimpleDateFormat(DATE_PATTERNS, Locale.getDefault()).format(erzeugt);
	}
	
	public void setErzeugtStr(String date) throws ParseException {
			erzeugt = new SimpleDateFormat(DATE_PATTERNS, Locale.getDefault()).parse(date);
	}

	public Date getVersanddatum() {
		return (Date) this.versanddatum;
	}

	public void setVersanddatum(Date versanddatum) {
		this.versanddatum = versanddatum;
	}

	public String getVersanddatumStr() {
		if (versanddatum == null)
			return null;
		
		return new SimpleDateFormat(DATE_PATTERNS, Locale.getDefault()).format(versanddatum);
	}
	
	public void setVersanddatumStr(String date) throws ParseException {
			versanddatum = new SimpleDateFormat(DATE_PATTERNS, Locale.getDefault()).parse(date);
	}
	
	@JsonIgnore
	public List<Bestellposition> getBestellpositionen() {
		return this.bestellpositionenFL;
	}

	public void setBestellpositions(List<Bestellposition> bestellpositionen) {
		this.bestellpositionenFL = bestellpositionen;
	}
	
	public void setValues(Lieferung lieferung) {
		art = lieferung.getArt();
		versanddatum = lieferung.getVersanddatum();
	}

	@Override
	public String toString() {
		return "Lieferung [id=" + getId() 
				+ ", aktualisiert=" + getAktualisiert() 
				+ ", art=" + getArt() 
				+ ", erzeugt=" + getErzeugt() 
				+ ", versanddatum=" + getVersanddatum()
				+ ", bestellpositionen=" + getBestellpositionen() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((aktualisiert == null) ? 0 : aktualisiert.hashCode());
		result = prime * result + ((art == null) ? 0 : art.hashCode());
		result = prime * result + ((bestellpositionenFL == null) ? 0 : bestellpositionenFL.hashCode());
		result = prime * result + ((erzeugt == null) ? 0 : erzeugt.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((versanddatum == null) ? 0 : versanddatum.hashCode());
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
		final Lieferung other = (Lieferung) obj;
		if (aktualisiert == null) {
			if (other.aktualisiert != null) {
				return false;
			}
		} 
		else if (!aktualisiert.equals(other.aktualisiert)) {
			return false;
		}
		if (art == null) {
			if (other.art != null) {
				return false;
			}
		} 
		else if (!art.equals(other.art)) {
			return false;
		}
		if (bestellpositionenFL == null) {
			if (other.bestellpositionenFL != null) {
				return false;
			}
		} 
		else if (!bestellpositionenFL.equals(other.bestellpositionenFL)) {
			return false;
		}
		if (erzeugt == null) {
			if (other.erzeugt != null) {
				return false;
			}
		} 
		else if (!erzeugt.equals(other.erzeugt)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} 
		else if (!id.equals(other.id)) {
			return false;
		}
		if (versanddatum == null) {
			if (other.versanddatum != null) {
				return false;
			}
		} 
		else if (!versanddatum.equals(other.versanddatum)) {
			return false;
		}
		return true;
	}

}

