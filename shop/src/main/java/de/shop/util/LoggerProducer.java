package de.shop.util;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.logging.Logger;


public class LoggerProducer {
	@Produces
	private static Logger getLogger(InjectionPoint injectionPoint) {
		// InjectionPoint ist nur verfuegbar, wenn das Bean, in das injiziert wird, @Dependent ist
		// und nicht z.B. @RequestScoped oder @SessionScoped.
		// Dieser Producer fuer Logger ist also nur bei den Service-Klassen im Anwendungskern anwendbar.
		return Logger.getLogger(injectionPoint.getMember().getDeclaringClass().getName());
	}
}
