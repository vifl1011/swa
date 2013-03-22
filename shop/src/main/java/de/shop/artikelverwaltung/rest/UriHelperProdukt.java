package de.shop.artikelverwaltung.rest;


import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import de.shop.artikelverwaltung.domain.Produkt;
import de.shop.util.Log;


@ApplicationScoped
@Log
public class UriHelperProdukt {
	public URI getUriProdukt(Produkt produkt, UriInfo uriInfo) {
		final UriBuilder ub = uriInfo.getBaseUriBuilder()
		                             .path(ProduktResource.class)
		                             .path(ProduktResource.class, "findProduktById");
		final URI uri = ub.build(produkt.getId());
		return uri;
	}
}
