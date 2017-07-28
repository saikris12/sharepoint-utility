package com.sharepoint.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.microsoft.sharepoint.webservices.CopySoap;
import com.microsoft.sharepoint.webservices.GetListItems;
import com.microsoft.sharepoint.webservices.GetListItemsResponse;
import com.microsoft.sharepoint.webservices.ListsSoap;
import com.microsoft.sharepoint.webservices.UpdateListItems.Updates;
import com.microsoft.sharepoint.webservices.UpdateListItemsResponse.UpdateListItemsResult;


public class SharePointDeleteListItemUtil extends SharePointBaseUtil {

	private String delete = null;
	private String deleteListItemQuery = null;
	private String queryOptions = null;
	private static final Log logger = LogFactory.getLog(SharePointUploadDocumentUtil.class);

	private static Properties properties = new Properties();

	public Properties getProperties() {
		return properties;
	}

	/**
	 * @param args
	 */
	/*public static void main(String[] args) {
		logger.debug("main...");
		SharePointDeleteListItemUtil example = new SharePointDeleteListItemUtil();
		try {
			example.initialize();
			CopySoap cp = example.getCopySoap();
			example.uploadDocument(cp, properties.getProperty("copy.sourceFile"));
			ListsSoap ls = example.getListsSoap();
			example.executeQueryAndDelete(ls);
		} catch (Exception ex) {
			logger.error("Error caught in main: ", ex);
		}

	}*/

	public void executeQueryAndDelete(ListsSoap ls) throws Exception {

		Date today = Calendar.getInstance().getTime();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String formattedDate = simpleDateFormat.format(today);
		String queryFormatted = String.format(deleteListItemQuery,formattedDate);		
		
		GetListItems.QueryOptions msQueryOptions = new GetListItems.QueryOptions();
		GetListItems.Query msQuery = new GetListItems.Query();
		msQuery.getContent().add(createSharePointCAMLNode(queryFormatted));
		msQueryOptions.getContent().add(createSharePointCAMLNode(this.queryOptions));
		GetListItemsResponse.GetListItemsResult result = ls.getListItems(
				properties.getProperty("folder"), "", msQuery, null, "",
				msQueryOptions, "");
		writeResult(result.getContent().get(0), System.out);

		Element element = (Element) result.getContent().get(0);
		NodeList nl = element.getElementsByTagName("z:row");
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			String id = node.getAttributes().getNamedItem("ows_ID").getNodeValue();
			String fileRefRelativePath = node.getAttributes().getNamedItem("ows_FileRef").getNodeValue();
			logger.debug("id: " + id);
			logger.debug("fileRefRelativePath: " + fileRefRelativePath);
			String fileRef = properties.getProperty("delete.FileRef.base") + fileRefRelativePath.split("#")[1];
			logger.debug("fileRef: " + fileRef);
			deleteListItem(ls, properties.getProperty("folder"), id, fileRef);
		}

	}

	public void deleteListItem(ListsSoap ls, String listName, String listId, String fileRef) throws Exception {
		String deleteFormatted = String.format(delete, listId, fileRef);		
		Updates u = new Updates();
		u.getContent().add(createSharePointCAMLNode(deleteFormatted));
		UpdateListItemsResult ret = ls.updateListItems(listName, u);
		
		writeResult(ret.getContent().get(0), System.out);
	}

	
	public void initialize() throws Exception {
		logger.info("initialize()...");
		properties.load(getClass().getResourceAsStream("/SharePointDeleteListItemExample.properties"));
		//super.initialize();
		this.delete = new String(readAll(new File(this.getClass().getResource("/Delete.xml").toURI())));
		this.deleteListItemQuery = new String(readAll(new File(this.getClass().getResource("/DeleteListItemQuery.xml").toURI())));
		this.queryOptions = new String(readAll(new File(this.getClass().getResource("/QueryOptions.xml").toURI())));
	}

}
