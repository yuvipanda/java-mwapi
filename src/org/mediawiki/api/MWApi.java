package org.mediawiki.api;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
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
    
    public ApiResult upload(String filename, InputStream file, String text, String comment) throws IOException {
        String token = this.getEditToken();
        HttpRequestBuilder builder = Http.multipart(apiURL)
                .data("action", "upload")
                .data("token", token)
                .data("text", text)
                .data("ignorewarnings", "1")
                .data("comment", comment)
                .data("filename", filename)
                .file("file", filename, file);
        return ApiResult.fromRequestBuilder(builder, client);
    }
    
    public void logout() throws IOException {
        this.action("logout").post();
    }

    public String getEditToken() throws IOException {
        ApiResult result = this.action("tokens").param("type", "edit").get();
        return result.getString("/api/tokens/@edittoken");
    }

    private ApiResult makeRequest(String method, HashMap<String, Object> params) throws IOException {
        HttpRequestBuilder builder;
        if (method == "POST") {
            builder = Http.post(apiURL);
        } else {
            builder = Http.get(apiURL);
        }
        builder.data(params);
        return ApiResult.fromRequestBuilder(builder, client);
    }
}
;