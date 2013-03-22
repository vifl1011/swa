package de.shop.bestellverwaltung.rest;

import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.TEXT_XML;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

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

import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;

import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.bestellverwaltung.domain.Lieferung;
import de.shop.bestellverwaltung.service.BestellService;
import de.shop.util.Log;
import de.shop.util.NotFoundException;

@Path("/lieferung")
@Produces({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
@Consumes
@RequestScoped
@Log
public class LieferungResource {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private static final String VERSION = "1.0";

	@Inject
	private BestellService bs;

	@Inject
	private UriHelperLieferung uriHelperLieferung;

	@PostConstruct
	private void postConstruct() {
		LOGGER.log(FINER, "CDI-faehiges Bean {0} wurde erzeugt", this);
	}

	@PreDestroy
	private void preDestroy() {
		LOGGER.log(FINER, "CDI-faehiges Bean {0} wird geloescht", this);
	}

	@GET
	@Produces(TEXT_PLAIN)
	@Path("version")
	public String getVersion() {
		return VERSION;
	}

	@GET
	@Wrapped(element = "lieferung")
	public Collection<Lieferung> findAllLieferungen(@Context UriInfo uriInfo,
			@Context HttpHeaders headers) {
		final List<Locale> locales = headers.getAcceptableLanguages();
		final Locale locale = locales.isEmpty() ? Locale.GERMAN : locales.get(0);
		final Collection<Lieferung> lieferungen = bs.findLieferungen(locale);

		if (lieferungen.isEmpty()) {
			// TODO msg passend zu locale
			final String msg = "Keine Kunden gefunden ";
			throw new NotFoundException(msg);
		}

		return lieferungen;
	}
	
	@GET
	@Path("{id:[1-9][0-9]*}")
	@Wrapped(element = "lieferung")
	public Lieferung findLieferung(@PathParam("id") Long id, @Context UriInfo uriInfo,
			@Context HttpHeaders headers) {
		final List<Locale> locales = headers.getAcceptableLanguages();
		final Locale locale = locales.isEmpty() ? Locale.GERMAN : locales.get(0);
		final Lieferung lieferung = bs.findLieferungById(id, locale);

		if (lieferung == null) {
			// TODO msg passend zu locale
			final String msg = "Keine Kunden gefunden ";
			throw new NotFoundException(msg);
		}
		
		uriHelperLieferung.updateUriLieferung(lieferung, uriInfo);
		
		return lieferung;
	}
	
	@GET
	@Path("{id:[1-9][0-9]*}/bestellpositionen")
	@Wrapped(element = "bestellposition")
	public Collection<Bestellposition> findBestellpositionenByLieferung(
			@PathParam("id") Long id, @Context UriInfo uriInfo, @Context HttpHeaders headers) {
		final List<Locale> locales = headers.getAcceptableLanguages();
		final Locale locale = locales.isEmpty() ? Locale.GERMAN : locales.get(0);
		final Collection<Bestellposition> bestellpositionen = bs.findLieferungById(id, locale).getBestellpositionen();

		if (bestellpositionen.isEmpty()) {
			// TODO msg passend zu locale
			final String msg = "Keine Kunden gefunden ";
			throw new NotFoundException(msg);
		}

		return bestellpositionen;
	}
	
	@POST
	@Consumes({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
	@Produces
	public Response createLieferung(Lieferung lieferung, @Context UriInfo uriInfo, @Context HttpHeaders headers) {
		final List<Locale> locales = headers.getAcceptableLanguages();
		final Locale locale = locales.isEmpty() ? Locale.getDefault() : locales.get(0);
		
		lieferung = bs.createLieferung(lieferung, locale);
		//LOGGER.log(FINEST, "Lieferung: {0}", lieferung);
		
		final URI lieferungUri = uriHelperLieferung.getUriLieferung(lieferung, uriInfo);
		return Response.created(lieferungUri).build();
	}
	
	@PUT
	@Consumes({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
	@Produces
	@Log
	public void updateLieferung(Lieferung lieferung, @Context UriInfo uriInfo, @Context HttpHeaders headers) {
		// Vorhandenen Kunden 	ermitteln
		final List<Locale> locales = headers.getAcceptableLanguages();
		final Locale locale = locales.isEmpty() ? Locale.getDefault() : locales.get(0);
		Lieferung orgLieferung = bs.findLieferungById(lieferung.getId(), locale);
		
		if (orgLieferung == null) {
			String msg = "Kunde nicht gefunden";
			throw new NotFoundException(msg);
		}
		LOGGER.log(FINEST, "Kunde vorher: %s", orgLieferung);
		orgLieferung.setValues(lieferung);
		LOGGER.log(FINEST, "Kunde nachher: %s", orgLieferung);
		
		lieferung = bs.updateLieferung(orgLieferung, locale);
		if (lieferung == null) {
			final String msg = "Kein Kunde gefunden mit der ID " + orgLieferung.getId();
			throw new NotFoundException(msg);
		}
	}
}