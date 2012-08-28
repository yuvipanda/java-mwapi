package org.mediawiki.api;

import java.io.IOError;
import java.io.IOException;
import java.util.*;

import javax.management.RuntimeErrorException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import de.mastacode.http.*;
import de.mastacode.http.Http.HttpRequestBuilder;

import org.apache.http.client.HttpClient;
import org.json.simple.parser.*;
import org.json.simple.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class MWApi {
    public class RequestBuilder {
        private HashMap<String, Object> params;
        private MWApi api;

        RequestBuilder(MWApi api) {
            params = new HashMap<String, Object>();
            this.api = api;
        }

        public RequestBuilder param(String key, Object value) {
            params.put(key, value);
            return this;
        }

        public ApiResult get() throws IOException {
            return api.makeRequest("GET", params);
        }

        public ApiResult post() throws IOException {
            return api.makeRequest("POST", params);
        }
    }

    private HttpClient client;
    private String apiURL;
    private JSONParser parser;

    public MWApi(String apiURL, HttpClient client) {
        this.apiURL = apiURL;
        this.client = client;
        this.parser = new JSONParser();
    }

    public RequestBuilder action(String action) {
        RequestBuilder builder = new RequestBuilder(this);
        builder.param("action", action);
        return builder;
    }

    public String login(String username, String password) throws IOException {
        ApiResult tokenData = this.action("login").param("lgname", username).param("lgpassword", password).post();
        String result = tokenData.getString("/api/login/@result");
        if (result.equals("NeedToken")) {
            String token = tokenData.getString("/api/login/@token");
            ApiResult confirmData = this.action("login").param("lgname", username).param("lgpassword", password).param("lgtoken", token).post();
            return confirmData.getString("/api/login/@result");
        } else {
            return result;
        }
    }
    
    public void logout() throws IOException {
        this.action("logout").post();
    }

    public String getEditToken() throws IOException {
        ApiResult result = this.action("query").param("prop", "info").param("intoken", "edit").param("titles", "Bohemian Rhapsody").post();
        return result.getString("/api/query/pages/page/@edittoken");
    }

    private ApiResult makeRequest(String method, HashMap<String, Object> params) throws IOException {
        HttpRequestBuilder builder;
        if (method == "POST") {
            builder = Http.post(apiURL);
        } else {
            builder = Http.get(apiURL);
        }
        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = docBuilder.parse(builder.use(client).charset("utf-8").data("format", "xml").data(params).asResponse().getEntity().getContent());
            return new ApiResult(doc);
        } catch (ParserConfigurationException e) {
            // I don't know wtf I can do about this on...
            throw new RuntimeException(e);
        } catch (IllegalStateException e) {
            // So, this should never actually happen - since we assume MediaWiki always generates valid json
            // So the only thing causing this would be a network truncation
            // Sooo... I can throw IOError
            // Thanks Java, for making me spend significant time on shit that happens once in a bluemoon
            // I surely am writing Nuclear Submarine controller code
            throw new IOError(e);
        } catch (SAXException e) {
            // See Rant above
            throw new IOError(e);
        }
    }
}
