package org.ddialliance.ddiftp.util.xml;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.ddialliance.ddiftp.util.log.Log;
import org.ddialliance.ddiftp.util.log.LogFactory;
import org.ddialliance.ddiftp.util.log.LogType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Handles XPath and XQuery expressions on XML objects
 */
public class XQueryUtil {
	private Log logSystem = LogFactory.getLog(LogType.SYSTEM, this.getClass());
	private Map<String, String> namespaces = new HashMap<String, String>();
	
	public XQueryUtil(Map<String, String> namespaces) {
		this.namespaces = namespaces;
	}
	
	public XQueryUtil(XmlObject xmlObject) {
		XmlCursor xmlCursor = xmlObject.newCursor();
		xmlCursor.toFirstChild();

		// construct namespaces
		Map<String, String> tmpNamespaces = new HashMap<String, String>();
		xmlCursor.getAllNamespaces(tmpNamespaces);
		for (String key : tmpNamespaces.keySet()) {
			namespaces.put(tmpNamespaces.get(key), key);
			if (logSystem.isDebugEnabled()) {
				logSystem.debug(tmpNamespaces.get(key) + ", " + key);
			}
		}
		xmlCursor.dispose();
	}
	
	/**
	 * Get schema namespaces
	 * @return map<schema namespaces, prefix>
	 */
	public Map<String, String> getNamespaces() {
		return namespaces;
	}
	
	/**
	 * Execute a XPath expression against the an XML object 
	 * 
	 * @param query
	 *            to execute
	 * @param xmlObject to select path on
	 * @param generateNamespaceDecleration decide to let XPathUtil generate and add namespace decleration 
	 * @return xmlcursor to iterate on, with hasNextSelectiona and
	 *         toNextSelection
	 */
	public XmlCursor selectXPath(String query, XmlObject xmlObject, Boolean generateNamespaceDecleration) {
		if(generateNamespaceDecleration==null) {
			generateNamespaceDecleration = false;
		}
		String queryText = null;
		if (generateNamespaceDecleration) {
			queryText = generateNamespaceDeclaration() + "$this/" + query;
		} else {
			queryText = "$this/" + query;
		}
		
		if (logSystem.isDebugEnabled()) {
			logSystem.debug("QueryText: " + queryText);
		}
		XmlCursor itemCursor = xmlObject.newCursor();
		itemCursor.selectPath(queryText);
		return itemCursor;
	}
	
	/**
	 * Generate namespace declaration  
	 * @return generated namespace declaration  
	 */
	public String generateNamespaceDeclaration() {
		// declare namespace po = 'http://openuri.org/easypo';
		StringBuffer result = new StringBuffer();
		for (String key : namespaces.keySet()) {
			result.append("declare namespace ");
			result.append(namespaces.get(key));
			result.append(" = '");
			result.append(key);
			result.append("'; ");
		}
		return result.toString();
	}
	
	/**
	 * Constructs a XPath query to the supplied node.
	 * 
	 * @param node to retrieve XPath from 
	 * @return XPath
	 */
	public String getXPath(Node node) {
		if (null == node)
			return "";

		Node parentNode = null;
		Stack<Node> hierarchy = new Stack<Node>();
		StringBuffer result = new StringBuffer();

		// push element on stack
		hierarchy.push(node);

		parentNode = node.getParentNode();
		while (null != parentNode && parentNode.getNodeType() != Node.DOCUMENT_NODE) {
			// push on stack
			hierarchy.push(parentNode);

			// get parent of parent
			parentNode = parentNode.getParentNode();
		}

		// construct xpath
		Object obj = null;
		while (!hierarchy.isEmpty() && null != (obj = hierarchy.pop())) {
			Node currentNode = (Node) obj;

			// only consider elements
			if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element) currentNode;

				// element - append slash and element name
				result.append("/");
				result.append(this.namespaces.get(currentNode.getNamespaceURI()));
				result.append(":");
				result.append(currentNode.getLocalName());
				
					// no known attribute we could use - get sibling index
					int prev_siblings = 1;
					Node prevSiblingNode = currentNode.getPreviousSibling();
					while (null != prevSiblingNode) {
						if (prevSiblingNode.getNodeType() == currentNode.getNodeType()) {
							if (prevSiblingNode.getLocalName().equalsIgnoreCase(
									currentNode.getLocalName())) {
								prev_siblings++;
							}
						}
						prevSiblingNode = prevSiblingNode.getPreviousSibling();
					}
					result.append("[" + prev_siblings + "]");
				}
			}
		return result.toString();
	}
}
