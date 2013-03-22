package de.shop.artikelverwaltung.rest;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.CONFLICT;

import java.util.Collection;
import java.util.logging.Logger;

import javax.validation.ConstraintViolation;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import de.shop.artikelverwaltung.domain.Produkt;
import de.shop.artikelverwaltung.service.ProduktValidationException;


@Provider
public class ProduktValidationExceptionMapper implements ExceptionMapper<ProduktValidationException> {
	private static final Logger LOGGER = Logger.getLogger(ProduktValidationExceptionMapper.class.getName());

	@Override
	public Response toResponse(ProduktValidationException e) {
		LOGGER.entering(ProduktValidationExceptionMapper.class.getName(), "toResponse", e.getMessage());
		
		final Collection<ConstraintViolation<Produkt>> violations = e.getViolations();
		final StringBuilder sb = new StringBuilder();
		for (ConstraintViolation<Produkt> v : violations) {
			sb.append(v.getMessage());
			sb.append('\n');
		}
		final String msg = sb.toString();
		
		final Response response = Response.status(CONFLICT)
		                                  .type(TEXT_PLAIN)
		                                  .entity(msg)
		                                  .build();
		
		LOGGER.exiting(ProduktValidationExceptionMapper.class.getName(), "toResponse", msg);
		return response;
	}

}
