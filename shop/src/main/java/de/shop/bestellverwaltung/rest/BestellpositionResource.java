package de.shop.bestellverwaltung.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.TEXT_XML;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;
import de.shop.artikelverwaltung.domain.Produkt;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.domain.Lieferung;
import de.shop.artikelverwaltung.service.ProduktService;
import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.bestellverwaltung.service.BestellService;
import de.shop.util.Log;
import de.shop.util.NotFoundException;
import de.shop.util.Transactional;

@Path("/bestellposition")
@Produces(APPLICATION_JSON)
@Consumes
@RequestScoped
@Transactional
@Log
public class BestellpositionResource {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	private static final String VERSION = "1.0";
	private static final String MSG_OBJECT_NOT_FOUND = "{object.notFound}";

	@Inject
	private BestellService bs;
	
	@Inject
	private ProduktService av;

	@Inject
	private UriHelperBestellposition uriHelperBestellposition;
	
	@Inject
	private UriHelperBestellung uriHelperBestellung;

	@PostConstruct
	private void postConstruct() {
		LOGGER.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
	}

	@PreDestroy
	private void preDestroy() {
		LOGGER.debugf("CDI-faehiges Bean %s wird geloescht", this);
	}

	@GET
	@Produces(TEXT_PLAIN)
	@Path("version")
	public String getVersion() {
		return VERSION;
	}
	
	@GET
	@Path("{id:[1-9][0-9]*}")
	@Wrapped(element = "bestellposition")
	public Bestellposition findBestellpositionById(@PathParam("id") Long id,
			@Context UriInfo uriInfo, @Context HttpHeaders headers) {
		final List<Locale> locales = headers.getAcceptableLanguages();
		final Locale locale = locales.isEmpty() ? Locale.GERMAN : locales.get(0);
		final Bestellposition bestellposition = bs.findBestellpositionById(id, locale);

		if (bestellposition == null) {
			final String msg = "{object.notFound}";
			throw new NotFoundException(msg);
		}

		// URLs innerhalb der Bestellposition anpassen
		uriHelperBestellposition.updateUriBestellposition(bestellposition, uriInfo);

		return bestellposition;
	}
	
	@GET
	@Path("{id:[1-9][0-9]*}/produkt")
	@Wrapped(element = "produkt")
	public Produkt findProduktByBestellpositionId(@PathParam("id") Long id,
			@Context UriInfo uriInfo, @Context HttpHeaders headers) {
		final List<Locale> locales = headers.getAcceptableLanguages();
		final Locale locale = locales.isEmpty() ? Locale.GERMAN : locales.get(0);
		final Bestellposition bestellposition = bs.findBestellpositionById(id, locale);

		if (bestellposition == null) {
			final String msg = MSG_OBJECT_NOT_FOUND;
			throw new NotFoundException(msg);
		}

		// URLs innerhalb der Bestellposition anpassen
		//uriHelperBestellposition.updateUriBestellposition(bestellposition, uriInfo);

		return bestellposition.getProdukt();
	}
	
	@GET
	@Path("{id:[1-9][0-9]*}/lieferung")
	@Wrapped(element = "lieferung")
	public Lieferung findLieferungByBestellpositionId(@PathParam("id") Long id,
			@Context UriInfo uriInfo, @Context HttpHeaders headers) {
		final List<Locale> locales = headers.getAcceptableLanguages();
		final Locale locale = locales.isEmpty() ? Locale.GERMAN : locales.get(0);
		final Bestellposition bestellposition = bs.findBestellpositionById(id, locale);

		if (bestellposition == null) {
			final String msg = MSG_OBJECT_NOT_FOUND;
			throw new NotFoundException(msg);
		}

		// URLs innerhalb der Bestellposition anpassen
		//uriHelperBestellposition.updateUriBestellposition(bestellposition, uriInfo);

		return bestellposition.getLieferung();
	}
	
	@GET
	@Path("{id:[1-9][0-9]*}/bestellung")
	@Wrapped(element = "bestellung")
	public Bestellung findBestellungByBestellpositionId(@PathParam("id") Long id,
			@Context UriInfo uriInfo, @Context HttpHeaders headers) {
		final List<Locale> locales = headers.getAcceptableLanguages();
		final Locale locale = locales.isEmpty() ? Locale.GERMAN : locales.get(0);
		final Bestellposition bestellposition = bs.findBestellpositionById(id, locale);

		if (bestellposition == null) {
			final String msg = MSG_OBJECT_NOT_FOUND;
			throw new NotFoundException(msg);
		}
		
		final Bestellung bestellung = bestellposition.getBestellung();
		// URLs innerhalb der Bestellung anpassen
		uriHelperBestellung.updateUriBestellung(bestellung, uriInfo);
		
		return bestellung;
	}
	
