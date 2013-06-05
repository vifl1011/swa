package de.shop.bestellverwaltung.domain;

import java.io.Serializable;
import java.net.URI;

import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import de.shop.artikelverwaltung.domain.Produkt;
import de.shop.util.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * The persistent class for the bestellposition database table.
 * 
 */
@Entity
@Table(name = "bestellposition")
@Cacheable
@NamedQueries({
		@NamedQuery(name = Bestellposition.FIND_BESTELLPOSITIONEN_BY_KUNDE, 
			query = "select b from Bestellposition as b"
				+ " JOIN b.bestellung c"
				+ " JOIN c.kunde d"
				+ " WHERE d.id = :id"),
		@NamedQuery(name = Bestellposition.FIND_BESTELLPOSITIONEN_BY_BESTELLUNG_ID, 
			query = "SELECT b FROM Bestellposition as b "
				+ "Where b.bestellung.id = :"
				+ Bestellposition.PARAM_BESTELLUNG_ID),
		@NamedQuery(name = Bestellposition.FIND_BESTELLPOSITIONEN_BY_BESTELLUNG_BY_PRODUKT, 
			query = "SELECT b FROM Bestellposition as b "
				+ "Where b.bestellung.id = :"
				+ Bestellposition.PARAM_BESTELLUNG_ID
				+ " AND b.produkt.id = :"
				+ Bestellposition.PARAM_PRODUKT_ID),
		@NamedQuery(name = Bestellposition.FIND_BESTELLPOSITION_BY_ID,
					query = "select b from Bestellposition as b where b.id = :" + Bestellposition.PARAM_ID)
		})

public class Bestellposition implements Serializable {
	/**
	 * Generated Serial Version UID
	 */
	private static final long serialVersionUID = -9055224075649831017L;
	
	// Named Queries
	public static final String PREFIX = "Bestellposition.";
	public static final String FIND_BESTELLPOSITIONEN_BY_KUNDE = 
			PREFIX + "findBestellpositionenByKunde";
	public static final String FIND_BESTELLPOSITIONEN_BY_BESTELLUNG_ID = 
			PREFIX + "findBestellpositionenByBestellungId";
	public static final String FIND_BESTELLPOSITIONEN_BY_BESTELLUNG_BY_PRODUKT = 
			PREFIX + "findBestellpositionByBestellungIdByProduktId";
	public static final String FIND_BESTELLPOSITION_BY_ID = 
			PREFIX + "findBestellpositionById";

	// Query Parameters
	public static final String PARAM_ID = "BestellpositionId";
	public static final String PARAM_BESTELLUNG_ID = "BestellungId";
	public static final String PARAM_PRODUKT_ID = "ProduktId";
	
	@Id
	@GeneratedValue
	@Column(name = "id", unique = true, nullable = false, updatable = false, precision = Constants.LONG_ANZ_ZIFFERN)
	private Long id = Constants.KEINE_ID;

	@Version
	@Basic(optional = false)
	@Column(name = "version")
	private int version = 0;

	@JsonIgnore
	private Date aktualisiert;

	@Min(value = Constants.ANZAHL_MIN, message = "{bestellverwaltung.bestellposition.preis.min}")
	private float einzelpreis;

	//@Column(nullable = false, updatable = false)
	@JsonIgnore
	private Date erzeugt;

	@Min(value = Constants.ANZAHL_MIN, message = "{bestellverwaltung.bestellposition.anzahl.min}")
	private int menge;

	// bi-directional many-to-one association to Produkt
	@ManyToOne(optional = false)
	@JoinColumn(name = "PRODUKT_ID", nullable = false, updatable = false)
	@NotNull(message = "{bestellverwaltung.bestellposition.artikel.notNull}")
	@JsonIgnore
	private Produkt produkt;
	
	@Transient
	@JsonProperty
	private URI produktUri;

	// bi-directional many-to-one association to Lieferung
	@ManyToOne
	@JoinColumn(name = "LIEFERUNG_ID", nullable = true)
	//@NotEmpty(message = "{bestellverwaltung.bestellposition.lieferung.notNull}")
	@JsonIgnore
	private Lieferung lieferung;
	
	@Transient
	@JsonProperty
	private URI lieferungUri;

	// bi-directional many-to-one association to Bestellung
	@ManyToOne(optional = false)
	@JoinColumn(name = "BESTELLUNG_ID", insertable = false, nullable = false, updatable = false)
	@NotNull(message = "{bestellverwaltung.bestellposition.bestellung.notNull}")
	@JsonIgnore
	private Bestellung bestellung;
	
	@Transient
	@JsonProperty
	private URI bestellungUri;
	

	public Bestellposition(Bestellung bestellung, Lieferung lieferung, Produkt produkt, int me) {
		this();
		this.aktualisiert = null;
		this.einzelpreis = produkt.getPreis();
		this.id = null;
		this.menge = me;
		this.bestellung = bestellung;
		this.lieferung = lieferung;
		this.produkt = produkt;

		// Verweise setzen
		bestellung.addBestellposition(this);
	}
	
	public Bestellposition(Produkt produkt) {
		this();
		this.aktualisiert = null;
		this.einzelpreis = produkt.getPreis();
		this.id = null;
		this.menge = 1;
		this.produkt = produkt;
	}

