/**
 * 
 */
package de.shop.bestellverwaltung.domain;

import java.util.Date;

import javax.persistence.EntityManager;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.jboss.arquillian.junit.Arquillian;

import de.shop.util.AbstractResourceTest;
import de.shop.kundenverwaltung.domain.Kunde;

/**
 * @author Tobi
 * 
 */
@RunWith(Arquillian.class)
public class BestellungTest extends AbstractResourceTest {
	private static final Long BESTELLUNG_ID_VORHANDEN = Long.valueOf(603);
	private static final Long KUNDE_ID_VORHANDEN = Long.valueOf(205);
	private static final Long BESTELLUNG_GESAMTPREIS = Long.valueOf(20);
	
	@Test
	public void simpleTest() {
		// Given
		boolean expected = true;
		// When
		boolean result = true;
		// Then
		assertThat(result, is(expected));
	}

//	TODO [findBestellung] aufgrund von nicht vorhandenem EntityManager vorest auskommentiert
//	@Test
//	public void findBestellung() {
//		// Given
//		Long bestellungId = BESTELLUNG_ID_VORHANDEN; // must be available
//		// When
//		Bestellung bestellung = getEntityManager().find(Bestellung.class, bestellungId);
//		// Then
//		assertThat(bestellung.getId(), is(bestellungId));
//	}

	
//	TODO [createBestellung] aufgrund von nicht vorhandenem EntityManager vorest auskommentiert
//	@Test
//	public void createBestellung() {
//		Long kundeId = KUNDE_ID_VORHANDEN;
//		// Given
//		Bestellung bestellung = new Bestellung();
//		bestellung.setErzeugt(new Date());
//		bestellung.setGesamtpreis(BESTELLUNG_GESAMTPREIS);
//		bestellung.setGezahlt((byte) 0);
//		bestellung.setBestellstatus("bestellt");
//
//		Kunde k = getEntityManager().find(Kunde.class, kundeId);
//		bestellung.setKunde(k);
//		k.addBestellung(bestellung);
//
//		// When
//		getEntityManager().persist(bestellung);
//
//		// @PersistenceContext
//		EntityManager em = getEntityManager();
//
//		// Get Bestellung
//		final Bestellung best = em
//				.createNamedQuery(Bestellung.FIND_BESTELLUNGEN_BY_DATE, Bestellung.class)
//				.setParameter(Bestellung.PARAM_BESTELLUNG_DATE, bestellung.getErzeugt()).getSingleResult();
//
//		boolean kundeHasBestellung = k.getBestellungen().contains(bestellung);
//
//		// Then
//		assertThat(bestellung.getErzeugt(), is(best.getErzeugt()));
//		assertThat(kundeHasBestellung, is(true));
//	}
}
