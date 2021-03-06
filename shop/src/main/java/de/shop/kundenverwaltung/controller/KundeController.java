package de.shop.kundenverwaltung.controller;

import static de.shop.util.Constants.JSF_INDEX;
import static de.shop.util.Constants.JSF_REDIRECT_SUFFIX;
import static de.shop.util.Messages.MessagesType.KUNDENVERWALTUNG;
import static javax.ejb.TransactionAttributeType.REQUIRED;
import static javax.ejb.TransactionAttributeType.SUPPORTS;
import static javax.persistence.PersistenceContextType.EXTENDED;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.groups.Default;
import javax.xml.bind.DatatypeConverter;

import org.jboss.logging.Logger;
import org.richfaces.cdi.push.Push;
import org.richfaces.component.SortOrder;
import org.richfaces.component.UIPanelMenuItem;
import org.richfaces.event.FileUploadEvent;
import org.richfaces.model.UploadedFile;

import de.shop.auth.controller.AuthController;
import de.shop.auth.controller.KundeLoggedIn;
import de.shop.kundenverwaltung.domain.Adresse;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.domain.PasswordGroup;
import de.shop.kundenverwaltung.service.EmailExistsException;
import de.shop.kundenverwaltung.service.InvalidKundeException;
import de.shop.kundenverwaltung.service.InvalidNachnameException;
import de.shop.kundenverwaltung.service.KundeDeleteBestellungException;
import de.shop.kundenverwaltung.service.KundeService;
import de.shop.kundenverwaltung.service.KundeService.FetchType;
import de.shop.util.AbstractShopException;
import de.shop.util.Client;
import de.shop.util.ConcurrentDeletedException;
import de.shop.util.File;
import de.shop.util.FileHelper;
import de.shop.util.Log;
import de.shop.util.Messages;
import static de.shop.auth.service.jboss.AuthService.ABTEILUNGSLEITER;
import static de.shop.auth.service.jboss.AuthService.ADMIN;
import static de.shop.auth.service.jboss.AuthService.KUNDE;
import static de.shop.auth.service.jboss.AuthService.MITARBEITER;

/**
 * Dialogsteuerung fuer die Kundenverwaltung
 */
