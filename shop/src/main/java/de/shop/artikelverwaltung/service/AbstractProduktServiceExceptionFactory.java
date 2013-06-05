package de.shop.artikelverwaltung.service;

import de.shop.util.AbstractShopException;

public abstract class AbstractProduktServiceExceptionFactory extends AbstractShopException {
	private static final long serialVersionUID = -2849585609393128387L;

	public AbstractProduktServiceExceptionFactory(String msg) {
		super(msg);
	}
	
	public AbstractProduktServiceExceptionFactory(String msg, Throwable t) {
		super(msg, t);
	}
}
