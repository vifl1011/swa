package de.shop.bestellverwaltung.domain;

import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REMOVE;
import static javax.persistence.TemporalType.TIMESTAMP;

import java.io.Serializable;
import java.net.URI;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.NamedQuery;
import javax.persistence.OrderColumn;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import org.hibernate.validator.constraints.NotEmpty;
import org.jboss.resteasy.annotations.providers.jaxb.Formatted;

import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.util.Constants;
import de.shop.util.IdGroup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * The persistent class for the bestellung database table.
 * 
 */

@NamedQueries({
		// ToDo - still does not work
		@NamedQuery(name = Bestellung.FIND_BESTELLUNGEN, 
			query = "SELECT b "
				+ "FROM Bestellung b"),
		@NamedQuery(name = Bestellung.FIND_BESTELLUNGEN_FETCH_KUNDE, 
			query = "SELECT distinct b "
				+ "FROM Bestellung b LEFT JOIN FETCH b.kunde"),
		@NamedQuery(name = Bestellung.FIND_BESTELLUNGEN_BY_DATE, 
			query = "SELECT b "
				+ "FROM Bestellung b "
				+ "WHERE b.erzeugt = :"
				+ Bestellung.PARAM_BESTELLUNG_DATE),
		@NamedQuery(name = Bestellung.FIND_BESTELLUNGEN_BY_KUNDE_ID, 
			query = "SELECT b "
				+ "FROM Bestellung b "
				+ "WHERE b.kunde.id = :"
				+ Bestellung.PARAM_BESTELLUNG_KUNDE_ID),
		@NamedQuery(name = Bestellung.FIND_BESTELLUNG_BY_ID, query = "SELECT b "
				+ "FROM Bestellung b "
				+ "WHERE b.id = :"
				+ Bestellung.PARAM_BESTELLUNG_ID),
		@NamedQuery(name = Bestellung.FIND_BESTELLUNG_BY_ID_FETCH_POSITIONEN, query = "SELECT DISTINCT b "
				+ "FROM Bestellung b "
				+ "LEFT JOIN FETCH b.bestellpositionen "
				+ "WHERE b.id = :" + Bestellung.PARAM_BESTELLUNG_ID)
})

@XmlRootElement
@Formatted
@Entity
@Table(name = "bestellung")
public class Bestellung implements Serializable {
	/**
	 * Generated Serial Version UID
	 */
	private static final long serialVersionUID = -219732051634085511L;
	
	
	public static final String PREFIX = "Bestellung.";
	public static final String FIND_BESTELLUNGEN = PREFIX + "findBestellungen";
	public static final String FIND_BESTELLUNGEN_FETCH_KUNDE = PREFIX
			+ "findBestellungenFetchKunde";
	public static final String FIND_BESTELLUNGEN_BY_KUNDE_ID = PREFIX
			+ "findBestellungenByKundeId";
	public static final String FIND_BESTELLUNGEN_BY_DATE = PREFIX
			+ "findBestellungenByDate";
	public static final String FIND_BESTELLUNG_BY_ID = PREFIX
			+ "findBestellungById";
	public static final String FIND_BESTELLUNG_BY_ID_FETCH_POSITIONEN = PREFIX
			+ "findBestellungByIdFetchPositionen";
	public static final String FIND_BESTELLUNG_BY_KUNDE_ORDER_BY_IDX_DESC = PREFIX
			+ "findBestellungByKundeIdOrderByIdxDesc";

	// Parameter für Namedqueries
	public static final String PARAM_BESTELLUNG_ID = "bestellungId";
	public static final String PARAM_BESTELLUNG_ID_PREFIX = "idPrefix";
	public static final String PARAM_BESTELLUNG_DATE = "bestllungDate";
	public static final String PARAM_BESTELLUNG_KUNDE_ID = "bestellungKundeId";


