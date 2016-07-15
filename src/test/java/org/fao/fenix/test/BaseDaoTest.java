package org.fao.fenix.test;

import org.springframework.test.jpa.AbstractJpaTests;

public abstract class BaseDaoTest extends AbstractJpaTests {

    protected String[] getConfigLocations() {
        setDependencyCheck(false);
        return new String[] { "applicationContext-test.xml" };
    }
}