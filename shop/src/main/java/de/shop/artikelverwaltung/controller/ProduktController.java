package de.shop.artikelverwaltung.controller;

import static de.shop.auth.service.jboss.AuthService.ABTEILUNGSLEITER;
import static de.shop.auth.service.jboss.AuthService.ADMIN;
import static de.shop.auth.service.jboss.AuthService.KUNDE;
import static de.shop.auth.service.jboss.AuthService.MITARBEITER;
import static de.shop.util.Messages.MessagesType.ARTIKELVERWALTUNG;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.faces.context.Flash;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.OptimisticLockException;
import javax.servlet.http.HttpSession;

import org.jboss.logging.Logger;
import org.richfaces.cdi.push.Push;

import de.shop.artikelverwaltung.domain.Produkt;
import de.shop.artikelverwaltung.service.BezeichnungExistsException;
import de.shop.artikelverwaltung.service.ProduktService;
import de.shop.auth.controller.AuthController;
import de.shop.util.Client;
import de.shop.util.ConcurrentDeletedException;
import de.shop.util.Log;
import de.shop.util.Messages;
import de.shop.util.Transactional;
import static de.shop.util.Constants.JSF_INDEX;
import static de.shop.util.Constants.JSF_REDIRECT_SUFFIX;
import static javax.ejb.TransactionAttributeType.REQUIRED;
import static javax.ejb.TransactionAttributeType.SUPPORTS;


/**
 * Dialogsteuerung fuer die ArtikelService
 */
@Named("ac")
@SessionScoped
@Log
@Stateful
@TransactionAttribute(SUPPORTS)
@PermitAll
public class ProduktController implements Serializable {
	private static final long serialVersionUID = 1564024850446471639L;

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	private static final String JSF_PRODUKTVERWALTUNG = "/artikelverwaltung";
	private static final String JSF_LIST_ARTIKEL = JSF_PRODUKTVERWALTUNG + "/listArtikel";
	private static final String JSF_UPDATE_PRODUKT = JSF_PRODUKTVERWALTUNG + "/updateArtikel";
	private static final String FLASH_ARTIKEL = "artikel";
	private static final int ANZAHL_LADENHUETER = 5;
	
	private static final String JSF_SELECT_ARTIKEL = "/artikelverwaltung/selectArtikel";
	private static final String SESSION_VERFUEGBARE_ARTIKEL = "verfuegbareArtikel";
	
	private static final String PRODUCT_ID_PRODUKTID = "form:produktIdInput";
	private static final String MSG_KEY_PRODUKT_NOT_FOUND_BY_ID = "selectArtikel.notFound";
	private static final String MSG_KEY_UPDATE_PRODUKT_BEZEICHNUNG = "updateProdukt.bezeichnung";
	


	private String bezeichnung;
	
	private List<Produkt> ladenhueter;
	
	private Produkt produkt;
	
	private boolean geaendertProdukt;    // fuer ValueChangeListener
	
	private Produkt neuesProdukt;
	
	public Produkt getNeuesProdukt() {
		return neuesProdukt;
	}

	private Long produktId;

	@Inject
	private ProduktService as;
	
	@Inject
	private AuthController auth;
	
	@Inject
	private Flash flash;
	
	@Inject
	private transient HttpSession session;
	
	@Inject
	@Client
	private Locale locale;
	
	@Inject
	private Messages messages;
	
	/*
	@Inject
	@Push(topic = "marketing")
	private transient Event<String> neuesProduktEvent;
	*/
	
	@Inject
	@Push(topic = "updateProdukt")
	private transient Event<String> updateProduktEvent;
	
	@PostConstruct
	private void postConstruct() {
		createEmptyProdukt();
		LOGGER.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
	}

	@PreDestroy
	private void preDestroy() {
		LOGGER.debugf("CDI-faehiges Bean %s wird geloescht", this);
	}
	
	@Override
	public String toString() {
		return "ArtikelController [bezeichnung=" + bezeichnung + "]"
				 + ", geaendertProdukt=" + geaendertProdukt + "]";
	}

	public void setProduktId(Long id) {
		produktId = id;
	}
	
	public Long getProduktId() {
		return produktId;
	}
	
	public Produkt getProdukt() {
		return produkt;
	}
	
	public String getBezeichnung() {
		return bezeichnung;
	}

	public void setBezeichnung(String bezeichnung) {
		this.bezeichnung = bezeichnung;
	}

	@PermitAll
	public List<Produkt> getLadenhueter() {
		return ladenhueter;
	}

//	@Transactional
//	public String findArtikelByBezeichnung() {
////		final List<Produkt> artikel = as.findArtikelByBezeichnung(bezeichnung);
////		flash.put(FLASH_ARTIKEL, artikel);
////
////		return JSF_LIST_ARTIKEL;
//		
//		final Produkt artikel = as.findProduktByIdEm(Long.valueOf(bezeichnung));       //  joa so natürlich nicht
//		flash.put(FLASH_ARTIKEL, artikel);
//
//		return JSF_LIST_ARTIKEL;
//	
//	}
	
