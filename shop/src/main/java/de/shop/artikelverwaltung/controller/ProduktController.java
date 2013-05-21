package de.shop.artikelverwaltung.controller;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.Flash;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.jboss.logging.Logger;

import de.shop.artikelverwaltung.domain.Produkt;
import de.shop.artikelverwaltung.service.ProduktService;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.util.Client;
import de.shop.util.Log;
import de.shop.util.Transactional;


/**
 * Dialogsteuerung fuer die ArtikelService
 */
@Named("ac")
@RequestScoped
@Log
public class ProduktController implements Serializable {
	private static final long serialVersionUID = 1564024850446471639L;

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	private static final String JSF_LIST_ARTIKEL = "/artikelverwaltung/listArtikel";
	private static final String FLASH_ARTIKEL = "artikel";
	private static final String JSF_LIST_Produkt = "/Produktverwaltung/listProdukt";
	private static final String JSF_VIEW_PRODUKT = "/Produktverwaltung/viewProdukt";
	private static final String FLASH_Produkt = "Produkt";
	private static final int ANZAHL_LADENHUETER = 5;
	
	private static final String JSF_SELECT_ARTIKEL = "/artikelverwaltung/selectArtikel";
	private static final String SESSION_VERFUEGBARE_ARTIKEL = "verfuegbareArtikel";

	private String bezeichnung;
	
	private List<Produkt> ladenhueter;
	
	private Produkt produkt;
	
	private Long produktId;

	@Inject
	private ProduktService as;
	
	@Inject
	private Flash flash;
	
	@Inject
	private transient HttpSession session;
	
	@Inject
	@Client
	private Locale locale;
	
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
		return "ArtikelController [bezeichnung=" + bezeichnung + "]";
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


	public List<Produkt> getLadenhueter() {
		return ladenhueter;
	}

	@Transactional
	public String findArtikelByBezeichnung() {
//		final List<Produkt> artikel = as.findArtikelByBezeichnung(bezeichnung);
//		flash.put(FLASH_ARTIKEL, artikel);
//
//		return JSF_LIST_ARTIKEL;
		
		final Produkt artikel = as.findProduktByIdEm(Long.valueOf(bezeichnung));       //  joa so nat�rlich nicht
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
			return JSF_SELECT_ARTIKEL;
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
			flash.remove(FLASH_Produkt);
			return null;
		}
		flash.put(FLASH_Produkt, produkt);
		return JSF_LIST_Produkt;
	}
	
}