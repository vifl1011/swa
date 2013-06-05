package de.shop.artikelverwaltung.rest;

import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
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

import de.shop.artikelverwaltung.domain.Produkt;
import de.shop.artikelverwaltung.service.ProduktService;
import de.shop.util.Log;
import de.shop.util.NotFoundException;
import de.shop.util.Transactional;

@Path("/produkt")
//@Produces({ APPLICATION_XML, TEXT_XML, APPLICATION_JSON })
@Produces({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
@Consumes
@RequestScoped
@Transactional
@Log
public class ProduktResource {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles
			.lookup().lookupClass().getName());

	@Inject
	private ProduktService ps;

	@Inject
	private UriHelperProdukt uriHelperProdukt;

	@PostConstruct
	private void postConstruct() {
		LOGGER.log(FINER, "CDI-faehiges Bean {0} wurde erzeugt", this);
	}

	@PreDestroy
	private void preDestroy() {
		LOGGER.log(FINER, "CDI-faehiges Bean {0} wird geloescht", this);
	}

	@GET
	@Path("{id:[1-9][0-9]*}")
	public Produkt findProduktById(@PathParam("id") Long id,
			@Context UriInfo uriInfo) {
		// final Produkt produkt = ps.findProduktById(id);
		final Produkt produkt = ps.findProduktByIdEm(id);
		if (produkt == null) {
			final String msg = "Kein Produkt gefunden mit der ID " + id;
			throw new NotFoundException(msg);
		}

		return produkt;
	}
	
	@GET
	@Path("")
	@Wrapped(element = "produkt")
	public Collection<Produkt> findAlleProdukte(@Context UriInfo uriInfo,
			                           @Context HttpHeaders headers) {
		final Collection<Produkt> produkte = ps.findAlleProdukte();
		if (produkte.isEmpty()) {
			final String msg = "Keine Produkte gefunden ";
			throw new NotFoundException(msg);
		}
	
//		for (Produkt produkt : produkte) {
//			uriHelperProdukt.getUriProdukt(produkt, uriInfo);
//		}
		return produkte;
	}

	@POST
	@Consumes({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
//	@Consumes({ APPLICATION_XML, TEXT_XML })
	@Produces
	public Response createProdukt(Produkt produkt, @Context UriInfo uriInfo,
			@Context HttpHeaders headers) {
		final List<Locale> locales = headers.getAcceptableLanguages();
		final Locale locale = locales.isEmpty() ? Locale.getDefault() : locales
				.get(0);

		produkt = ps.createProdukt(produkt, locale);
		LOGGER.log(FINEST, "Kunde: {0}", produkt);

		final URI produktUri = uriHelperProdukt.getUriProdukt(produkt, uriInfo);
		return Response.created(produktUri).build();
	}

	
	// bsp
	
//	@PUT
//	@Consumes(APPLICATION_JSON)
//	public void updateKunde(Produkt produkt) {
//		final Locale locale = localeHelper.getLocale(headers);
//
//		// Vorhandenen Kunden ermitteln
//		final AbstractKunde origKunde = ks.findKundeById(kunde.getId(), FetchType.NUR_KUNDE, locale);
//		if (origKunde == null) {
//			// TODO msg passend zu locale
//			final String msg = "Kein Kunde gefunden mit der ID " + kunde.getId();
//			throw new NotFoundException(msg);
//		}
//		LOGGER.tracef("Kunde vorher = %s", origKunde);
//	
//		// Daten des vorhandenen Kunden ueberschreiben
//		origKunde.setValues(kunde);
//		LOGGER.tracef("Kunde nachher = %s", origKunde);
//		
//		// Update durchfuehren
//		ks.updateKunde(origKunde, locale, false);
//	}
	
	
	
	
	
	// 			ALT
	 @PUT
	 @Consumes({APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
	 @Produces
	 @Log
	 public void updateProdukt(Produkt produkt, @Context UriInfo uriInfo,
	 @Context HttpHeaders headers) {
	 final List<Locale> locales = headers.getAcceptableLanguages();
	 final Locale locale = locales.isEmpty() ? Locale.getDefault() 
			 : locales.get(0);
	 
	 final Produkt orgProdukt = ps.findProduktById(produkt.getId(), locale);
	
	 if (orgProdukt == null) {
	 final String msg = "Kein Produkt gefunden! ";
	 throw new NotFoundException(msg);
	 }
	 
	 orgProdukt.setValues(produkt);
	 
	 produkt = ps.updateProdukt(orgProdukt, locale);
	
	 if (produkt == null) {
		 final String msg = "Kein Produkt gefunden!";
		 throw new NotFoundException(msg);
	 	 }	
	 }

}