	@POST
	@Consumes({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
	@Produces
	public Response createBestellposition(
			Bestellposition bestellposition, @Context UriInfo uriInfo, @Context HttpHeaders headers) {
		final List<Locale> locales = headers.getAcceptableLanguages();
		final Locale locale = locales.isEmpty() ? Locale.getDefault() : locales.get(0);
		
		//Schlüssel der Bestellung extrahieren
		final String bestellungUriStr = bestellposition.getBestellungUri().toString();
		int startPos = bestellungUriStr.lastIndexOf('/') + 1;
		final String bestellungIdStr = bestellungUriStr.substring(startPos);
		Long bestellungId = null;
		
		try {
			bestellungId = Long.valueOf(bestellungIdStr);
		}
		catch (NumberFormatException e) {
			throw new NotFoundException("Keine Bestellung vorhanden mit ID " + bestellungIdStr, e);
		}
		
		//bestellung ermitteln, zu der eine neue Bestellposition angelegt werden soll
		final Bestellung bestellung = bs.findBestellungById(bestellungId, locale);
		
		if (bestellung == null) {
			throw new NotFoundException("keine Bestellung vorhanden mit Id " + bestellungId);
		}
		
		//Schlüssel des Produkts extrahieren
		final String produktUriStr = bestellposition.getProduktUri().toString();
		startPos = produktUriStr.lastIndexOf('/') + 1;
		final String produktIdStr = produktUriStr.substring(startPos);
		Long produktId = null;
		
		try {
			produktId = Long.valueOf(produktIdStr);
		}
		catch (NumberFormatException e) {
			throw new NotFoundException("Kein Produkt vorhanden mit ID " + produktIdStr, e);
		}
		
		//Produkt ermitteln, zu dem eine neue Bestellposition angelegt werden soll
		final Produkt produkt = av.findProduktById(produktId, locale);
				
		if (produkt == null) {
			throw new NotFoundException("kein Produkt vorhanden mit Id " + produktId);
		}
		
		//Schlüssel der Lieferung extrahieren
		final String lieferungUriStr = bestellposition.getLieferungUri().toString();
		startPos = lieferungUriStr.lastIndexOf('/') + 1;
		final String lieferungIdStr = lieferungUriStr.substring(startPos);
		Long lieferungId = null;
		
		try {
			lieferungId = Long.valueOf(lieferungIdStr);
		}
		catch (NumberFormatException e) {
			throw new NotFoundException("Keine Lieferung vorhanden mit ID " + lieferungIdStr, e);
		}
		
		//Lieferung ermitteln, zu der eine neue Bestellposition angelegt werden soll
		final Lieferung lieferung = bs.findLieferungById(lieferungId, locale);
				
		if (lieferung == null) {
			throw new NotFoundException("keine Lieferung vorhanden mit Id " + lieferungId);
		}
		
		//Bestellposition erstellen
		
		bestellposition.setBestellung(bestellung);
		bestellung.addBestellposition(bestellposition);
		bestellposition.setProdukt(produkt);	
		//bestellposition.setEinzelpreis(produkt.getPreis());
		bestellposition.setLieferung(lieferung);
		
		bestellposition = bs.createBestellposition(bestellposition, locale);
		LOGGER.tracef("Bestellposition: %s", bestellposition);
		
		final URI bestellpositionUri = uriHelperBestellposition.getUriBestellposition(bestellposition, uriInfo);
		return Response.created(bestellpositionUri).build();
	}
	
	@PUT
	@Consumes({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
	@Produces
	@Log
	public void updateBestellposition(
			Bestellposition bestellposition, @Context UriInfo uriInfo, @Context HttpHeaders headers) {
		// Vorhandene Bestellposition 	ermitteln
		final List<Locale> locales = headers.getAcceptableLanguages();
		final Locale locale = locales.isEmpty() ? Locale.getDefault() : locales.get(0);
		
		
		bestellposition = bs.updateBestellposition(bestellposition, locale);
		if (bestellposition == null) {
			final String msg = "Keine Bestellposition gefunden";
			throw new NotFoundException(msg);
		}
	}
}

