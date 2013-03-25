package de.shop.util;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.CONFLICT;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


@Provider
@ApplicationScoped
@Log
public class OptimisticLockExceptionMapper implements ExceptionMapper<OptimisticLockException> {
	@PersistenceContext
	private EntityManager em;
	@Override
	public Response toResponse(OptimisticLockException e) {
		final Object id = em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(e.getEntity());
		final String msg = "Konkurrierendes Update fuer das Objekt mit der ID: " + id;
		final Response response = Response.status(CONFLICT)
		                                  .type(TEXT_PLAIN)
		                                  .entity(msg)
		                                  .build();
		return response;
	}
}
