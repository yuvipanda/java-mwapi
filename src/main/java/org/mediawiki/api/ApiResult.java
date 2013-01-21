package org.mediawiki.api;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.*;
import javax.xml.xpath.*;

import org.apache.http.client.HttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.mediawiki.api.de.mastacode.http.Http.HttpRequestBuilder;

public class ApiResult {
    private Node doc;
    private XPath evaluator;

    ApiResult(Node doc) {
        this.doc = doc;
        this.evaluator = XPathFactory.newInstance().newXPath();
    }

    static ApiResult fromRequestBuilder(HttpRequestBuilder builder, HttpClient client) throws IOException {
        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputStream response = builder.use(client).charset("utf-8").data("format", "xml").asResponse().getEntity().getContent();
            Document doc = docBuilder.parse(response);
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
    public Node getDocument() {
        return doc;
    }

    public ArrayList<ApiResult> getNodes(String xpath) {
        try {
            ArrayList<ApiResult> results = new ArrayList<ApiResult>();
            NodeList nodes = (NodeList) evaluator.evaluate(xpath, doc, XPathConstants.NODESET);
            for(int i = 0; i < nodes.getLength(); i++) {
                results.add(new ApiResult(nodes.item(i)));
            }
            return results;
        } catch (XPathExpressionException e) {
            return null;
        }
        
    }
    public ApiResult getNode(String xpath) {
        try {
            return new ApiResult((Node) evaluator.evaluate(xpath, doc, XPathConstants.NODE));
        } catch (XPathExpressionException e) {
            return null;
        }
    }
    
    public Double getNumber(String xpath) {
        try {
            return (Double) evaluator.evaluate(xpath, doc, XPathConstants.NUMBER);
        } catch (XPathExpressionException e) {
            return null;
        }
    }
    
    public String getString(String xpath) {
        try {
            return (String) evaluator.evaluate(xpath, doc, XPathConstants.STRING);
        } catch (XPathExpressionException e) {
            return null;
        }
    }
}
