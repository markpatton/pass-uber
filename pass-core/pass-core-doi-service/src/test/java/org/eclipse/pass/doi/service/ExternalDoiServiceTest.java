package org.eclipse.pass.doi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ExternalDoiServiceTest {

    ExternalDoiService underTest = new XrefDoiService();

    /**
     * Test that our verify method correctly handles the usual expected doi formats
     */
    @Test
    public void verifyTest() {
        String doi0 = "http://dx.doi.org/10.4137/cmc.s38446";
        assertEquals("10.4137/cmc.s38446", underTest.verify(doi0));

        String doi1 = "https://dx.doi.org/10.4137/cmc.s38446";
        assertEquals("10.4137/cmc.s38446", underTest.verify(doi1));

        String doi2 = "dx.doi.org/10.4137/cmc.s38446";
        assertEquals("10.4137/cmc.s38446", underTest.verify(doi2));

        String doi3 = "10.4137/cmc.s38446";
        assertEquals("10.4137/cmc.s38446", underTest.verify(doi3));

        String doi4 = "4137/cmc.s38446";
        assertNull(underTest.verify(doi4));
    }
}
