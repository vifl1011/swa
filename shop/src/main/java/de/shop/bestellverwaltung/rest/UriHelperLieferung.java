package de.shop.bestellverwaltung.rest;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import de.shop.bestellverwaltung.domain.Lieferung;
import de.shop.util.Log;

@ApplicationScoped
@Log
public class UriHelperLieferung {
	public URI getUriLieferung(Lieferung lieferung, UriInfo uriInfo) {
		final UriBuilder ub = uriInfo.getBaseUriBuilder().path(LieferungResource.class)
				.path(LieferungResource.class, "findLieferung");
		final URI lieferungUri = ub.build(lieferung.getId());
		return lieferungUri;
	}

	public void updateUriLieferung(Lieferung lieferung, UriInfo uriInfo) {
		// URL fuer Bestellpositionen setzen
		/*
		final UriBuilder ub = uriInfo.getBaseUriBuilder().path(LieferungResource.class)
				.path(LieferungResource.class, "findBestellpositionenByLieferung");
		final URI bestellpositionenUri = ub.build(lieferung.getId());
		lieferung.setBestellpositionenUri(bestellpositionenUri);
		*/
	}

}
