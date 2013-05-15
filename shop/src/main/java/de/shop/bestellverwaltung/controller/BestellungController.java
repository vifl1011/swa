package de.shop.bestellverwaltung.controller;

import java.io.Serializable;
import java.util.Locale;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.Flash;
import javax.inject.Inject;
import javax.inject.Named;

import de.shop.auth.controller.KundeLoggedIn;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.service.BestellService;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.service.KundeService;
import de.shop.util.Client;
import de.shop.util.Transactional;

@Named("bc")
@RequestScoped
public class BestellungController implements Serializable {
	private static final long serialVersionUID = -3892465338743970807L;
	
	private static final String FLASH_BESTELLUNG = "bestellung";
	private static final String JSF_VIEW_BESTELLUNG = "/bestellverwaltung/viewBestellung";
	
	
	@Inject
	private BestellService bs;
	
	@Inject
	private KundeService ks;
	
	@Inject
	@KundeLoggedIn
	private Kunde kunde;
	
	@Inject
	private Flash flash;
	
	@Inject
	@Client
	private Locale locale;
	
	private Bestellung neueBestellung;
	
	private Long bestellungId;
	
	@Override
	public String toString() {
		return "BestellungController [kundeId=" + bestellungId + "]";
	}
	
	public void setBestellungId(Long id) {
		bestellungId = id;
	}
	
	public Long getBestellungId() {
		return bestellungId;
	}
	
	public void createEmptyBestellung() {
		if (neueBestellung != null)
			return;
		neueBestellung = new Bestellung();
	}
	
	/**
	 * Action Methode, um eine Bestellung zu gegebener ID zu suchen
	 * @return URL fuer Anzeige der gefundenen Bestellung; sonst null
	 */
	//TODO Locale wird nicht richtig gesetzt
	@Transactional
	public String findBestellungById() {
		final Bestellung bestellung = bs.findBestellungById(bestellungId, locale);
		if (bestellung == null) {
			flash.remove(FLASH_BESTELLUNG);
			return null;
		}
		
		flash.put(FLASH_BESTELLUNG, bestellung);
		return JSF_VIEW_BESTELLUNG;
	}
}
