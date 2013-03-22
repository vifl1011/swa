package de.shop.bestellverwaltung.rest;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.util.Log;

@ApplicationScoped
@Log
public class UriHelperBestellposition {
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
		final UriBuilder ubProdukt = uriInfo.getBaseUriBuilder().path(BestellpositionResource.class)
				.path(BestellpositionResource.class, "findProduktByBestellpositionId");
		final URI produktUri = ubProdukt.build(bestellposition.getId());
		bestellposition.setProduktUri(produktUri);
		
		// URL fuer Lieferung setzen
		final UriBuilder ubLieferung = uriInfo.getBaseUriBuilder().path(BestellpositionResource.class)
				.path(BestellpositionResource.class, "findLieferungByBestellpositionId");
		final URI lieferungUri = ubLieferung.build(bestellposition.getId());
		bestellposition.setLieferungUri(lieferungUri);
		
		// URL fuer Bestellung setzen
		final UriBuilder ubBestellung = uriInfo.getBaseUriBuilder().path(BestellpositionResource.class)
				.path(BestellpositionResource.class, "findBestellungByBestellpositionId");
		final URI bestellungUri = ubBestellung.build(bestellposition.getId());
		bestellposition.setBestellungUri(bestellungUri);
	}
	
}