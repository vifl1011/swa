package de.shop.artikelverwaltung.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import de.shop.util.AbstractDomainTest;

import javax.persistence.TypedQuery;

/**
 * @author Alex
 *
 */

@RunWith(Arquillian.class)

public class ProduktTest extends AbstractDomainTest {
	private static final Long ID_VORHANDEN = Long.valueOf(500);
	
	// ein bereits vorhandenes Produkt finden
	@Test
	public void findProduktById() {
		// Given
		final Long id = ID_VORHANDEN;
		
		// When
		final Produkt produkt = getEntityManager().find(Produkt.class, id);
		
		// Then
		assertThat(produkt.getId(), is(id));
	}
	
	// ein neues Produkt anlegen und finden
//	@Test
//	public void createProdukt() {
//		// Given
//		Produkt produkt = new Produkt();
//		produkt.setId(ID_NEU);
//		produkt.setFarbe("Blau");
//		produkt.setVorrat(100);
//		produkt.setGroesse("40");
//		
//		// When
//		getEntityManager().persist(produkt);
//			
//		// Then
//		final Produkt produktGefunden = getEntityManager().find(Produkt.class, ID_NEU);
//				
//		// Neu angelegtes Produkt finden
//		assertThat(produktGefunden.getId(), is(ID_NEU));	
//	}
	
	// Produkt finden mit named query
	@Test
	public void findProduktByIdWithNQ() {
		// Given
		final Long id = ID_VORHANDEN;
		
		// When
		final TypedQuery<Produkt> query = getEntityManager().createNamedQuery(Produkt.FIND_PRODUKT_BY_ID,
				                                                                    Produkt.class);
		query.setParameter(Produkt.PARAM_ID, id);
		Produkt produkt = query.getSingleResult();
		
		// Then
		assertThat(produkt.getId(), is(id));
	}
	
	
}