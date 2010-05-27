package net.anvil.commons.test;

import org.easymock.classextension.IMocksControl;

public class MocksControlHelper {

    public static <T> T createMock(final IMocksControl control, final String mockName, final Class<T> mockedType) {
        return control.createMock(mockName, mockedType);
    }
}