	@Id
	@GeneratedValue
	@Column(name = "id", unique = true, nullable = false, updatable = false, precision = Constants.LONG_ANZ_ZIFFERN)
	@Min(value = Constants.MIN_ID, message = "{bestellverwaltung.bestellung.id.min}", groups = IdGroup.class)
	@XmlAttribute
	private Long id = Constants.KEINE_ID;

	@Column(name = "aktualisiert", nullable = false)
	@Temporal(TIMESTAMP)
	@XmlTransient
	@JsonIgnore
	private Date aktualisiert;

	@NotNull
	@NotEmpty
	private String bestellstatus;

	@Temporal(TIMESTAMP)
	@XmlTransient
	@JsonIgnore
	private Date bestellzeitpunkt;

	@Column(name = "erzeugt", nullable = false)
	@Temporal(TIMESTAMP)
	@XmlTransient
	@JsonIgnore
	private Date erzeugt;

	@Min(value = (long) 0, message = "gesamtpreis darf nicht negativ sein")
	@Column(name = "gesamtpreis", nullable = false)
	private float gesamtpreis;

	@NotNull
	private byte gezahlt;

	// bi-directional many-to-one association to Bestellposition
	@OneToMany(fetch = FetchType.EAGER, cascade = { PERSIST, REMOVE })
	@JoinColumn(name = "bestellung_id", nullable = false, updatable = false)
	@OrderColumn(name = "idx", nullable = false)
	@XmlElement
	@JsonProperty
	private List<Bestellposition> bestellpositionen;


	// bi-directional many-to-one association to Kunde
	@ManyToOne(optional = false)
	@JoinColumn(name = "kunde_id", nullable = false, updatable = false, insertable = false)
	@JsonIgnore
	private Kunde kunde;
	
	@Transient
	@XmlElement(name = "kunde", required = true)
	@JsonProperty
	private URI kundeUri;

	// Standardkonstruktor
	public Bestellung() {
		super();
		bestellpositionen = new ArrayList<>();
		this.gezahlt = 0;
		this.bestellstatus = "offen";
	}
	
	public Bestellung(String bestellstatus, float gesamtpreis, byte gezahlt, Bestellposition bestellposition, 
			Kunde kunde) {
		this();
		this.bestellstatus = bestellstatus;
		this.gesamtpreis = gesamtpreis;
		this.gezahlt = gezahlt;
		this.addBestellposition(bestellposition);
		bestellposition.setBestellung(this);
		this.kunde = kunde;
		kunde.addBestellung(this);
	}

	public Bestellung(List<Bestellposition> bestellpositionen) {
		this();		
		this.bestellpositionen = bestellpositionen;
	}

	public void addBestellposition(Bestellposition bestellposition) {
		if (bestellposition == null)
			return;
		if (bestellpositionen == null) {
			bestellpositionen = new ArrayList<>();
		}

		bestellpositionen.add(bestellposition);
		gesamtpreis += bestellposition.getEinzelpreis();
	}

	

	@PrePersist
	public void prePersist() {
		this.erzeugt = new Date();
		this.aktualisiert = new Date();
	}

	@PreUpdate
	public void preUpdate() {
		this.aktualisiert = new Date();
	}

	// Getter-/Setter
	// ...................................
	@XmlTransient
	public URI getKundeUri() {
		return kundeUri;
	}
	
	public void setKundeUri(URI kundeUri) {
		this.kundeUri = kundeUri;
	}
	
	@XmlTransient
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@XmlTransient
	public Date getAktualisiert() {
		if (aktualisiert != null)
			return (Date) this.aktualisiert.clone();
		else
			return null;
	}

	public void setAktualisiert(Date aktualisiert) {
		this.aktualisiert = aktualisiert == null ? null : (Date) aktualisiert.clone();
	}
	
	public String getAktualisiertStr() {
		if (aktualisiert == null)
			return null;
		
		return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(aktualisiert);
	}
	
