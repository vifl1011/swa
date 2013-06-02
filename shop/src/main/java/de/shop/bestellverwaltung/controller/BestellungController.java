package de.shop.bestellverwaltung.controller;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static de.shop.util.Constants.JSF_DEFAULT_ERROR;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.Flash;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;

import de.shop.auth.controller.AuthController;
import de.shop.auth.controller.KundeLoggedIn;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.service.BestellService;
import de.shop.bestellverwaltung.service.BestellungValidationException;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.service.KundeService;
import de.shop.util.Client;
import de.shop.util.Transactional;
import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.kundenverwaltung.service.KundeService.FetchType;
import de.shop.util.Log;

@Named("bc")
@RequestScoped
@Log
public class BestellungController implements Serializable {
	private static final long serialVersionUID = -3892465338743970807L;
	
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
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
	private AuthController auth;
	
	@Inject
	private Warenkorb warenkorb;
	
	@Inject
	@Client
	private Locale locale;
	
	private Bestellung bestellung;
	
	private Long bestellungId;
	
	@Named("bc")
	@RequestScoped
	@Log
	@Transactional
	public String bestellen() {
		auth.preserveLogin();
		
		if (warenkorb == null || warenkorb.getPositionen() == null || warenkorb.getPositionen().isEmpty()) {
			return JSF_DEFAULT_ERROR;
		}
		
		kunde = ks.findKundeById(kunde.getId(), FetchType.MIT_BESTELLUNGEN, locale);
		
		final List<Bestellposition> positionen = warenkorb.getPositionen();
		final List<Bestellposition> neuePositionen = new ArrayList<>(positionen.size());
		for (Bestellposition bp : positionen) {
			if (bp.getMenge() > 0) {
				neuePositionen.add(bp);
			}
		}
		
		warenkorb.endConversation();
		
		// Neue Bestellung mit neuen Bestellpositionen erstellen
		Bestellung bestellung = new Bestellung();
		bestellung.setBestellpositionen(neuePositionen);
		
		LOGGER.tracef("Neue Bestellung: %s\nBestellpositionen: %s", bestellung, bestellung.getBestellpositionen());
		
		// Bestellung mit VORHANDENEM Kunden verknuepfen:
		// dessen Bestellungen muessen geladen sein, weil es eine bidirektionale Beziehung ist
		try {
			bestellung = bs.createBestellung(bestellung, kunde, locale);
		}
		catch (BestellungValidationException e) {
			// Validierungsfehler KOENNEN NICHT AUFTRETEN, da Attribute durch JSF validiert wurden
			// und in der Klasse Bestellung keine Validierungs-Methoden vorhanden sind
			throw new IllegalStateException(e);
		}
		
		// Bestellung im Flash speichern wegen anschliessendem Redirect
		flash.put("bestellung", bestellung);
		
		return JSF_VIEW_BESTELLUNG;
	}
	
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
	
	public Bestellung getBestellung() {
		return bestellung;
	}
	
	public void createEmptyBestellung() {
		if (bestellung != null)
			return;
		bestellung = new Bestellung();
	}
	
	/**
	 * Action Methode, um eine Bestellung zu gegebener ID zu suchen
	 * @return URL fuer Anzeige der gefundenen Bestellung; sonst null
	 */
	@Transactional
	public String findBestellungById() {
		bestellung = bs.findBestellungById(bestellungId, locale);
		if (bestellung == null) {
			flash.remove(FLASH_BESTELLUNG);
			return null;
		}
		
		flash.put(FLASH_BESTELLUNG, bestellung);
		return JSF_VIEW_BESTELLUNG;
	}
}
