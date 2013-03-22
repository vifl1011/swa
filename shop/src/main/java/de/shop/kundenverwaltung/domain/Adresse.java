package de.shop.kundenverwaltung.domain;


import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;



import javax.validation.constraints.Min;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonIgnore;

import de.shop.util.Constants;
import de.shop.util.IdGroup;

import java.util.Date;


/**
 * The persistent class for the adresse database table.
 * 
 */
@Entity
@Table(name = "Adresse")
public class Adresse implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@XmlAttribute
	@Min(value = Constants.MIN_ID, message = "{kundenverwaltung.adresse.id.min }", groups = IdGroup.class)
	@Column(name = "ID", nullable = false, updatable = false)
	private Long id = null;

	@Column(name = "AKTUALISIERT", nullable = false)
	@XmlTransient
	@JsonIgnore
	private Date aktualisiert;

	@Column(name = "ERZEUGT", nullable = false)
	@XmlTransient
	@JsonIgnore
	private Date erzeugt;

	@Column(name = "HAUSNUMMER", length = 30, nullable = false)
	@XmlElement(required = true)
	private String hausnummer;

	@Column(name = "ORT", length = 45, nullable = false)
	@XmlElement(required = true)
	private String ort;

	@Column(name = "PLZ", length = 10, nullable = false)
	@XmlElement(required = true)
	private String plz;

	@Column(name = "STRASSE", length = 30, nullable = false)
	@XmlElement(required = true)
	private String strasse;

	//bi-directional many-to-one association to Kunde
	@OneToOne
	@XmlTransient
	@JsonIgnore
	private Kunde kunde;


	@PrePersist
	private void prePersist() {
		this.erzeugt = new Date();
		this.aktualisiert = new Date();
	}
	
	@PreUpdate
	protected void preUpdate() {
		aktualisiert = new Date();
	}
	public Adresse() {
	}
	
	public Adresse(String ort, String plz, String strasse, String hausnummer) {
		this.hausnummer = hausnummer;
		this.ort = ort;
		this.plz = plz;
		this.strasse = strasse;
	}
	public Adresse(String ort, String plz, String strasse, String hausnummer, Kunde kunde) {
		this(ort,  plz,  strasse, hausnummer);
		this.kunde = kunde;
	}


	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getAktualisiert() {
		if (this.aktualisiert == null)
			return null;
		return (Date) this.aktualisiert.clone();
	}

	public void setAktualisiert(Date aktualisiert) {
		if (aktualisiert == null)
			return;
		this.aktualisiert = (Date) aktualisiert.clone();
	}

	public Date getErzeugt() {
		if (this.erzeugt == null)
			return null;
		return (Date) this.erzeugt.clone();
	}

	public void setErzeugt(Date erzeugt) {
		if (erzeugt == null)
			return;
		this.erzeugt = (Date) erzeugt.clone();
	}

	public String getHausnummer() {
		return this.hausnummer;
	}

	public void setHausnummer(String hausnummer) {
		this.hausnummer = hausnummer;
	}

	public String getOrt() {
		return this.ort;
	}

	public void setOrt(String ort) {
		this.ort = ort;
	}

	public String getPlz() {
		return this.plz;
	}

	public void setPlz(String plz) {
		this.plz = plz;
	}

	public String getStrasse() {
		return this.strasse;
	}

	public void setStrasse(String strasse) {
		this.strasse = strasse;
	}

	public Kunde getKunde() {
		return this.kunde;
	}

	public void setKunde(Kunde kunde) {
		this.kunde = kunde;
	}
	
	public void setValues(Adresse a) {
		setHausnummer(a.getHausnummer());
		setOrt(a.getOrt());
		setPlz(a.getPlz());
		setStrasse(a.getStrasse());
	}

	@Override
	public String toString() {
		return "Adresse [id=" + getId() 
				+ ", erzeugt=" + (getErzeugt() == null ? "ungesetzt" : getErzeugt().toString())
				+ ", aktualisiert=" + (getAktualisiert() == null ? "ungesetzt" : getAktualisiert().toString())
				+ ", erzeugt=" + getErzeugt()
				+ ", hausnummer=" + getHausnummer()
				+ ", ort=" + getOrt() 
				+ ", plz=" + getPlz()
				+ ", strasse=" + getStrasse()
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result	+ ((aktualisiert == null) ? 0 : aktualisiert.hashCode());
		result = prime * result + ((erzeugt == null) ? 0 : erzeugt.hashCode());
		result = prime * result	+ ((hausnummer == null) ? 0 : hausnummer.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((ort == null) ? 0 : ort.hashCode());
		result = prime * result + ((plz == null) ? 0 : plz.hashCode());
		result = prime * result + ((strasse == null) ? 0 : strasse.hashCode());
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
		Adresse other = (Adresse) obj;
		if (aktualisiert == null) {
			if (other.aktualisiert != null) {
				return false;
			}
		} 
		else if (!aktualisiert.equals(other.aktualisiert)) {
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
		if (hausnummer == null) {
			if (other.hausnummer != null) {
				return false;
			}
		} 
		else if (!hausnummer.equals(other.hausnummer)) {
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
		if (kunde == null) {
			if (other.kunde != null) {
				return false;
			}
		} 
		else if (!kunde.equals(other.kunde)) {
			return false;
		}
		if (ort == null) {
			if (other.ort != null) {
				return false;
			}
		} 
		else if (!ort.equals(other.ort)) {
			return false;
		}
		if (plz == null) {
			if (other.plz != null) {
				return false;
			}
		} 
		else if (!plz.equals(other.plz)) {
			return false;
		}
		if (strasse == null) {
			if (other.strasse != null) {
				return false;
			}
		} 
		else if (!strasse.equals(other.strasse)) {
			return false;
		}
		return true;
	}
	
	
	
}