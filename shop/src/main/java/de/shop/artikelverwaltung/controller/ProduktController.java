package de.shop.artikelverwaltung.controller;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.List;

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
import de.shop.util.Log;
import de.shop.util.Transactional;


/**
 * Dialogsteuerung fuer die ProduktService
 */
@Named("ac")
@RequestScoped
@Log
public class ProduktController implements Serializable {
	private static final long serialVersionUID = 1564024850446471639L;

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	private static final String JSF_LIST_Produkt = "/Produktverwaltung/listProdukt";
	private static final String FLASH_Produkt = "Produkt";
	private static final int ANZAHL_LADENHUETER = 5;
	
	private static final String JSF_SELECT_Produkt = "/Produktverwaltung/selectProdukt";
	private static final String SESSION_VERFUEGBARE_Produkt = "verfuegbareProdukt";

	private String bezeichnung;
	
	private List<Produkt> ladenhueter;

	@Inject
	private ProduktService as;
	
	@Inject
	private Flash flash;
	
	@Inject
	private transient HttpSession session;

	
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
		return "ProduktController [bezeichnung=" + bezeichnung + "]";
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
	public void loadLadenhueter() {
		ladenhueter = as.ladenhueter(ANZAHL_LADENHUETER);
	}
	
	@Transactional
	public String selectProdukt() {
		if (session.getAttribute(SESSION_VERFUEGBARE_Produkt) != null) {
			return JSF_SELECT_Produkt;
		}
		
		final List<Produkt> alleProdukt = as.findProdukte();
		session.setAttribute(SESSION_VERFUEGBARE_Produkt, alleProdukt);
		return JSF_SELECT_Produkt;
	}
}
