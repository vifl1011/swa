package de.shop.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.SystemException;

import org.junit.Before;

public abstract class AbstractDomainTest extends AbstractTest {
	@PersistenceContext
	private EntityManager em;
	
	@Before
	public void before() throws SystemException {   // NotSupportedException
		super.before();
		
		assertThat(em, is(notNullValue()));
	}
	
	protected EntityManager getEntityManager() {
		return em;
	}
}
