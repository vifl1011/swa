package de.shop.artikelverwaltung.domain;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
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
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.util.Constants;
import de.shop.util.IdGroup;

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import org.codehaus.jackson.annotate.JsonIgnore;

import static de.shop.util.Constants.ERSTE_VERSION;


/**
 * The persistent class for the produkt database table.
 * 
 */
@Entity
@Table(name = "produkt")
@NamedQueries({ 
	@NamedQuery(name  = Produkt.FIND_PRODUKTE,
            query = "SELECT p "
			        + " FROM   Produkt as p"),
	@NamedQuery(name = Produkt.FIND_PRODUKT_BY_ID, 
			query = "SELECT   p"  + " FROM   Produkt as p" + " WHERE p.id = :" + Produkt.PARAM_ID),
	@NamedQuery(name  = Produkt.FIND_LADENHUETER,
            query = "SELECT    a"
	           	    + " FROM   Produkt a"
	           	    + " WHERE  a NOT IN (SELECT bp.produkt FROM Bestellposition bp)")
})
	

@XmlRootElement
public class Produkt implements Serializable {
	private static final long serialVersionUID = -3725063964228781630L;
	private static final String PREFIX = "Produkt.";
	public static final String FIND_PRODUKT_BY_ID = PREFIX + "findProduktById";
	public static final String FIND_PRODUKTE = PREFIX + "findProdukte";
	public static final String FIND_LADENHUETER = PREFIX + "findLadenhueter";
	public static final String PARAM_ID = "id";

	@Id
	@GeneratedValue
	@Column(name = "id", unique = true, nullable = false, updatable = false)
	@Min(value = Constants.MIN_ID, message = "{artikelverwaltung.produkt.id.min}", groups = IdGroup.class)
	@XmlElement
	private Long id;
	
	@Version
	@Basic(optional = false)
	private int version = ERSTE_VERSION;

	@Column(name = "bezeichnung", nullable = true)
	@Pattern(regexp = "[A-Za-z]*")
	@XmlElement
	private String bezeichnung;

	@Column(name = "preis", nullable = true)
	@XmlElement
	private Float preis;

	@XmlTransient
	@JsonIgnore
	private Date aktualisiert;

	@XmlTransient
	@JsonIgnore
	private Date erzeugt;

	@Column(name = "farbe", nullable = true)
	@XmlElement
	private String farbe;

	@Column(name = "groesse", nullable = true)
	@XmlElement
	private String groesse;

	@Column(name = "vorrat", nullable = true)
	@XmlElement
	private int vorrat;

	// bi-directional many-to-one association to Bestellposition
	@OneToMany(mappedBy = "produkt")
	@XmlTransient
	@JsonIgnore
	private List<Bestellposition> bestellpositionen;

	// Standardkonstruktor
	public Produkt() {
		super();
	}

	// Konstruktor
	public Produkt(String bezeichnung, Float preis, String farbe,
			String groesse, int vorrat) {
		id = null;
		this.bezeichnung = bezeichnung;
		this.preis = preis;
		this.aktualisiert = null;
		this.erzeugt = new Date();
		this.farbe = farbe;
		this.groesse = groesse;
		this.vorrat = vorrat;
	}

//	@SuppressWarnings("unused")
	@PrePersist
	private void prePersist() {
		erzeugt = new Date();
		aktualisiert = new Date();
	}

//	@SuppressWarnings("unused")
	@PreUpdate
	private void preUpdate() {
		aktualisiert = new Date();
	}
	
//	@PostUpdate
//	protected void postUpdate() {
//		LOGGER.debugf("Produkt mit ID=%d aktualisiert: version=%d", id, version);
//	}

	public void addBestellposition(Bestellposition bestellPosition) {
		if (bestellPosition != null)
			this.bestellpositionen.add(bestellPosition);
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getBezeichnung() {
		return this.bezeichnung;
	}

	public void setBezeichnung(String bezeichnung) {
		this.bezeichnung = bezeichnung;
	}

	public Float getPreis() {
		return preis;
	}

	public void setPreis(Float preis) {
		this.preis = preis;
	}

	public Date getAktualisiert() {
		return this.aktualisiert;
	}

	public void setAktualisiert(Date aktualisiert) {
		this.aktualisiert = aktualisiert;
	}

	public Date getErzeugt() {
		return this.erzeugt;
	}

	public void setErzeugt(Date erzeugt) {
		this.erzeugt = erzeugt;
	}

	public String getFarbe() {
		return this.farbe;
	}

	public void setFarbe(String farbe) {
		this.farbe = farbe;
	}

	public String getGroesse() {
		return this.groesse;
	}

	public void setGroesse(String groesse) {
		this.groesse = groesse;
	}

	public int getVorrat() {
		return this.vorrat;
	}

	public void setVorrat(int vorrat) {
		this.vorrat = vorrat;
	}
	
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}

	@XmlTransient
	public List<Bestellposition> getBestellpositionen() {
		return this.bestellpositionen;
	}

	public void setBestellpositionen(List<Bestellposition> bestellpositionen) {
		this.bestellpositionen = bestellpositionen;
	}
	
	public void setValues(Produkt p) {
		setVersion(p.getVersion());
		setBezeichnung(p.getBezeichnung());
		setPreis(p.getPreis());
		setFarbe(p.getFarbe());
		setGroesse(p.groesse);
		setVorrat(p.getVorrat());
		
	}

	// Object Methoden
	// ===================================================================================
	@Override
	public String toString() {
		return "Produkt [id=" + getId() + ", version=" + version + ", aktualisiert=" + getAktualisiert()
				+ ", bezeichnung " + getBezeichnung() + ",preis: " + getPreis()
				+ ", erzeugt=" + getErzeugt() + ", farbe=" + getFarbe()
				+ ", groesse=" + getGroesse() + ", vorrat=" + getVorrat()
				+ ", bestellpositionen=" + getBestellpositionen() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((aktualisiert == null) ? 0 : aktualisiert.hashCode());
		result = prime
				* result
				+ ((bestellpositionen == null) ? 0 : bestellpositionen
						.hashCode());
		result = prime * result + ((erzeugt == null) ? 0 : erzeugt.hashCode());
		result = prime * result + ((farbe == null) ? 0 : farbe.hashCode());
		result = prime * result + ((groesse == null) ? 0 : groesse.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + vorrat;
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
		Produkt other = (Produkt) obj;
		if (aktualisiert == null) {
			if (other.aktualisiert != null) {
				return false;
			}
		} 
		else if (!aktualisiert.equals(other.aktualisiert)) {
			return false;
		}
		if (bestellpositionen == null) {
			if (other.bestellpositionen != null) {
				return false;
			}
		} 
		else if (!bestellpositionen.equals(other.bestellpositionen)) {
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
		if (farbe == null) {
			if (other.farbe != null) {
				return false;
			}
		} 
		else if (!farbe.equals(other.farbe)) {
			return false;
		}
		if (groesse == null) {
			if (other.groesse != null) {
				return false;
			}
		} 
		else if (!groesse.equals(other.groesse)) {
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
		return true;
	}
	
	

}
