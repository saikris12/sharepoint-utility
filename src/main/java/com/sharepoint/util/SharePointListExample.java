package com.sharepoint.util;

import java.io.File;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.microsoft.sharepoint.webservices.GetListItems;
import com.microsoft.sharepoint.webservices.GetListItemsResponse;
import com.microsoft.sharepoint.webservices.ListsSoap;

public class SharePointListExample extends SharePointBaseUtil {

	private String query = null;
	private String queryOptions = null;
	private static Properties properties = new Properties();
	private static final Log logger = LogFactory.getLog(SharePointListExample.class);
	
	public  Properties getProperties(){
		return properties;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		logger.debug("main...");
		SharePointListExample example = new SharePointListExample();
		try {
			example.initialize();
			ListsSoap ls = example.getListsSoap();
			example.querySharePointFolder(ls);
		} catch (Exception ex) {
			logger.error("Error caught in main: ",ex);
		}
	}



	public void querySharePointFolder(ListsSoap ls) throws Exception {	
		
		GetListItems.ViewFields viewFields = null;
		GetListItems.QueryOptions msQueryOptions = new GetListItems.QueryOptions();
		GetListItems.Query msQuery = new GetListItems.Query();
		msQuery.getContent().add(createSharePointCAMLNode(query));
		msQueryOptions.getContent().add(createSharePointCAMLNode(this.queryOptions));
		GetListItemsResponse.GetListItemsResult result = ls.getListItems(
				properties.getProperty("folder"), "", msQuery, viewFields, "",
				msQueryOptions, "");
		writeResult(result.getContent().get(0), System.out);
		
		Element element = (Element)result.getContent().get(0);
		NodeList nl = element.getElementsByTagName("z:row");
		for(int i = 0; i < nl.getLength(); i++){
			Node node = nl.item(i);
			logger.debug("ID: " + node.getAttributes().getNamedItem("ows_ID").getNodeValue());
			logger.debug("FileRef: " + node.getAttributes().getNamedItem("ows_FileRef").getNodeValue());
		}
		
	}
	
	protected void initialize() throws Exception {
		properties.load(getClass().getResourceAsStream("/SharePointListExample.properties"));
		//super.initialize();		
		this.query = new String(readAll(new File(this.getClass().getResource("/Query.xml").toURI())));
		this.queryOptions = new String(readAll(new File(this.getClass().getResource("/QueryOptions.xml").toURI())));
	}

}
