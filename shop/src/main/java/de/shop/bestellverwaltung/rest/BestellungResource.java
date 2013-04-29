package de.shop.bestellverwaltung.rest;

import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.ArrayList;
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
import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.domain.Lieferung;
import de.shop.bestellverwaltung.service.BestellService;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.service.KundeService;
import de.shop.kundenverwaltung.service.KundeService.FetchType;
import de.shop.util.LocaleHelper;
import de.shop.util.Log;
import de.shop.util.NotFoundException;
import de.shop.util.Transactional;

@Path("/bestellung")
@Produces(APPLICATION_JSON)
@Consumes
@RequestScoped
@Transactional
@Log
public class BestellungResource {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private static final String VERSION = "1.0";

	@Context
	private UriInfo uriInfo;

	@Context
	private HttpHeaders headers;

	@Inject
	private BestellService bs;

	@Inject
	private KundeService ks;

	@Inject
	private ProduktService ps;

	@Inject
	private UriHelperBestellung uriHelperBestellung;

	@Inject
	private UriHelperBestellposition uriHelperBestellposition;

	@Inject
	private LocaleHelper localeHelper;

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
	 * Mit der URL /bestellung/{id} einen Kunden ermitteln
	 * 
	 * @param id
	 *            ID des Kunden
	 * @return Objekt mit Kundendaten, falls die ID vorhanden ist
	 */
	@GET
	@Wrapped(element = "bestellung")
	public Collection<Bestellung> findBestellungen() {
		final Locale locale = localeHelper.getLocale(headers);
		final Collection<Bestellung> bestellungen = bs.findBestellungen(locale);

		if (bestellungen.isEmpty()) {
			final String msg = "{object.notFound}";
			throw new NotFoundException(msg);
		}

		// URLs innerhalb des gefundenen Kunden anpassen
		for (Bestellung bestellung : bestellungen) {
			uriHelperBestellung.updateUriBestellung(bestellung, uriInfo);
		}

		return bestellungen;
	}

	@GET
	@Path("{id:[1-9][0-9]*}")
	@Wrapped(element = "bestellung")
	public Bestellung findBestellungById(@PathParam("id") Long id) {
		final Locale locale = localeHelper.getLocale(headers);
		Bestellung bestellung = bs.findBestellungById(id, locale);

		if (bestellung == null) {
			final String msg = "{object.notFound}";
			throw new NotFoundException(msg);
		}

		// URLs innerhalb der Lieferung anpassen
		uriHelperBestellung.updateUriBestellung(bestellung, uriInfo);

		return bestellung;
	}

	@GET
	@Path("{id:[1-9][0-9]*}/bestellpositionen")
	@Wrapped(element = "bestellposition")
	public Collection<Bestellposition> findBestellpositionenByBestellungId(@PathParam("id") Long id) {
		final Locale locale = localeHelper.getLocale(headers);
		Bestellung bestellung = bs.findBestellungById(id, locale);
		Collection<Bestellposition> bestellpositionen = bs.findBestellpositionenByBestellung(bestellung);

		if (bestellpositionen.isEmpty()) {
			final String msg = "{object.notFound}";
			throw new NotFoundException(msg);
		}

		for (Bestellposition bestellposition : bestellpositionen) {
			uriHelperBestellposition.updateUriBestellposition(bestellposition, uriInfo);
		}

		return bestellpositionen;
	}

	@GET
	@Path("{id:[1-9][0-9]*}/kunde")
	@Wrapped(element = "kunde")
	public Kunde findKundeByBestellungId(@PathParam("id") Long id) {
		final Locale locale = localeHelper.getLocale(headers);
		Bestellung bestellung = bs.findBestellungById(id, locale);
		if (bestellung != null) {
			Kunde kunde = bestellung.getKunde();
			if (kunde == null) {
				final String msg = "Kein Kunde gefunden";
				throw new NotFoundException(msg);
			}
			return kunde;
		} else {
			// TODO msg passend zu locale
			final String msg = "Keine Bestellung gefunden mit der ID " + id;
			throw new NotFoundException(msg);
		}
	}

