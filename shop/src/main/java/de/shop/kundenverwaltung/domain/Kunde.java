
package de.shop.kundenverwaltung.domain;

import java.io.Serializable;
import java.net.URI;

import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REMOVE;
import static javax.persistence.FetchType.EAGER;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.TemporalType.TIMESTAMP;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.persistence.UniqueConstraint;

import org.codehaus.jackson.annotate.JsonIgnore;
//Gruppe Tobias Weigel, Florian Vießer, Alex Vollmann, Patrik Steuer
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.ScriptAssert;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.util.File;
import de.shop.util.IdGroup;
import de.shop.util.Constants;
import de.shop.util.Log;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import de.shop.auth.service.jboss.AuthService.RolleType;


/**
 * The persistent class for the kunde database table.
 * 
 */

@NamedQueries({
	@NamedQuery(name  = Kunde.FIND_KUNDEN,
            query = "SELECT k "
			        + " FROM   Kunde as k"),
	@NamedQuery(name  = Kunde.FIND_KUNDE_BY_LOGIN,
			query = "SELECT k "
			    			        + " FROM   Kunde as k"
			    	   			    + " WHERE  k.login = :" + Kunde.PARAM_KUNDE_LOGIN),
	@NamedQuery(name  = Kunde.FIND_LOGIN_BY_LOGIN_PREFIX,
			query = "SELECT   CONCAT('', k.id)"
			    	+ " FROM  Kunde k"
			    	+ " WHERE CONCAT('', k.id) LIKE :" + Kunde.PARAM_LOGIN_PREFIX),
	@NamedQuery(name  = Kunde.FIND_KUNDE_BY_EMAIL,
	   	    query = "SELECT DISTINCT k"
	   			    + " FROM   Kunde k"
	   			    + " WHERE  k.email = :" + Kunde.PARAM_KUNDE_EMAIL),	
	@NamedQuery(name  = Kunde.FIND_KUNDEN_FETCH_BESTELLUNGEN,
			query = "SELECT  DISTINCT k"
					+ " FROM Kunde k LEFT JOIN FETCH k.bestellungen"),
	@NamedQuery(name = Kunde.FIND_KUNDEN_FETCH_ADRESSE,
			query = "SELECT DISTINCT k"
					+ " FROM Kunde k LEFT JOIN FETCH k.adresse"),
	@NamedQuery(name  = Kunde.FIND_KUNDEN_BY_NACHNAME,
            query = "SELECT k"
			        + " FROM   Kunde k"
            		+ " WHERE  UPPER(k.name) = UPPER(:" + Kunde.PARAM_KUNDE_NACHNAME + ")"),
    @NamedQuery(name  = Kunde.FIND_KUNDE_BY_ID,
            query = "SELECT k "
			        + " FROM   Kunde as k"
			        + " WHERE k.id = :" + Kunde.PARAM_KUNDE_ID),      
    @NamedQuery(name  = Kunde.FIND_KUNDE_BY_ID_FETCH_BESTELLUNGEN,
            query = "SELECT k "
			        + " FROM   Kunde as k LEFT JOIN FETCH k.bestellungen"
			        + " WHERE k.id = :" + Kunde.PARAM_KUNDE_ID),   
    @NamedQuery(name  = Kunde.FIND_KUNDE_BY_ID_FETCH_ADRESSE,
            query = "SELECT k "
			        + " FROM   Kunde as k LEFT JOIN FETCH k.adresse"
			        + " WHERE k.id = :" + Kunde.PARAM_KUNDE_ID),
	@NamedQuery(name  = Kunde.FIND_IDS_BY_PREFIX,
		            query = "SELECT   k.id"
					        + " FROM  Kunde k"
					        + " WHERE CONCAT('', k.id) LIKE :" + Kunde.PARAM_KUNDE_ID_PREFIX
					        + " ORDER BY k.id"),
	@NamedQuery(name  = Kunde.FIND_KUNDEN_BY_ID_PREFIX,
					        query = "SELECT   k"
					                + " FROM  Kunde k"
					                + " WHERE CONCAT('', k.id) LIKE :" + Kunde.PARAM_KUNDE_ID_PREFIX
					                + " ORDER BY k.id")                 			        
})

