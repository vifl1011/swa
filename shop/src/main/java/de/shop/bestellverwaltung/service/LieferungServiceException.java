package de.shop.bestellverwaltung.service;

import de.shop.util.ShopException;

public abstract class LieferungServiceException extends ShopException {
	private static final long serialVersionUID = -2849585609393128387L;

	public LieferungServiceException(String msg) {
		super(msg);
	}
	
	public LieferungServiceException(String msg, Throwable t) {
		super(msg, t);
	}
}