	@POST
	@Consumes(APPLICATION_JSON)
	@Produces
	public Response createBestellung(Bestellung bestellung) throws Exception {
		final Locale locale = localeHelper.getLocale(headers);
		final String kundeUriStr = bestellung.getKundeUri().toString();
		int startPos = kundeUriStr.lastIndexOf('/') + 1;
		final String kundeIdStr = kundeUriStr.substring(startPos);
		Long kundeId = null;

		try {
			kundeId = Long.valueOf(kundeIdStr);
		} catch (NumberFormatException e) {
			throw new NotFoundException("Kein Kunde vorhanden mit ID " + kundeIdStr, e);
		}

		final Kunde kunde = ks.findKundeById(kundeId, FetchType.NUR_KUNDE, locale);

		if (kunde == null) {
			throw new NotFoundException("kein Kunde vorhanden mit Id " + kundeId);
		}

		Collection<Bestellposition> bestellpositionen = bestellung.getBestellpositionen();
		final List<Bestellposition> neueBestellpositionen = new ArrayList<>(bestellpositionen.size());
		// List<Long> produktIds = new ArrayList<>(bestellpositionen.size());

		for (Bestellposition bp : bestellpositionen) {
			// Ermittle Produkt
			// TODO momentant wird für jeden Artikel und für jede Lieferung ein
			// DB-Zugriff durchgeführt
			// Bitte beizeiten noch entsprechende Methoden zur Bestell- und
			// Artikelverwaltung zum
			// zum finden mehrer Objekte anhand der Ids bereitstellen
			final String produktUriStr = bp.getProduktUri().toString();
			startPos = produktUriStr.lastIndexOf('/') + 1;
			final String produktIdStr = produktUriStr.substring(startPos);
			Long produktId = null;

			try {
				produktId = Long.valueOf(produktIdStr);
			} catch (NumberFormatException e) {
				throw new NumberFormatException("Kein Produkt vorhanden mit ID " + produktIdStr);
			}

			Produkt produkt = ps.findProduktById(produktId, locale);
			if (produkt == null) {
				throw new Exception("Kein Produkt vorhanden mit ID " + produktId);
			}

			// Ermittle Lieferung
			final String lieferungUriStr = bp.getLieferungUri().toString();
			startPos = lieferungUriStr.lastIndexOf('/') + 1;
			final String lieferungIdStr = lieferungUriStr.substring(startPos);
			Long lieferungId = null;
			try {
				lieferungId = Long.valueOf(lieferungIdStr);
			} catch (NumberFormatException e) {
				throw new NumberFormatException("Keine Lieferung vorhanden mit ID " + lieferungIdStr);
			}
			Lieferung lieferung = bs.findLieferungById(lieferungId, locale);
			if (lieferung == null) {
				throw new Exception("Keine Lieferung vorhanden mit ID " + lieferungId);
			}

			bp.setLieferung(lieferung);
			bp.setProdukt(produkt);
			// füge bestellposition hinzu
			neueBestellpositionen.add(bp);
		}

		if (neueBestellpositionen.isEmpty()) {
			// keine einzige gueltige Artikel-ID
			final StringBuilder sb = new StringBuilder("Keine Artikel vorhanden mit den IDs: ");
			for (Bestellposition bp : bestellpositionen) {
				final String produktUriStr = bp.getProduktUri().toString();
				startPos = produktUriStr.lastIndexOf('/') + 1;
				sb.append(produktUriStr.substring(startPos));
				sb.append(" ");
			}
			throw new NotFoundException(sb.toString());
		}

		bestellung.setBestellpositionen(neueBestellpositionen);

		bestellung = bs.createBestellung(bestellung, kunde, locale);

		LOGGER.log(FINEST, "Bestellung: {0}", bestellung);

		final URI bestellungUri = uriHelperBestellung.getUriBestellung(bestellung, uriInfo);
		return Response.created(bestellungUri).build();
	}

	@PUT
	@Consumes(APPLICATION_JSON)
	@Produces
	@Log
	public void updateBestellung(Bestellung bestellung) {
		final Locale locale = localeHelper.getLocale(headers);
		Bestellung orgBestellung = bs.findBestellungById(bestellung.getId(), locale);
		
		if (orgBestellung == null) {
			String msg = "Bestellung nicht gefunden";
			throw new NotFoundException(msg);
		}
		
		List<Bestellposition> orgBestellpositionen = bs.findBestellpositionenByBestellung(orgBestellung);
		List<Bestellposition> bestellpositionen = orgBestellung.getBestellpositionen();
		Bestellposition bestellposition = null;
		
		int count = 0;
		for (Bestellposition bp : bestellpositionen) {
			bestellposition = orgBestellpositionen.get(count);
			if (bestellposition == null)
				continue;
			
			// Extrahiere Lieferung ID (Produkt erhält kein update)
			final String lieferungUriStr = bestellposition.getLieferungUri().toString();
			int startPos = lieferungUriStr.lastIndexOf('/') + 1;
			final String lieferungIdStr = lieferungUriStr.substring(startPos);
			
			// TODO Produkt und Bestellung URI Updaten (ID extrahieren)
			
			Long lieferungId = null;

			try {
				lieferungId = Long.valueOf(lieferungIdStr);
			} catch (NumberFormatException e) {
				throw new NotFoundException("keine Lieferung vorhanden mit ID " + lieferungIdStr, e);
			}

			Lieferung lieferung = bs.findLieferungById(lieferungId, locale);
			bestellposition.setLieferung(lieferung);

			bp.setValues(bestellposition);

			count++;
		}
		
		bestellung.setBestellpositionen(bestellpositionen);
		LOGGER.log(FINEST, "Bestellung: %s", orgBestellung);
				
		bs.updateBestellung(bestellung, locale);
	}
}
