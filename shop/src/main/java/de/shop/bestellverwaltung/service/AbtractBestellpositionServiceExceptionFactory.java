package de.shop.bestellverwaltung.service;

import de.shop.util.ShopException;

public abstract class AbtractBestellpositionServiceExceptionFactory extends ShopException {
	private static final long serialVersionUID = -2849585609393128387L;

	public AbtractBestellpositionServiceExceptionFactory(String msg) {
		super(msg);
	}
	
	public AbtractBestellpositionServiceExceptionFactory(String msg, Throwable t) {
		super(msg, t);
	}
}

