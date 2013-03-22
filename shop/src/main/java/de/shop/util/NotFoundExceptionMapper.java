package de.shop.util;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.util.logging.Logger;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {
	private static final Logger LOGGER = Logger.getLogger(NotFoundExceptionMapper.class.getName());

	@Override
	public Response toResponse(NotFoundException e) {
		LOGGER.entering(NotFoundExceptionMapper.class.getName(), "toResponse", e.getMessage());
		
		final String msg = e.getMessage();
		final Response response = Response.status(NOT_FOUND)
		                                  .type(TEXT_PLAIN)
		                                  .entity(msg)
		                                  .build();
		
		LOGGER.exiting(NotFoundExceptionMapper.class.getName(), "toResponse", msg);
		return response;
	}

}
