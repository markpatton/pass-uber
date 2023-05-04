package org.eclipse.pass.metadataschema;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import com.yahoo.elide.Elide;
import com.yahoo.elide.ElideSettings;
import com.yahoo.elide.RefreshableElide;
import com.yahoo.elide.core.datastore.DataStore;
import com.yahoo.elide.core.datastore.DataStoreTransaction;
import com.yahoo.elide.core.dictionary.EntityDictionary;

public final class SchemaTestUtils {

    private SchemaTestUtils() {}

    /**
     * Returns a RefreshableElideMocked object. The RefreshableElideMocked object contains
     * a mocked RefreshableElide that can be passed to constructors as required. It also
     * contains a mocked DataStoreTransaction that can be used to create mocked calls for
     * elide CRUD operations.
     * @return a RefreshableElideMocked object
     */
    static RefreshableElideMocked getMockedRefreshableElide() {
        RefreshableElide refreshableElideMock = mock(RefreshableElide.class);
        Elide elideMock = mock(Elide.class);
        when(refreshableElideMock.getElide()).thenReturn(elideMock);
        ElideSettings elideSettingsMock = mock(ElideSettings.class);
        when(elideMock.getElideSettings()).thenReturn(elideSettingsMock);
        EntityDictionary entityDictionaryMock = mock(EntityDictionary.class);
        when(elideSettingsMock.getDictionary()).thenReturn(entityDictionaryMock);
        when(elideSettingsMock.getDictionary().getApiVersions()).thenReturn(Set.of("1"));
        DataStore dataStoreMock = mock(DataStore.class);
        when(elideMock.getDataStore()).thenReturn(dataStoreMock);
        DataStoreTransaction dataStoreTransactionMock = mock(DataStoreTransaction.class);
        when(dataStoreMock.beginReadTransaction()).thenReturn(dataStoreTransactionMock);
        return new RefreshableElideMocked(refreshableElideMock, dataStoreTransactionMock);
    }

    static class RefreshableElideMocked {
        private final RefreshableElide refreshableElideMock;
        private final DataStoreTransaction dataStoreTransactionMock;

        RefreshableElideMocked(RefreshableElide refreshableElideMock, DataStoreTransaction dataStoreTransactionMock) {
            this.refreshableElideMock = refreshableElideMock;
            this.dataStoreTransactionMock = dataStoreTransactionMock;
        }

        RefreshableElide getRefreshableElideMock() {
            return refreshableElideMock;
        }

        DataStoreTransaction getDataStoreTransactionMock() {
            return dataStoreTransactionMock;
        }
    }
}
