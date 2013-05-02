package de.shop.util;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


public class JsfProducers {
	@Produces
	@RequestScoped
	// http://docs.oracle.com/javaee/6/api/index.html?javax/faces/context/FacesContext.html
	private static FacesContext getFacesContext() {
		final FacesContext ctx = FacesContext.getCurrentInstance();
		if (ctx == null) {
			throw new ContextNotActiveException("FacesContext is not active");
		}
		return ctx;
	}

	@Produces
	@RequestScoped
	// http://docs.oracle.com/javaee/6/api/index.html?javax/servlet/http/ExternalContext.html
	private static ExternalContext getExternalContext(final FacesContext ctx) {
		if (ctx == null) {
			throw new ContextNotActiveException("FacesContext is not active");
		}
		final ExternalContext externalCtx = ctx.getExternalContext();
		if (externalCtx == null) {
			throw new ContextNotActiveException("ExternalContext is not active");
		}
		return externalCtx;
	}

	@Produces
	@RequestScoped
	// TODO Entfaellt ab CDI 1.1 (Java EE 7)
	// http://docs.oracle.com/javaee/6/api/index.html?javax/servlet/http/HttpServletRequest.html
	private static HttpServletRequest getHttpServletRequest(final ExternalContext ctx) {
		return (HttpServletRequest) ctx.getRequest();
	}
	
	@Produces
	@RequestScoped
	// TODO Entfaellt ab CDI 1.1 (Java EE 7)
	// http://docs.oracle.com/javaee/6/api/index.html?javax/servlet/http/HttpSession.html
	private static HttpSession getHttpSession(final ExternalContext ctx) {
		return (HttpSession) ctx.getSession(false);
	}
	
	@Produces
	@RequestScoped
	// http://docs.oracle.com/javaee/6/api/index.html?javax/faces/context/Flash.html
	private static Flash getFlash(final ExternalContext ctx) {
		final Flash flash = ctx.getFlash();
		if (flash == null) {
			throw new ContextNotActiveException("Flash is not active");
		}

		return flash;
	}
}