@ScriptAssert(lang = "javascript",
script = "(_this.password == null && _this.passwordWdh == null)"
         + "|| (_this.password != null && _this.password.equals(_this.passwordWdh))",
message = "{kundenverwaltung.kunde.password.notEqual}",
groups = PasswordGroup.class)

@XmlRootElement
//@Formatted
@Entity
@Table(name = "kunde")
public class Kunde implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;

	private static final String PREFIX = "Kunde.";
	public static final String FIND_KUNDEN = PREFIX + "findKunden";
	public static final String FIND_KUNDE_BY_LOGIN = PREFIX + "findKundenByLogin";
	public static final String FIND_LOGIN_BY_LOGIN_PREFIX = PREFIX + "findLoginByLoginPrefix";
	public static final String FIND_KUNDE_BY_EMAIL = PREFIX + "findKundeByEmail";
	public static final String FIND_KUNDE_BY_ID = PREFIX + "findKundeById";
	public static final String FIND_KUNDE_BY_ID_FETCH_BESTELLUNGEN = PREFIX + "findKundeByIdFetchBestellungen";
	public static final String FIND_KUNDE_BY_ID_FETCH_ADRESSE = PREFIX + "findKundeByIdFetchAdresse";	
	public static final String FIND_KUNDEN_FETCH_BESTELLUNGEN = PREFIX + "findKundenFetchBestellungen";
	public static final String FIND_KUNDEN_FETCH_ADRESSE = PREFIX + "findKundenFetchAdresse";
	public static final String FIND_KUNDEN_BY_NACHNAME = PREFIX + "findKundenByNachname";
	public static final String FIND_IDS_BY_PREFIX = PREFIX + "findIdsByIdPrefix";
	public static final String FIND_KUNDEN_BY_ID_PREFIX = PREFIX + "findKundenByIdPrefix";
	
	//Parameter für Namedqueries
	public static final String PARAM_KUNDE_ID = "kundeId";
	public static final String PARAM_KUNDE_NACHNAME = "nachname";
	public static final String PARAM_KUNDE_EMAIL = "email";
	public static final String PARAM_KUNDE_LOGIN = "login";
	public static final String PARAM_LOGIN_PREFIX = "login_prefix";
	public static final String PARAM_KUNDE_ID_PREFIX = "idPrefix";

	@Id
	@GeneratedValue
	@Column(name = "id", nullable = false, updatable = false)
	@Min(value = Constants.MIN_ID, message = "{kundenverwaltung.kunde.id.min}", groups = IdGroup.class)
	@XmlAttribute
	private Long id = null;
	
	@Version
	@Basic(optional = false)
	private int version = Constants.ERSTE_VERSION;

	@Column(name = "AKTUALISIERT", nullable = false)
	@Temporal(TIMESTAMP)
	@XmlTransient
	@JsonIgnore
	private Date aktualisiert;

	@Column(name = "email", length = 255, nullable = false, unique = true)
	@Email(message = "{kundenverwaltung.kunde.email.pattern}")
	private String email;

	@Column(name = "ERZEUGT", nullable = false)
	@XmlTransient
	@JsonIgnore
	private Date erzeugt;

	@Column(name = "GESCHLECHT", length = 1, nullable = true)
	private String geschlecht;

	@Column(name = "LOGIN", length = 20, nullable = false)
	private String login;

	@Column(name = "NAME", length = 45, nullable = false)
	@XmlElement(required = true)
	private String name;

	@Column(name = "PASSWORT", length = 255, nullable = false)
	private String passwort;

	@Column(name = "RABATT", nullable = false)
	private float rabatt;

	@Column(name = "VORNAME", length = 45, nullable = false)
	private String vorname;

	@OneToOne(cascade = { PERSIST, REMOVE }, mappedBy = "kunde")
	@Valid
	@NotNull(message = "{kundenverwaltung.kunde.adresse.notNull}")
	@XmlElement(required = true)
	private Adresse adresse;

	//bi-directional many-to-one association to Bestellung
	@OneToMany		//(mappedBy = "kunde", fetch = FetchType.LAZY)
	@JoinColumn(name = "kunde_id", nullable = false)
	@OrderColumn(name = "idx", nullable = false)
	@XmlTransient
	@JsonIgnore
	private List<Bestellung> bestellungen;
	
	@Transient
	@XmlElement(name = "bestellungen")
	private URI bestellungenUri;
	
	@ElementCollection(fetch = EAGER)
	@CollectionTable(name = "kunde_rolle",
		joinColumns = @JoinColumn(name = "kunde_fk", nullable = false),
		uniqueConstraints =  @UniqueConstraint(columnNames = { "kunde_fk", "rolle_fk" }))
	@Column(table = "kunde_rolle", name = "rolle_fk", nullable = false)
	private Set<RolleType> rollen;
	
	@OneToOne(fetch = LAZY, cascade = { PERSIST, REMOVE })
	@JoinColumn(name = "file_fk")
	@JsonIgnore
	private File file;
	
	@PrePersist
	private void prePersist() {
		this.erzeugt = new Date();
		this.aktualisiert = new Date();
	}
	
	@PreUpdate
	protected void preUpdate() {
		aktualisiert = new Date();
	}
	
	public Kunde() {
		super();
		this.rabatt = 0;
	}

	//Konstruktor
	//fehlen von Plausibilitätsprüfungen
	public Kunde(String email, GeschlechtType geschlecht, String login, String name, String vorname,
				 String passwort, float rabatt) {
		this();
		this.id = null;
		this.email = email;
		this.geschlecht = geschlecht == GeschlechtType.MAENNLICH ?  "m" : "w";
		this.login = login;
		this.name = name;
		this.vorname = vorname;
		this.passwort = passwort;
		this.rabatt = rabatt;
		this.adresse = null;
		}
	public Kunde(String email, String geschlecht, String login, String name, String vorname,
			 String passwort, float rabatt) {
		this(email,  GeschlechtType.valueOf(geschlecht),  login,  name,  vorname,  passwort,  rabatt);
	}
	public Kunde(String email, String geschlecht, String login, String name, String vorname,
			 String passwort, float rabatt, Adresse adresse) {
		this(email,  GeschlechtType.valueOf(geschlecht),  login,  name,  vorname,  passwort,  rabatt);
		this.adresse = adresse;
	}

	//Will add a new Bestellung to the bestellungen List
	public void addBestellung(Bestellung bestellung) {
		if (bestellung != null)
			bestellungen.add(bestellung);
	}
	
	public Adresse getAdresse() {
		return adresse;
	}
	
	public void setAdresse(Adresse adresse) {
		this.adresse = adresse;
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

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getGeschlecht() {
		return this.geschlecht;
	}
	
	public void setGeschlecht(String geschlecht) {
		if  (geschlecht == null)
			return;
		if  (geschlecht.matches("[mw]"))
			this.geschlecht = geschlecht;
		
	}
	
	@JsonIgnore
	public void setGeschlecht(GeschlechtType geschlecht) {
		this.geschlecht = (geschlecht == GeschlechtType.MAENNLICH ?  "m" : "w");
	}
	
	public String getLogin() {
		return this.login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPasswort() {
		return this.passwort;
	}

	public void setPasswort(String passwort) {
		this.passwort = passwort;
	}

	public float getRabatt() {
		return this.rabatt;
	}

	public void setRabatt(float rabatt) {
		this.rabatt = rabatt;
	}

	public String getVorname() {
		return this.vorname;
	}

	public void setVorname(String vorname) {
		this.vorname = vorname;
	}

	public List<Bestellung> getBestellungen() {
		return (this.bestellungen == null ? null : Collections.unmodifiableList(this.bestellungen)); 
	}
	
	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		if(file != null){
			this.file = file;
		}
	}

	public void setBestellungen(List<Bestellung> bestellungen) {
		if (this.bestellungen == null) {
			this.bestellungen = bestellungen;
			return;
		}
		// Wiederverwendung der vorhandenen Collection
		this.bestellungen.clear();
		this.bestellungen.addAll(bestellungen);
	}
	
	public void setValues(Kunde k) {
		setName(k.getName());
		setVorname(k.getVorname());
		setRabatt(k.getRabatt());
		setEmail(k.getEmail());
		setPasswort(k.getPasswort());
		setLogin(k.getLogin());
		setGeschlecht(k.geschlecht);
		setVersion(k.getVersion());
		setFile(k.getFile());
		adresse.setValues(k.getAdresse());
		
	}
	
	public Set<RolleType> getRollen() {
		return rollen;
	}

	public void setRollen(Set<RolleType> rollen) {
		this.rollen = rollen;
	}
	
	@Log
	@Override
	public String toString() {
		return "Kunde [id=" + getId()
			   + ", nachname=" + getName() + ", vorname=" + getVorname()
			   + ", rabatt=" + getRabatt()
			   + ", email=" + getEmail()
			   + ", password=" + getPasswort() 
			   // null muss hier überprüft werden
			   + ", erzeugt=" + (getErzeugt() == null ? "ungesetzt" : getErzeugt().toString())
			   + ", aktualisiert=" + (getAktualisiert() == null ? "ungesetzt" : getAktualisiert().toString()) 
			   + ", Version=" + getVersion() + "]";

	}

	@Log
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result	+ ((aktualisiert == null) ? 0 : aktualisiert.hashCode());
		result = prime * result	+ ((bestellungen == null) ? 0 : bestellungen.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((erzeugt == null) ? 0 : erzeugt.hashCode());
		result = prime * result	+ ((geschlecht == null) ? 0 : geschlecht.hashCode());
		result = prime * result + ((id == null) ? 0 : Float.floatToIntBits(id));
		result = prime * result + ((login == null) ? 0 : login.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result	+ ((passwort == null) ? 0 : passwort.hashCode());
		result = prime * result + Float.floatToIntBits(rabatt);
		result = prime * result + ((vorname == null) ? 0 : vorname.hashCode());
		return result;
	}

	@Log
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
		final Kunde other = (Kunde) obj;
		if (adresse == null) {
			if (other.adresse != null) {
				return false;
			}
		} 
		else if (!adresse.equals(other.adresse)) {
			return false;
		}
		if (aktualisiert == null) {
			if (other.aktualisiert != null) {
				return false;
			}
		} 
		else if (!aktualisiert.equals(other.aktualisiert)) {
			return false;
		}
		if (bestellungen == null) {
			if (other.bestellungen != null) {
				return false;
			}
		} 
		else if (!bestellungen.equals(other.bestellungen)) {
			return false;
		}
		if (email == null) {
			if (other.email != null) {
				return false;
			}
		} 
		else if (!email.equals(other.email)) {
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
		if (geschlecht == null) {
			if (other.geschlecht != null) {
				return false;
			}
		} 
		else if (!geschlecht.equals(other.geschlecht)) {
			return false;
		}
		if (Float.floatToIntBits(id) != Float.floatToIntBits(other.id)) {
			return false;
		}

		if (login == null) {
			if (other.login != null) {
				return false;
			}
		} 
		else if (!login.equals(other.login)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} 
		else if (!name.equals(other.name)) {
			return false;
		}
		if (passwort == null) {
			if (other.passwort != null) {
				return false;
			}
		} 
		else if (!passwort.equals(other.passwort)) {
			return false;
		}
		if (Float.floatToIntBits(rabatt) != Float.floatToIntBits(other.rabatt)) {
			return false;
		}
		if (vorname == null) {
			if (other.vorname != null) {
				return false;
			}
		} 
		else if (!vorname.equals(other.vorname)) {
			return false;
		}
		return true;
	}
	
	@Log
	public Kunde clone() {
		final Kunde result = new Kunde(this.email, this.geschlecht, this.login, this.name, this.vorname, 
					   this.passwort, this.rabatt);
		return result;
	}

	public void setBestellungenUri(URI bestellungenUri) {
		this.bestellungenUri = bestellungenUri;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
}
