package de.shop.util;

import static javax.transaction.Status.STATUS_ACTIVE;
import static javax.transaction.Status.STATUS_NO_TRANSACTION;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;

import javax.annotation.Resource;
import javax.ejb.ApplicationException;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.jboss.logging.Logger;


@Transactional
@Interceptor
// TODO Entfaellt ab JTA 1.2 (Java EE 7)
public class TransactionalInterceptor implements Serializable {
	private static final long serialVersionUID = 5962891407714952654L;
	
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	@Resource
	private transient UserTransaction trans;

	@AroundInvoke
	public Object manageTransaction(InvocationContext ctx) throws Exception {
		boolean started = false;
		if (trans.getStatus() != STATUS_ACTIVE) {
			trans.begin();
			LOGGER.trace("Transaktion gestartet >>>");
			started = true;
		}
		
		Object result = null;
		try {
			result = ctx.proceed();
		}
		catch (AbstractShopException e) {
			if (started) {
				final ApplicationException annotationApplicationException =
		                   e.getClass().getAnnotation(ApplicationException.class);
				if (annotationApplicationException != null && annotationApplicationException.rollback()) {
					trans.rollback();
					LOGGER.trace("<<< Rollback durchgefuehrt");
				}
				else {
					endOfTransaction();
				}
			}
			
			throw e;
		}
		
		if (started) {
			endOfTransaction();
		}

		return result;
	}

	private void endOfTransaction() throws SystemException {
		switch (trans.getStatus()) {
			case STATUS_ACTIVE:
				try {
					LOGGER.trace(">>> Commit beginnt");
					trans.commit();
					LOGGER.trace("<<< Commit beendet");
				}
				catch (SystemException | HeuristicRollbackException | HeuristicMixedException
					   | RollbackException e) {
					trans.rollback();
					LOGGER.trace("<<< Rollback durchgefuehrt");
				}
				break;
				
			case STATUS_NO_TRANSACTION:
				break;
				
			default:
				trans.rollback();
				LOGGER.trace("<<< Rollback durchgefuehrt");
				break;
		}
	}
}