	public void setAktualisiertStr(String date) throws ParseException {
			aktualisiert = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date);
	}

	public String getBestellstatus() {
		return this.bestellstatus;
	}

	public void setBestellstatus(String bestellstatus) {
		this.bestellstatus = bestellstatus;
	}
	
	@XmlTransient
	public Date getBestellzeitpunkt() {
		return this.bestellzeitpunkt;
	}

	public void setBestellzeitpunkt(Date bestellzeitpunkt) {
		this.bestellzeitpunkt = bestellzeitpunkt;
	}

	public String getBestellzeitpunktStr() {
		if (bestellzeitpunkt == null)
			return null;
		
		return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(bestellzeitpunkt);
	}
	
	public void setBestellzeitpunktStr(String date) throws ParseException {
			bestellzeitpunkt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date);
	}

	@XmlTransient
	public Date getErzeugt() {
		return this.erzeugt;
	}

	public void setErzeugt(Date erzeugt) {
		this.erzeugt = erzeugt;
	}

	public String getErzeugtStr() {
		if (erzeugt == null)
			return null;
		
		return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(erzeugt);
	}
	
	public void setErzeugtStr(String date) throws ParseException {
			erzeugt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date);
	}
	
	public float getGesamtpreis() {
		return this.gesamtpreis;
	}

	public void setGesamtpreis(float gesamtpreis) {
		this.gesamtpreis = gesamtpreis;
	}

	public byte getGezahlt() {
		return this.gezahlt;
	}

	public void setGezahlt(byte gezahlt) {
		this.gezahlt = gezahlt;
	}

	@XmlTransient
	@JsonIgnore
	public List<Bestellposition> getBestellpositionen() {
		return this.bestellpositionen;
	}

	public void setBestellpositionen(List<Bestellposition> bestellpositionen) {
		this.bestellpositionen = bestellpositionen;
	}

	@XmlTransient
	public Kunde getKunde() {
		return this.kunde;
	}

	public void setKunde(Kunde kunde) {
		this.kunde = kunde;
	}
	
	public void setValues(Bestellung bestellung) {
		bestellstatus = bestellung.getBestellstatus();
		bestellzeitpunkt = bestellung.getBestellzeitpunkt();
		gezahlt = bestellung.getGezahlt();
		bestellpositionen = bestellung.getBestellpositionen();
	}

	@Override
	public String toString() {
		return "Bestellung [id=" + getId() 
				+ ", aktualisiert=" + getAktualisiert() 
				+ ", bestellstatus=" + getBestellstatus()
				+ ", bestellzeitpunkt=" + getBestellzeitpunkt() 
				+ ", erzeugt=" + getErzeugt() 
				+ ", gesamtpreis=" + getGesamtpreis()
				+ ", gezahlt=" + getGezahlt() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((aktualisiert == null) ? 0 : aktualisiert.hashCode());
		result = prime * result + ((bestellpositionen == null) ? 0 : bestellpositionen.hashCode());
		result = prime * result + ((bestellstatus == null) ? 0 : bestellstatus.hashCode());
		result = prime * result + ((bestellzeitpunkt == null) ? 0 : bestellzeitpunkt.hashCode());
		result = prime * result + ((erzeugt == null) ? 0 : erzeugt.hashCode());
		result = prime * result + Float.floatToIntBits(gesamtpreis);
		result = prime * result + gezahlt;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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

		Bestellung other = (Bestellung) obj;

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
		if (bestellstatus == null) {
			if (other.bestellstatus != null) {
				return false;
			}
		} 
		else if (!bestellstatus.equals(other.bestellstatus)) {
			return false;
		}
		if (bestellzeitpunkt == null) {
			if (other.bestellzeitpunkt != null) {
				return false;
			}
		} 
		else if (!bestellzeitpunkt.equals(other.bestellzeitpunkt)) {
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
		if (Float.floatToIntBits(gesamtpreis) != Float
				.floatToIntBits(other.gesamtpreis)) {
			return false;
		}
		if (gezahlt != other.gezahlt) {
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
		return true;
	}
}
