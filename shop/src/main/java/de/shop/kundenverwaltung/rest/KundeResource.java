package de.shop.kundenverwaltung.rest;

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

import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.service.BestellService;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.service.KundeService;
import de.shop.kundenverwaltung.service.KundeService.FetchType;
import de.shop.util.Config;
import de.shop.util.Log;
import de.shop.util.NotFoundException;
import de.shop.util.Transactional;


@Path("/kunden")
@Produces({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
@Consumes
@RequestScoped
@Transactional
@Log
public class KundeResource {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private static final String VERSION = "1.0";
	
	@Inject
	private Config config;
	
	@Inject
	private KundeService ks;
	
	@Inject
	private BestellService bs;
	
	@Inject
	private UriHelperKunde uriHelperKunde;
	
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
	
	/**
	 * Mit der URL /kunden/{id} einen Kunden ermitteln
	 * @param id ID des Kunden
	 * @return Objekt mit Kundendaten, falls die ID vorhanden ist
	 */
	@GET
	@Path("")
	@Wrapped(element = "kunden")
	public Collection<Kunde> findAllKunden(@Context UriInfo uriInfo,
			                           @Context HttpHeaders headers) {
		final Collection<Kunde> kunden = ks.findAllKunden(FetchType.NUR_KUNDE);
		if (kunden.isEmpty()) {
			// TODO msg passend zu locale
			final String msg = "Keine Kunden gefunden ";
			throw new NotFoundException(msg);
		}
	
		// URLs innerhalb des gefundenen Kunden anpassen
		for (Kunde kunde : kunden) {
			uriHelperKunde.updateUriKunde(kunde, uriInfo);
		}
		return kunden;
	}
	
	/**
	 * Mit der URL /kunden/{id} einen Kunden ermitteln
	 * @param id ID des Kunden
	 * @return Objekt mit Kundendaten, falls die ID vorhanden ist
	 */
	@GET
	@Path("{id:[1-9][0-9]*}")
	public Kunde findKundeById(@PathParam("id") Long id,
			                           @Context UriInfo uriInfo,
			                           @Context HttpHeaders headers) {
		final List<Locale> locales = headers.getAcceptableLanguages();
		final Locale locale = locales.isEmpty() ? config.getDefaultLocale() : locales.get(0);
		final Kunde kunde = ks.findKundeById(id, FetchType.NUR_KUNDE, locale);
		if (kunde == null) {
			// TODO msg passend zu locale
			final String msg = "Kein Kunde gefunden mit der ID " + id;
			throw new NotFoundException(msg);
		}
	
		// URLs innerhalb des gefundenen Kunden anpassen
		uriHelperKunde.updateUriKunde(kunde, uriInfo);
		return kunde;
	}
	
	
	/**
	 * Mit der URL /kunden/{id}/bestellungen die bestellungen eines Kunden ermitteln
	 * @param id ID des Kunden
	 * @return Objekt mit Bestellungen, falls die ID und Bestellungen vorhanden ist
	 */
	@GET
	@Path("{id:[1-9][0-9]*}/bestellungen")
	@Wrapped(element = "bestellungen")
	public Collection<Bestellung> findBestellungenByKundeId(@PathParam("id") Long id,
			                           @Context UriInfo uriInfo,
			                           @Context HttpHeaders headers) {
		final List<Locale> locales = headers.getAcceptableLanguages();
		final Locale locale = locales.isEmpty() ? Locale.getDefault() : locales.get(0);
		final Kunde kunde = ks.findKundeById(id, FetchType.NUR_KUNDE, locale);
		if (kunde == null) {
			// TODO msg passend zu locale
			final String msg = "Kein Kunde gefunden mit der ID " + id;
			throw new NotFoundException(msg);
		}
		final Collection<Bestellung> bestellungen = kunde.getBestellungen();
		if (bestellungen.isEmpty()) {
			final String msg = "Kunde mit der ID " + id + " hat keine Bestellungen";
			throw new NotFoundException(msg);
		}
		
	
		// TODO URLs innerhalb des gefundenen Bestellungen anpassen
		//for (Bestellung bestellung : bestellungen) {
		//	uriHelperBestellung.updateUriBestellung(bestellung, uriInfo);
		//}
		return bestellungen;
	}
	
	@POST
	@Consumes({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
	@Produces
	public Response createKunde(Kunde kunde, @Context UriInfo uriInfo, @Context HttpHeaders headers) {
	//TODO tut leider noch nicht
		final List<Locale> locales = headers.getAcceptableLanguages();
		final Locale locale = locales.isEmpty() ? Locale.getDefault() : locales.get(0);
		
		kunde = ks.createKunde(kunde, locale);
		LOGGER.log(FINEST, "Kunde: {0}", kunde);
		
		final URI kundeUri = uriHelperKunde.getUriKunde(kunde, uriInfo);
		return Response.created(kundeUri).build();
	}
	@PUT
	@Consumes({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
	@Produces
	@Log
	public void updateKunde(Kunde kunde, @Context UriInfo uriInfo, @Context HttpHeaders headers) {
		// Vorhandenen Kunden 	ermitteln
		final List<Locale> locales = headers.getAcceptableLanguages();
		final Locale locale = locales.isEmpty() ? Locale.getDefault() : locales.get(0);
		Kunde orgKunde = ks.findKundeById(kunde.getId(), FetchType.MIT_ADRESSE, locale);
		
		if (orgKunde == null) {
			String msg = "Kunde nicht gefunden";
			throw new NotFoundException(msg);
		}
		LOGGER.log(FINEST, "Kunde vorher: %s", orgKunde);
		orgKunde.setValues(kunde);
		LOGGER.log(FINEST, "Kunde nachher: %s", orgKunde);
		
		kunde = ks.updateKunde(orgKunde, locale);
		if (kunde == null) {
			final String msg = "Kein Kunde gefunden mit der ID " + orgKunde.getId();
			throw new NotFoundException(msg);
		}
	}

}
