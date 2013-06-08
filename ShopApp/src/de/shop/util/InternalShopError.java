package de.shop.util;

public class InternalShopError extends RuntimeException {
	private static final long serialVersionUID = 8756361101145241386L;

	public InternalShopError(String msg) {
		super(msg);
	}
	
	public InternalShopError(String msg, Throwable t) {
		super(msg, t);
	}
}