	@Transactional
	public String findArtikelByBezeichnung() {
		final List<Produkt> artikel = as.findArtikelByBezeichnung(bezeichnung);
		flash.put(FLASH_ARTIKEL, artikel);

		return JSF_LIST_ARTIKEL;
	}

	@Transactional
	public void loadLadenhueter() {
		ladenhueter = as.ladenhueter(ANZAHL_LADENHUETER);
	}
	
	@Transactional
	public String selectArtikel() {
		if (session.getAttribute(SESSION_VERFUEGBARE_ARTIKEL) != null) {
			session.removeAttribute(SESSION_VERFUEGBARE_ARTIKEL);

		}
		
		final List<Produkt> alleArtikel = as.findAlleProdukte();
		session.setAttribute(SESSION_VERFUEGBARE_ARTIKEL, alleArtikel);
		return JSF_SELECT_ARTIKEL;
	}
	
	/**
	 * Action Methode, um eine Bestellung zu gegebener ID zu suchen
	 * @return URL fuer Anzeige der gefundenen Bestellung; sonst null
	 */
	
	@Transactional
	public String findProduktById() {
		produkt = as.findProduktById(produktId, locale);
		if (produkt == null) {
			flash.remove(FLASH_ARTIKEL);
			return null;
		}
		flash.put(FLASH_ARTIKEL, produkt);
		return JSF_LIST_ARTIKEL;
	}
	
	@Transactional
	public String findProduct() {
		produkt = as.findProduktById(produkt.getId(), locale);
		if (produkt == null) {
			// Kein Produkt zu gegebener ID gefunden
			return findProduktByIdErrorMsg(produktId.toString());
		}

		return JSF_SELECT_ARTIKEL;
	}
	
	private String findProduktByIdErrorMsg(String id) {
		messages.error(ARTIKELVERWALTUNG, MSG_KEY_PRODUKT_NOT_FOUND_BY_ID, PRODUCT_ID_PRODUKTID, id);
		return null;
	}
	
	public void createEmptyProdukt() {
		if (neuesProdukt != null) {
			return;
		}
		
		this.neuesProdukt = new Produkt();
		LOGGER.debugf("leeres Produkt erstell :D");
	}
	
	
	/*
	 * legt ein neues Produkt an
	 */
	@Transactional
	@RolesAllowed({ ADMIN, MITARBEITER, ABTEILUNGSLEITER })
	public String createProdukt() {
		try {
			as.createProdukt(neuesProdukt, locale);
		} catch (Exception e) {
			return e.getMessage();
		}
		
		produktId = neuesProdukt.getId();
		produkt = neuesProdukt;
		
		return JSF_LIST_ARTIKEL + JSF_REDIRECT_SUFFIX;
	}
	
	@RolesAllowed({ ADMIN, MITARBEITER, ABTEILUNGSLEITER })
	@TransactionAttribute(REQUIRED)
	public String update() {
		auth.preserveLogin();
		
		if (!geaendertProdukt || produkt == null) {
			return JSF_INDEX;
		}
		
		LOGGER.tracef("Aktualisierter Artikel: %s", produkt);
		try {
			produkt = as.updateProdukt(produkt, locale);
		}
		catch (OptimisticLockException | ConcurrentDeletedException e) {
			final String outcome = updateErrorMsg(e, produkt.getClass());
			return outcome;
		}

		// Push-Event fuer Webbrowser
		updateProduktEvent.fire(String.valueOf(produkt.getId()));
		
		// ValueChangeListener zuruecksetzen
		geaendertProdukt = false;
		
		// Aufbereitung fuer listArtikel.xhtml
		produktId = produkt.getId();
		
		return JSF_LIST_ARTIKEL + JSF_REDIRECT_SUFFIX;
	}
	
	private String updateErrorMsg(RuntimeException e, Class<? extends Produkt> produktClass) {
		final Class<? extends RuntimeException> exceptionClass = e.getClass();
		if (exceptionClass.equals(BezeichnungExistsException.class)) {
			messages.error(ARTIKELVERWALTUNG, MSG_KEY_UPDATE_PRODUKT_BEZEICHNUNG, null);
		}
		return null;
	}
	
	public void geaendert(ValueChangeEvent e) {
		if (geaendertProdukt) {
			return;
		}
		
		if (e.getOldValue() == null) {
			if (e.getNewValue() != null) {
				geaendertProdukt = true;
			}
			return;
		}

		if (!e.getOldValue().equals(e.getNewValue())) {
			geaendertProdukt = true;				
		}
	}
	
	public String selectForUpdate(Produkt ausgewaehltesProdukt) {
		if (ausgewaehltesProdukt == null) {
			return null;
		}
		
		produkt = ausgewaehltesProdukt;
		
		return JSF_UPDATE_PRODUKT;
	}
}
