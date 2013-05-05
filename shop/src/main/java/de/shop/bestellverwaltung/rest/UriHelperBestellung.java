package de.shop.bestellverwaltung.rest;

import java.net.URI;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.rest.UriHelperKunde;
import de.shop.util.Log;

@ApplicationScoped
@Log
public class UriHelperBestellung {
	
	@Inject
	private UriHelperBestellposition uriHelperBestellposition;
	
	@Inject
	private UriHelperKunde uriHelperKunde;
	
	public URI getUriBestellung(Bestellung bestellung, UriInfo uriInfo) {
		final UriBuilder ub = uriInfo.getBaseUriBuilder().path(BestellungResource.class)
				.path(BestellungResource.class, "findBestellungById");
		final URI bestellungUri = ub.build(bestellung.getId());
		return bestellungUri;
	}

	public void updateUriBestellung(Bestellung bestellung, UriInfo uriInfo) {
		// URL fuer Kunde setzen
		final Kunde kunde = bestellung.getKunde();
		if (kunde != null) {
			final URI kundeUri = uriHelperKunde.getUriKunde(bestellung.getKunde(), uriInfo);
			bestellung.setKundeUri(kundeUri);
		}
		
		final List<Bestellposition> bestellpositionen = bestellung.getBestellpositionen();
		for (Bestellposition bp : bestellpositionen) {
			uriHelperBestellposition.updateUriBestellposition(bp, uriInfo);
		}
		
	}
}
