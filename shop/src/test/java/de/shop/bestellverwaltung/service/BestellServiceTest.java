package de.shop.bestellverwaltung.service;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.validation.groups.Default;

import de.shop.artikelverwaltung.domain.Produkt;
import de.shop.artikelverwaltung.service.ProduktService;
import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.domain.Lieferung;
import de.shop.bestellverwaltung.service.BestellService.FetchType;
import static org.junit.Assert.assertThat;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;
import static org.hamcrest.CoreMatchers.is;

import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.jboss.arquillian.junit.Arquillian;

import de.shop.util.AbstractResourceTest;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.service.KundeService;

@RunWith(Arquillian.class)
@FixMethodOrder(NAME_ASCENDING)
@Ignore
public class BestellServiceTest extends AbstractResourceTest {
	private static final Long KUNDE_ID_VORHANDEN = Long.valueOf("200");
	private static final Long BESTELLUNG_ID_VORHANDEN = Long.valueOf("600");
	private static final Long BESTELLPOSITION_ID_VORHANDEN = Long.valueOf("700");
	private static final Long LIEFERUNG_ID_VORHANDEN = Long.valueOf("300");
	private static final Long PRODUKT_ID_VORHANDEN = Long.valueOf("506");
	private static final Long PRODUKT_PREIS_VORHANDEN = Long.valueOf(800);
	private static final short PRODUKT_MENGE = 4;
	private static final short BESTELLUNGEN_ANZAHL = 10;

	@Rule
	private ExpectedException thrown = ExpectedException.none();
	
	@Inject
	private BestellService bs;

	@Inject
	private KundeService ks;

	@Inject
	private ProduktService as;

	@PersistenceContext
	private transient EntityManager em;

	@Test
	public void aFindBestellungen() {
		// Given
		List<Bestellung> bestellungen = null;
		List<Bestellung> fetchedBestellungen = null;
		// When
		bestellungen = bs.findBestellungen(Locale.GERMAN);
		fetchedBestellungen = bs.findBestellungen(Locale.GERMAN);

		// Then
		assertThat(bestellungen.size() == BESTELLUNGEN_ANZAHL, is(true));
		assertThat(fetchedBestellungen.size() == BESTELLUNGEN_ANZAHL, is(true));
	}

	@Test
	public void bFindBestellungById() {
		Bestellung result = null;
		
		result = bs.findBestellungById(BESTELLUNG_ID_VORHANDEN, Locale.GERMAN);

		assertThat(result.getId(), is(BESTELLUNG_ID_VORHANDEN));
	}

	@Test
	public void dFindBestellungenByKunde() {
		List<Bestellung> result = null;
		final Locale locale = Locale.GERMAN;

		final Kunde kunde = ks.findKundeById(KUNDE_ID_VORHANDEN, KundeService.FetchType.NUR_KUNDE, locale);

		result = bs.findBestellungenByKunde(kunde, FetchType.NUR_BESTELLUNG);

		assertThat(kunde.getId(), is(KUNDE_ID_VORHANDEN));
		assertThat(result.size(), is(2));
	}

	@Test
	public void eFindBestellpositionenByBestellung() throws Exception {
		List<Bestellposition> result = null;
		final Bestellung bestellung = bs.findBestellungById(BESTELLUNG_ID_VORHANDEN, Locale.GERMAN);

		result = bs.findBestellpositionenByBestellung(bestellung);

		assertThat(result.size(), is(2));
	}

	@Test
	public void xCreateBestellung() throws Exception {
		Bestellung bestellung = null;
		final Locale locale = Locale.GERMAN;

		// Dieser Kunde legt eine neue Bestellung an
		final Kunde kunde = ks.findKundeById(KUNDE_ID_VORHANDEN, KundeService.FetchType.NUR_KUNDE, locale);
		
		// Anzahl Bestellungen des Kunden vorher
		final int bestellungenVorher = bs.findBestellungenByKunde(kunde, FetchType.NUR_BESTELLUNG).size();
		
		// Das Produkt das er kaufen möchte
		final Produkt produkt = as.findProduktById(PRODUKT_ID_VORHANDEN, locale);
		
		// Die Ausgewählte Lieferung
		final Lieferung lieferung = bs.findLieferungById(LIEFERUNG_ID_VORHANDEN, locale);
		
		// erstelle neue Bestellung
		bestellung = bs.createBestellung(kunde, produkt, PRODUKT_MENGE, lieferung, locale);
		final Long neueBestellungId = bestellung.getId();
		
		// end transaction
		assertThat(neueBestellungId != null, is(true));
		
		// versuche besagte Bestellung wieder aus der Datenbank raus zu lesen
		// Anzahl der Bestellungen des Kunden nach der Transaktion
		final int bestellungenNachher = bs.findBestellungenByKunde(kunde, FetchType.NUR_BESTELLUNG).size();
		
		// Neuladen der eben angelegten Bestellung
		bestellung = bs.findBestellungById(neueBestellungId, Locale.GERMAN);
		
		// Wie viele Bestellpositionen hat die neue Bestellung?
		final int anzahlBestellpositionen = bs.findBestellpositionenByBestellung(bestellung).size();
		
		// Then
		assertThat(bestellungenNachher, is(bestellungenVorher + 1));
		
		// Die Anzahl der Bestellpositionen der neuen Bestellung muss eins sein
		assertThat(anzahlBestellpositionen, is(1));
	}

