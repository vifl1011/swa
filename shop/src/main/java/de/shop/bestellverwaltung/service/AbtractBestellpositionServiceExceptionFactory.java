package de.shop.bestellverwaltung.service;

import de.shop.util.AbstractShopExceptionFactory;

public abstract class AbtractBestellpositionServiceExceptionFactory extends AbstractShopExceptionFactory {
	private static final long serialVersionUID = -2849585609393128387L;

	public AbtractBestellpositionServiceExceptionFactory(String msg) {
		super(msg);
	}
	
	public AbtractBestellpositionServiceExceptionFactory(String msg, Throwable t) {
		super(msg, t);
	}
}

