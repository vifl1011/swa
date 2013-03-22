package de.shop.artikelverwaltung.service;

import de.shop.util.AbstractShopException;

public abstract class AbstractProduktServiceException extends AbstractShopException {
	private static final long serialVersionUID = -2849585609393128387L;

	public AbstractProduktServiceException(String msg) {
		super(msg);
	}
	
	public AbstractProduktServiceException(String msg, Throwable t) {
		super(msg, t);
	}
}