	@Test
	public void wCreateBestellposition() throws Exception {
		Bestellung bestellung = bs.findBestellungById(BESTELLUNG_ID_VORHANDEN, Locale.GERMAN);
		final int sizeBefore = bs.findBestellpositionenByBestellung(bestellung).size();
		final Produkt produkt = as.findProduktById(PRODUKT_ID_VORHANDEN, Locale.GERMAN);
		final Lieferung lieferung = bs.findLieferungById(LIEFERUNG_ID_VORHANDEN, Locale.GERMAN);

		// Create New Bestellposition
		final Bestellposition bestellposition = new Bestellposition(bestellung, lieferung, produkt, 2);
		
		//em.persist(bestellposition);
		bs.createBestellposition(bestellposition, Locale.GERMAN);

		// Update Gesamtpreis der Bestellung
		final int sizeAfter = bs.findBestellpositionenByBestellung(bestellung).size();
		bestellung = bs.findBestellungById(BESTELLUNG_ID_VORHANDEN, Locale.GERMAN);

		final List<Bestellposition> bestellpositionen = bs.findBestellpositionenByBestellung(bestellung);
		int preisGesamt = 0;

		for (Bestellposition b : bestellpositionen) {
			preisGesamt += b.getEinzelpreis();
		}

		bestellung.setGesamtpreis(preisGesamt);
		bs.updateBestellung(bestellung, Locale.GERMAN);

		assertThat(sizeBefore + 1, is(sizeAfter));
	}
	
	@Test
	public void uCreateLieferung() throws RollbackException, HeuristicMixedException, 
											HeuristicRollbackException, SystemException, 
											NotSupportedException  {
		final int lieferungenVorher = bs.findLieferungen(Locale.GERMAN).size();
		final Lieferung lieferung = new Lieferung("DHL2");
		bs.createLieferung(lieferung, Locale.GERMAN);
		
		final int lieferungenNachher = bs.findLieferungen(Locale.GERMAN).size();
		
		assertThat(lieferungenVorher + 1, is(lieferungenNachher));
	
	}
	
	@Test
	public void sUpdateBestellposition() throws RollbackException, 
												HeuristicMixedException, 
												HeuristicRollbackException, 
												SystemException, NotSupportedException {
		Bestellposition bestellposition = bs.findBestellpositionById(BESTELLPOSITION_ID_VORHANDEN, Locale.GERMAN);
		bestellposition.setEinzelpreis(PRODUKT_PREIS_VORHANDEN);
		bs.updateBestellposition(bestellposition, Locale.GERMAN);
		
		bestellposition = bs.findBestellpositionById(BESTELLPOSITION_ID_VORHANDEN, Locale.GERMAN);
		final float result = bestellposition.getEinzelpreis();
		assertThat(result, is((float) PRODUKT_PREIS_VORHANDEN));
		
	}
	
	@Test
	public void tUpdateBestellung() throws RollbackException, 
											HeuristicMixedException, 
											HeuristicRollbackException, 
											SystemException, NotSupportedException {
		Bestellung bestellung = bs.findBestellungById(BESTELLUNG_ID_VORHANDEN, Locale.GERMAN);
		bestellung.setBestellstatus("funktioniert doch :P");
		bs.updateBestellung(bestellung, Locale.GERMAN);
		
		bestellung = bs.findBestellungById(BESTELLUNG_ID_VORHANDEN, Locale.GERMAN);
		final String result = bestellung.getBestellstatus();
		assertThat(result, is((String) "funktioniert doch :P"));
		
	}

	

	@Test
	public void yInvalidBestellung() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, 
												SystemException, NotSupportedException {
		final Locale locale = Locale.GERMAN;
		
		// Dieser Kunde legt eine neue Bestellung an
		final Kunde kunde = ks.findKundeById(KUNDE_ID_VORHANDEN, KundeService.FetchType.NUR_KUNDE, locale);

		// Das Produkt das er kaufen möchte
		final Produkt produkt = as.findProduktById(PRODUKT_ID_VORHANDEN, locale);

		// Die Ausgewählte Lieferung
		final Lieferung lieferung = bs.findLieferungById(LIEFERUNG_ID_VORHANDEN, locale);

		// neue Bestellung
		final Bestellung bestellung = new Bestellung();
		bestellung.setKunde(kunde);
		kunde.addBestellung(bestellung);

		// Bestellposition anlegen
		final Bestellposition bestellposition = new Bestellposition(bestellung, lieferung, produkt, 2);
		bestellung.setGesamtpreis(bestellposition.getEinzelpreis());
		bestellung.setBestellstatus(null);

		thrown.expect(BestellungValidationException.class);
		bs.validateBestellung(bestellung, locale, Default.class);

		em.persist(bestellung);
	}
}