	public Bestellposition() {
		super();
		//ToDo erzeugt und aktualisiert sollten automatisch vor dem persist erzeugt werden
		//defacto wird aber leider nur das feld aktualsiert mit einem neuen Datum versehen
		//was beim erstellen einer neuen Bestellposition zu einer NullPointerException führt - bitte beheben
		this.erzeugt = new Date();
		this.aktualisiert = new Date();
	}

	@PrePersist
	public void prePersist() {
		this.erzeugt = new Date();
		this.aktualisiert = new Date();
		this.version++;
	}

	@PreUpdate
	public void preUpdate() {
		this.aktualisiert = new Date();
		this.version++;
	}

	// Getter-/Setter
	// ...................................
	public URI getProduktUri() {
		return produktUri;
	}
	
	public void setProduktUri(URI produktUri) {
		this.produktUri = produktUri;
	}
	
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
	
	public URI getBestellungUri() {
		return bestellungUri;
	}
	
	public void setBestellungUri(URI bestellungUri) {
		this.bestellungUri = bestellungUri;
	}
	
	public URI getLieferungUri() {
		return lieferungUri;
	}
	
	public void setLieferungUri(URI lieferungUri) {
		this.lieferungUri = lieferungUri;
	}
	
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getAktualisiert() {
		if (aktualisiert != null)
			return (Date) this.aktualisiert.clone();
		else
			return null;
	}

	public void setAktualisiert(Date aktualisiert) {
		this.aktualisiert = aktualisiert == null ? null : (Date) aktualisiert
				.clone();
	}
	
	public String getAktualisiertStr() {
		if (aktualisiert == null)
			return null;
		
		return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(aktualisiert);
	}
	
	public void setAktualisiertStr(String date) throws ParseException {
		try {
			aktualisiert = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date);
		} catch (ParseException e) {
			throw e;
		}
	}

	public float getEinzelpreis() {
		return this.einzelpreis;
	}

	public void setEinzelpreis(float einzelpreis) {
		this.einzelpreis = einzelpreis;
	}

	public Date getErzeugt() {
		return (Date) this.erzeugt.clone();
	}
	
	public void setErzeugt(Date erz) {
		this.erzeugt = erzeugt == null ? null : (Date) erz.clone();
	}
	
	public String getErzeugtStr() {
		if (erzeugt == null)
			return null;
		
		return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(erzeugt);
	}
	
	public void setErzeugtStr(String date) throws ParseException {
		try {
			erzeugt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date);
		} catch (ParseException e) {
			throw e;
		}
	}

	public int getMenge() {
		return this.menge;
	}

	public void setMenge(int menge) {
		this.menge = menge;
	}

	public Produkt getProdukt() {
		return this.produkt;
	}

	public void setProdukt(Produkt produkt) {
		this.produkt = produkt;
	}

	public Lieferung getLieferung() {
		return this.lieferung;
	}

	public void setLieferung(Lieferung lieferung) {
		this.lieferung = lieferung;
	}

	public Bestellung getBestellung() {
		return this.bestellung;
	}

	public void setBestellung(Bestellung bestellung) {
		this.bestellung = bestellung;
	}
	
	public void setValues(Bestellposition bestellposition) {
		einzelpreis = bestellposition.getEinzelpreis();
		version = bestellposition.getVersion();
		menge = bestellposition.getMenge();
		produkt = bestellposition.getProdukt();
		produktUri = bestellposition.getProduktUri();
		lieferung = bestellposition.getLieferung();
		lieferungUri = bestellposition.getLieferungUri();
	}

	@Override
	public String toString() {
		String dat;
		if (aktualisiert == null)
			dat = "null";
		else
			dat = getAktualisiert().toString();

		return "Bestellposition [id=" + getId() 
				+ ", aktualisiert=" + dat
				+ ", einzelpreis=" + getEinzelpreis() 
				+ ", erzeugt=" + getErzeugt()
				+ ", menge=" + getMenge()
				+ ", version=" + getVersion()
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((aktualisiert == null) ? 0 : aktualisiert.hashCode());
		// result = prime * result + ((bestellung == null) ? 0 :
		// bestellung.hashCode());
		result = (int) (prime * result + einzelpreis);
		result = prime * result + ((erzeugt == null) ? 0 : erzeugt.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		// result = prime * result + idx;
		result = prime * result + ((lieferung == null) ? 0 : lieferung.hashCode());
		result = prime * result + menge;
		result = prime * result + ((produkt == null) ? 0 : produkt.hashCode());
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
		final Bestellposition other = (Bestellposition) obj;
		if (aktualisiert == null) {
			if (other.aktualisiert != null) {
				return false;
			}
		} 
		else if (!aktualisiert.equals(other.aktualisiert)) {
			return false;
		}
		if (bestellung == null) {
			if (other.bestellung != null) {
				return false;
			}
		} 
		else if (!bestellung.equals(other.bestellung)) {
			return false;
		}
		if (einzelpreis != other.einzelpreis) {
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
		/*
		 * if (idx != other.idx) { return false; }
		 */
		if (lieferung == null) {
			if (other.lieferung != null) {
				return false;
			}
		} 
		else if (!lieferung.equals(other.lieferung)) {
			return false;
		}
		if (menge != other.menge) {
			return false;
		}
		if (produkt == null) {
			if (other.produkt != null) {
				return false;
			}
		} 
		else if (!produkt.equals(other.produkt)) {
			return false;
		}
		return true;
	}
}

