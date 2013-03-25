package de.shop.util;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class ConcurrentDeletedException extends AbstractShopException {
	private static final long serialVersionUID = 3061376326827538106L;
	private final Object id;
	
//	@Resource(lookup = TRANSACTION_NAME)
//	private UserTransaction trans;

	public ConcurrentDeletedException(Object id) {
		super("Das Objekt mit der ID " + id + " wurde konkurrierend geloescht");
		this.id = id;
	}
	
//	@PostConstruct
//	@SuppressWarnings("unused")
//	private void setRollbackOnly() {
//		try {
//			if (trans.getStatus() == STATUS_ACTIVE) {
//				trans.setRollbackOnly();
//			}
//		}
//		catch (IllegalStateException | SystemException e) {
//			throw new InternalError(e);
//		}
//	}
	
	public Object getId() {
		return id;
	}
}
