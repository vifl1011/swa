package de.shop.util;

public abstract class ShopException extends RuntimeException {
	private static final long serialVersionUID = -1030863258479949134L;

	public ShopException(String msg) {
		super(msg);
	}

	public ShopException(String msg, Throwable t) {
		super(msg, t);
	}
}
