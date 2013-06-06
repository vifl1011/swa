package de.shop.bestellverwaltung.service;

import de.shop.util.AbstractShopExceptionFactory;

public abstract class AbstractBestellpositionServiceExceptionFactory extends AbstractShopExceptionFactory {
	private static final long serialVersionUID = -2849585609393128387L;

	public AbstractBestellpositionServiceExceptionFactory(String msg) {
		super(msg);
	}
	
	public AbstractBestellpositionServiceExceptionFactory(String msg, Throwable t) {
		super(msg, t);
	}
}

