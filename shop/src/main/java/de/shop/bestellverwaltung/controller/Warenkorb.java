package de.shop.bestellverwaltung.controller;

import static java.util.concurrent.TimeUnit.MINUTES;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;

import de.shop.artikelverwaltung.domain.Produkt;
import de.shop.artikelverwaltung.service.ProduktService;
import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.util.Log;


@Named("wk")
@ConversationScoped
@Log
public class Warenkorb implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2822410085540035488L;

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final String JSF_VIEW_WARENKORB = "/bestellverwaltung/viewWarenkorb?init=true";
	private static final int TIMEOUT = 5;
	
	private final List<Bestellposition> positionen = new ArrayList<Bestellposition>();
	private Long artikelId;  // fuer selectArtikel.xhtml
	
	@Inject
	private transient Conversation conversation;
	
	@Inject
	private ProduktService ps;
	
	@PostConstruct
	private void postConstruct() {
		LOGGER.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
	}
	
	@PreDestroy
	private void preDestroy() {
		LOGGER.debugf("CDI-faehiges Bean %s wird geloescht", this);
	}
	
	public List<Bestellposition> getPositionen() {
		return positionen;
	}
		
	public void setArtikelId(Long artikelId) {
		this.artikelId = artikelId;
	}

	public Long getArtikelId() {
		return artikelId;
	}
	
	public int getMenge() {
		return positionen.size();
	}
	
	@Override
	public String toString() {
		return "Warenkorb " + positionen;
	}
	
	/**
	 */
	public String add(Produkt produkt) {
		beginConversation();
		
		if (positionen != null) {
			for (Bestellposition bp : positionen) {
				if (bp.getProdukt().equals(produkt)) {
					// bereits im Warenkorb
					final int vorhandeneAnzahl = bp.getMenge();
					bp.setMenge((short) (vorhandeneAnzahl + 1));
					return JSF_VIEW_WARENKORB;
				}
			}
		}
		final Bestellposition neu = new Bestellposition(produkt);
		positionen.add(neu);
		return JSF_VIEW_WARENKORB;
	}
	
	/**
	 */
	public void remove(Bestellposition bestellposition) {
		positionen.remove(bestellposition);
		if (positionen.isEmpty()) {
			endConversation();
		}
	}
	
	/**
	 */
	public void beginConversation() {
		if (!conversation.isTransient()) {
			return;
		}
		conversation.begin();
		conversation.setTimeout(MINUTES.toMillis(TIMEOUT));
		LOGGER.debugf("Conversation beginnt");
	}
	
	/**
	 */
	public void endConversation() {
		conversation.end();
		LOGGER.debugf("Conversation beendet");
	}
}
