package org.mediawiki.api;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

public class MWApiTest {

    // Test accounts on local wiki. For write tests
    // Setup your local wiki and create this account before running tests
    private final String USERNAME = "admin";
    private final String PASSWORD = "testtest";
    private final String WRITEAPIURL = "http://localhost/w/api.php";

    // Use testwiki for read only tests
    private final String READAPIURL = "http://test.wikipedia.org/w/api.php";

    private MWApi api;

    @Before
    public void setUp() {
        api = new MWApi(READAPIURL, new DefaultHttpClient());
    }

    private void setupWriteableAPI() {
        api = new MWApi(WRITEAPIURL, new DefaultHttpClient());
    }

    @Test
    public void testSiteMatrix() throws IOException {
        ApiResult result = api.action("sitematrix").param("smlimit", 10).get();
        assertTrue(result.getNode("//sitematrix") != null);
        assertEquals(10, result.getNodes("//sitematrix/language").size());
    }

    @Test
    public void testLogin() throws IOException {
        setupWriteableAPI();
        assertEquals("Success", api.login(USERNAME, PASSWORD));
    }

    @Test
    public void testLogout() throws IOException {
        setupWriteableAPI();
        // Login, check token, logout, check token
        assertEquals("Success", api.login(USERNAME, PASSWORD));
        assertFalse("+\\".equals(api.getEditToken()));
        api.logout();
        assertEquals("+\\", api.getEditToken());
    }

    @Test
    public void testLoggedInEditAttempt() throws IOException {
        setupWriteableAPI();
        assertEquals("Success", api.login(USERNAME, PASSWORD));
        String token = api.getEditToken();
        String text = "Has anyone really been far even as decided to use even go want to do look more like?";
        String title = "India";
        ApiResult editResult = api.action("edit").param("title", title).param("text", text).param("token", token).param("summary", "Sample summary").post();
        assertEquals("Success", editResult.getString("/api/edit/@result"));
        ApiResult checkResult = api.action("parse").param("page", title).param("prop", "wikitext").get();
        assertEquals(text, checkResult.getString("/api/parse/wikitext"));
    }

    @Test
    public void testAnonymousEditToken() throws IOException {
        // +\ is anonymous edit token
        assertEquals("+\\", api.getEditToken());
    }

}
