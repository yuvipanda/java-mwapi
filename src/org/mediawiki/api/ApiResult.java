package org.mediawiki.api;

import java.util.ArrayList;

import javax.xml.xpath.*;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ApiResult {
    private Node doc;
    private XPath evaluator;

    ApiResult(Node doc) {
        this.doc = doc;
        this.evaluator = XPathFactory.newInstance().newXPath();
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