@Named("kc")
@Log
@SessionScoped
@Stateful
@TransactionAttribute(SUPPORTS)
@RolesAllowed({ ADMIN, KUNDE, MITARBEITER, ABTEILUNGSLEITER })
public class KundeController implements Serializable {
	private static final long serialVersionUID = -8817180909526894740L;

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());

	private static final int MAX_AUTOCOMPLETE = 10;

	private static final String JSF_KUNDENVERWALTUNG = "/kundenverwaltung/";
	private static final String JSF_VIEW_KUNDE = JSF_KUNDENVERWALTUNG + "viewKunde";
	private static final String JSF_LIST_KUNDEN = JSF_KUNDENVERWALTUNG + "/listKunden";
	private static final String JSF_UPDATE_KUNDE = JSF_KUNDENVERWALTUNG + "updateKunde";
	private static final String JSF_DELETE_OK = JSF_KUNDENVERWALTUNG + "okDelete";

	private static final String REQUEST_KUNDE_ID = "kundeId";

	private static final String CLIENT_ID_KUNDEID = "form:kundeIdInput";
	private static final String MSG_KEY_KUNDE_NOT_FOUND_BY_ID = "viewKunde.notFound";

	private static final String CLIENT_ID_KUNDEN_NAME = "form:name";
	private static final String MSG_KEY_KUNDEN_NOT_FOUND_BY_NAME = "listKunden.notFound";

	private static final String CLIENT_ID_CREATE_EMAIL = "createKundeForm:email";
	private static final String MSG_KEY_CREATE_KUNDE_EMAIL_EXISTS = "createKunde.emailExists";

	private static final Class<?>[] PASSWORD_GROUP = {PasswordGroup.class };
	private static final Class<?>[] DEFAULT_GROUP = {Default.class, PasswordGroup.class };
	private static final String CLIENT_ID_UPDATE_PASSWORD = "updateKundeForm:password";
	private static final String CLIENT_ID_UPDATE_EMAIL = "updateKundeForm:email";
	private static final String MSG_KEY_UPDATE_KUNDE_DUPLIKAT = "updateKunde.duplikat";
	private static final String MSG_KEY_UPDATE_KUNDE_CONCURRENT_UPDATE = "updateKunde.concurrentUpdate";
	private static final String MSG_KEY_UPDATE_KUNDE_CONCURRENT_DELETE = "updateKunde.concurrentDelete";

	private static final String MSG_KEY_SELECT_DELETE_KUNDE_BESTELLUNG = "listKunden.deleteKundeBestellung";

	private static final String CLIENT_ID_DELETE_BUTTON = "form:deleteButton";
	private static final String MSG_KEY_DELETE_KUNDE_BESTELLUNG = "viewKunde.deleteKundeBestellung";

	@PersistenceContext(type = EXTENDED)
	private transient EntityManager em;

	@Inject
	private KundeService ks;

	@Inject
	private transient HttpServletRequest request;

	@Inject
	private AuthController auth;

	@Inject
	@Client
	private Locale locale;

	@Inject
	private Messages messages;

	@Inject
	@Push(topic = "marketing")
	private transient Event<String> neuerKundeEvent;

	@Inject
	@Push(topic = "updateKunde")
	private transient Event<String> updateKundeEvent;

	@Inject
	private FileHelper fileHelper;

	@Inject
	@KundeLoggedIn
	private Kunde user;

	private Long kundeId;
	private Kunde kunde;

	private String name;

	private List<Kunde> kunden = Collections.emptyList();

	private SortOrder vornameSortOrder = SortOrder.unsorted;
	private String vornameFilter = "";

	private boolean geaendertKunde; // fuer ValueChangeListener
	private Kunde neuerKunde;

	private byte[] bytes;
	private String contentType;

	private transient UIPanelMenuItem menuItemEmail; // eigentlich nicht
														// dynamisch, nur zur
														// Demo

	@PostConstruct
	private void postConstruct() {
		LOGGER.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
	}

	@PreDestroy
	private void preDestroy() {
		LOGGER.debugf("CDI-faehiges Bean %s wird geloescht", this);
	}

	@Override
	public String toString() {
		return "KundenverwaltungController [kundeId=" + kundeId + ", name=" + name + ", geaendertKunde="
				+ geaendertKunde + "]";
	}

	public Long getKundeId() {
		return kundeId;
	}

	public void setKundeId(Long kundeId) {
		this.kundeId = kundeId;
	}

	public Kunde getKunde() {
		return kunde;
	}

	public String getName() {
		return name;
	}

	public void setName(String nachname) {
		this.name = nachname;
	}

	public List<Kunde> getKunden() {
		return kunden;
	}

	public SortOrder getVornameSortOrder() {
		return vornameSortOrder;
	}

	public void setVornameSortOrder(SortOrder vornameSortOrder) {
		this.vornameSortOrder = vornameSortOrder;
	}

	public void sortByVorname() {
		vornameSortOrder = vornameSortOrder.equals(SortOrder.ascending) ? SortOrder.descending : SortOrder.ascending;
	}

	public String getVornameFilter() {
		return vornameFilter;
	}

	public void setVornameFilter(String vornameFilter) {
		this.vornameFilter = vornameFilter;
	}

	public Kunde getNeuerKunde() {
		return neuerKunde;
	}

	public void setMenuItemEmail(UIPanelMenuItem menuItemEmail) {
		this.menuItemEmail = menuItemEmail;
	}

	public UIPanelMenuItem getMenuItemEmail() {
		return menuItemEmail;
	}

	@PermitAll
	public Date getAktuellesDatum() {
		final Date datum = new Date();
		return datum;
	}

	/**
	 * Action Methode, um einen Kunden zu gegebener ID zu suchen
	 * 
	 * @return URL fuer Anzeige des gefundenen Kunden; sonst null
	 */
	@TransactionAttribute(REQUIRED)
	@RolesAllowed({ MITARBEITER, ABTEILUNGSLEITER, ADMIN })
	public String findKundeById() {
		// Bestellungen werden durch "Extended Persistence Context" nachgeladen
		kunde = ks.findKundeById(kundeId, FetchType.NUR_KUNDE, locale);

		if (kunde == null) {
			// Kein Kunde zu gegebener ID gefunden
			return findKundeByIdErrorMsg(kundeId.toString());
		}

		return JSF_VIEW_KUNDE;
	}

	@TransactionAttribute(REQUIRED)
	public String findUser() {
		kunde = ks.findKundeById(user.getId(), FetchType.NUR_KUNDE, locale);
		if (kunde == null) {
			// Kein Kunde zu gegebener ID gefunden
			return findKundeByIdErrorMsg(kundeId.toString());
		}

		return JSF_VIEW_KUNDE;
	}

	private String findKundeByIdErrorMsg(String id) {
		messages.error(KUNDENVERWALTUNG, MSG_KEY_KUNDE_NOT_FOUND_BY_ID, CLIENT_ID_KUNDEID, id);
		return null;
	}

	/**
	 * F&uuml;r rich:autocomplete
	 * 
	 * @return Liste der potenziellen Kunden
	 */

	@TransactionAttribute(REQUIRED)
	@RolesAllowed({ MITARBEITER, ABTEILUNGSLEITER, ADMIN })
	public List<Kunde> findKundenByIdPrefix(String idPrefix) {
		List<Kunde> kundenPrefix = null;
		Long id = null;
		try {
			id = Long.valueOf(idPrefix);
		} catch (NumberFormatException e) {
			findKundeByIdErrorMsg(idPrefix);
			return null;
		}

		kundenPrefix = ks.findKundenByIdPrefix(id);
		if (kundenPrefix == null || kundenPrefix.isEmpty()) {
			// Kein Kunde zu gegebenem ID-Praefix vorhanden
			findKundeByIdErrorMsg(idPrefix);
			return null;
		}

		if (kundenPrefix.size() > MAX_AUTOCOMPLETE) {
			return kundenPrefix.subList(0, MAX_AUTOCOMPLETE);
		}
		return kundenPrefix;
	}

	@TransactionAttribute(REQUIRED)
	@RolesAllowed({ MITARBEITER, ABTEILUNGSLEITER, ADMIN })
	public void loadKundeById() {
		// Request-Parameter "kundeId" fuer ID des gesuchten Kunden
		final String idStr = request.getParameter("kundeId");
		Long id;
		try {
			id = Long.valueOf(idStr);
		} catch (NumberFormatException e) {
			return;
		}

		// Suche durch den Anwendungskern
		kunde = ks.findKundeById(id, FetchType.NUR_KUNDE, locale);
		if (kunde == null) {
			return;
		}
	}

	@TransactionAttribute(REQUIRED)
	@RolesAllowed({ MITARBEITER, ABTEILUNGSLEITER, ADMIN })
	public String findKundenByName() {
		LOGGER.debugf("name:%s:", name);
		if (name == null || name.isEmpty()) {
			kunden = ks.findAllKunden(FetchType.MIT_BESTELLUNGEN);
			return JSF_LIST_KUNDEN;
		}

		try {
			kunden = ks.findKundenByName(name, FetchType.MIT_BESTELLUNGEN, locale);
		} catch (InvalidNachnameException e) {
			final Collection<ConstraintViolation<Kunde>> violations = e.getViolations();
			messages.error(violations, CLIENT_ID_KUNDEN_NAME);
			return null;
		}
		return JSF_LIST_KUNDEN;
	}

	@TransactionAttribute(REQUIRED)
	@RolesAllowed({ MITARBEITER, ABTEILUNGSLEITER, ADMIN })
	public String details(Kunde ausgewaehlterKunde) {
		if (ausgewaehlterKunde == null) {
			return null;
		}

		// Bestellungen nachladen
		this.kunde = ks.findKundeById(ausgewaehlterKunde.getId(), FetchType.MIT_BESTELLUNGEN, locale);
		this.kundeId = this.kunde.getId();

		return JSF_VIEW_KUNDE;
	}

	@TransactionAttribute(REQUIRED)
	@RolesAllowed({ MITARBEITER, ABTEILUNGSLEITER, ADMIN })
	public String createKunde() {

		try {
			neuerKunde = (Kunde) ks.createKunde(neuerKunde, locale);
		} catch (InvalidKundeException | EmailExistsException e) {
			final String outcome = createKundeErrorMsg(e);
			return outcome;
		}

		// Push-Event fuer Webbrowser
		neuerKundeEvent.fire(String.valueOf(neuerKunde.getId()));

		// Aufbereitung fuer viewKunde.xhtml
		kundeId = neuerKunde.getId();
		kunde = neuerKunde;
		neuerKunde = null; // zuruecksetzen

		return JSF_VIEW_KUNDE + JSF_REDIRECT_SUFFIX;
	}

	private String createKundeErrorMsg(AbstractShopException e) {
		final Class<? extends AbstractShopException> exceptionClass = e.getClass();
		if (exceptionClass.equals(EmailExistsException.class)) {
			messages.error(KUNDENVERWALTUNG, MSG_KEY_CREATE_KUNDE_EMAIL_EXISTS, CLIENT_ID_CREATE_EMAIL);
		} 
		else if (exceptionClass.equals(InvalidKundeException.class)) {
			final InvalidKundeException orig = (InvalidKundeException) e;
			messages.error(orig.getViolations(), null);
		}
		return null;
	}

	public void createEmptyKunde() {
		if (neuerKunde != null) {
			return;
		}
		LOGGER.tracef("BEGIN createEmptyKunde()");
		neuerKunde = new Kunde();
		final Adresse adresse = new Adresse();
		neuerKunde.setAdresse(adresse);
		adresse.setKunde(neuerKunde);
		LOGGER.errorf("END createEmptyKunde() %s", neuerKunde.getAdresse().toString());
	}

	/**
	 * https://issues.jboss.org/browse/AS7-1348
	 * http://community.jboss.org/thread/169487
	 */

	public Class<?>[] getPasswordGroup() {
		return PASSWORD_GROUP.clone();
	}
	
	public Class<?>[] getDefaultGroup() {
		return DEFAULT_GROUP.clone();
	}

	/**
	 * Verwendung als ValueChangeListener bei updateKunde.xhtml und
	 * updateFirmenkunde.xhtml
	 */
	public void geaendert(ValueChangeEvent e) {
		if (geaendertKunde) {
			return;
		}

		if (e.getOldValue() == null) {
			if (e.getNewValue() != null) {
				geaendertKunde = true;
			}
			return;
		}

		if (!e.getOldValue().equals(e.getNewValue())) {
			geaendertKunde = true;
		}
	}

	@TransactionAttribute(REQUIRED)
	public String update() {
		auth.preserveLogin();

		if (!geaendertKunde || kunde == null) {
			return JSF_INDEX;
		}

		LOGGER.tracef("Aktualisierter Kunde: %s", kunde);
		try {
			kunde = ks.updateKunde(kunde, locale);
		} catch (EmailExistsException | InvalidKundeException 
				| OptimisticLockException | ConcurrentDeletedException e) {
			final String outcome = updateErrorMsg(e, kunde.getClass());
			return outcome;
		}

		// Push-Event fuer Webbrowser
		updateKundeEvent.fire(String.valueOf(kunde.getId()));

		// ValueChangeListener zuruecksetzen
		geaendertKunde = false;

		// Aufbereitung fuer viewKunde.xhtml
		kundeId = kunde.getId();

		return JSF_VIEW_KUNDE + JSF_REDIRECT_SUFFIX;
	}

	private String updateErrorMsg(RuntimeException e, Class<? extends Kunde> kundeClass) {
		final Class<? extends RuntimeException> exceptionClass = e.getClass();
		if (exceptionClass.equals(InvalidKundeException.class)) {
			// Ungueltiges Password: Attribute wurden bereits von JSF validiert
			final InvalidKundeException orig = (InvalidKundeException) e;
			final Collection<ConstraintViolation<Kunde>> violations = orig.getViolations();
			messages.error(violations, CLIENT_ID_UPDATE_PASSWORD);
		} 
		else if (exceptionClass.equals(EmailExistsException.class)) {
			messages.error(KUNDENVERWALTUNG, MSG_KEY_UPDATE_KUNDE_DUPLIKAT, CLIENT_ID_UPDATE_EMAIL);
		} 
		else if (exceptionClass.equals(OptimisticLockException.class)) {
			messages.error(KUNDENVERWALTUNG, MSG_KEY_UPDATE_KUNDE_CONCURRENT_UPDATE, null);
		} 
		else if (exceptionClass.equals(ConcurrentDeletedException.class)) {
			messages.error(KUNDENVERWALTUNG, MSG_KEY_UPDATE_KUNDE_CONCURRENT_DELETE, null);
		}
		return null;
	}

	/**
	 * Action Methode, um einen zuvor gesuchten Kunden zu l&ouml;schen
	 * 
	 * @return URL fuer Startseite im Erfolgsfall, sonst wieder die gleiche
	 *         Seite
	 */

	@TransactionAttribute(REQUIRED)
	public String deleteAngezeigtenKunden() {
		if (kunde == null) {
			return null;
		}

		LOGGER.trace(kunde);
		try {
			ks.deleteKunde(kunde);
		} catch (KundeDeleteBestellungException e) {
			messages.error(KUNDENVERWALTUNG, MSG_KEY_DELETE_KUNDE_BESTELLUNG, CLIENT_ID_DELETE_BUTTON, e.getKundeId(),
					e.getAnzahlBestellungen());
			return null;
		}

		// Aufbereitung fuer ok.xhtml
		request.setAttribute(REQUEST_KUNDE_ID, kunde.getId());

		// Zuruecksetzen
		kunde = null;
		kundeId = null;

		return JSF_DELETE_OK;
	}

	public String selectForUpdate(Kunde ausgewaehlterKunde) {
		if (ausgewaehlterKunde == null) {
			return null;
		}

		kunde = ausgewaehlterKunde;

		return JSF_UPDATE_KUNDE;
	}

	@TransactionAttribute(REQUIRED)
	@RolesAllowed({ MITARBEITER, ABTEILUNGSLEITER, ADMIN })
	public String delete(Kunde ausgewaehlterKunde) {
		try {
			ks.deleteKunde(ausgewaehlterKunde);
		} catch (KundeDeleteBestellungException e) {
			messages.error(KUNDENVERWALTUNG, MSG_KEY_SELECT_DELETE_KUNDE_BESTELLUNG, null, e.getKundeId(),
					e.getAnzahlBestellungen());
			return null;
		}

		kunden.remove(ausgewaehlterKunde);
		return null;
	}

	public void uploadListener(FileUploadEvent event) {
		final UploadedFile uploadedFile = event.getUploadedFile();
		contentType = uploadedFile.getContentType();
		bytes = uploadedFile.getData();
	}

	@TransactionAttribute(REQUIRED)
	public String upload() {
		kunde = ks.findKundeById(kundeId, FetchType.NUR_KUNDE, locale);
		if (kunde == null) {
			return null;
		}
		ks.setFile(kunde, bytes, contentType);

		kundeId = null;
		bytes = null;
		contentType = null;
		kunde = null;

		return JSF_INDEX;
	}

	@PermitAll
	public String getFilename(File file) {
		if (file == null) {
			return "";
		}
		fileHelper.store(file);
		return file.getFilename();
	}

	public String getBase64(File file) {
		return DatatypeConverter.printBase64Binary(file.getBytes());
	}

	@TransactionAttribute(REQUIRED)
	@RolesAllowed({ MITARBEITER, ABTEILUNGSLEITER, ADMIN })
	public List<String> findNamenByPrefix(String namePrefix) {
		// NICHT: Liste von Kunden. Sonst waeren gleiche Nachnamen mehrfach
		// vorhanden.
		final List<String> namen = ks.findNamenByPrefix(namePrefix);
		if (namen.isEmpty()) {
			messages.error(KUNDENVERWALTUNG, MSG_KEY_KUNDEN_NOT_FOUND_BY_NAME, CLIENT_ID_KUNDEN_NAME, kundeId);
			return namen;
		}
		if (namen.size() > MAX_AUTOCOMPLETE) {
			return namen.subList(0, MAX_AUTOCOMPLETE);
		}
		return namen;
	}
}
