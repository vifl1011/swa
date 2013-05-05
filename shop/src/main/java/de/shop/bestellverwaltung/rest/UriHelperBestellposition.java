package de.shop.bestellverwaltung.rest;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import de.shop.artikelverwaltung.domain.Produkt;
import de.shop.artikelverwaltung.rest.UriHelperProdukt;
import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.domain.Lieferung;
import de.shop.util.Log;

@ApplicationScoped
@Log
public class UriHelperBestellposition {
	
	@Inject
	private UriHelperProdukt uriHelperProdukt;
	
	@Inject
	private UriHelperLieferung uriHelperLieferung;
	
	@Inject
	private UriHelperBestellung uriHelperBestellung;
	
	public URI getUriBestellposition(Bestellposition bestellposition, UriInfo uriInfo) 
	{
		final UriBuilder ub = uriInfo.getBaseUriBuilder()
		                             .path(BestellpositionResource.class)
		                             .path(BestellpositionResource.class, "findBestellpositionById");
		final URI bestellpositionUri = ub.build(bestellposition.getId());
		return bestellpositionUri;
	}
	
	public void updateUriBestellposition(Bestellposition bestellposition, UriInfo uriInfo) {
		// URL fuer Produkt setzen
		final Produkt produkt = bestellposition.getProdukt();
		if(produkt != null) {
			final URI produktUri = uriHelperProdukt.getUriProdukt(produkt, uriInfo);
			bestellposition.setProduktUri(produktUri);
		}
		
		// URL fuer Lieferung setzen
		final Lieferung lieferung = bestellposition.getLieferung();
		if(lieferung != null) {
			final URI lieferungUri = uriHelperLieferung.getUriLieferung(lieferung, uriInfo);
			bestellposition.setLieferungUri(lieferungUri);
		}
		
		// URL fuer Bestellung setzen		
		final Bestellung bestellung = bestellposition.getBestellung();
		if(bestellposition != null) {
			final URI bestellungURI = uriHelperBestellung.getUriBestellung(bestellung, uriInfo);
			bestellposition.setBestellungUri(bestellungURI);
		}
	}
	
}

