package com.sharepoint.util;

import java.util.Properties;

import javax.xml.ws.Holder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.microsoft.sharepoint.webservices.*;

public class SharePointUploadDocumentUtil extends SharePointBaseUtil {

	private static Properties properties = new Properties();
	private static final Log logger = LogFactory.getLog(SharePointUploadDocumentUtil.class);
	/**
	 * @param args
	 */
	public Holder<CopyResultCollection> uploadFileToSharepoint(String wsdl, String endpoint, String username, String password, String copyWsdl, String copyEndpoint, String copyLocation, 
			String sourceFile) {
		logger.debug("main...");	
		Holder<CopyResultCollection> resultCollection = null;
		try {		
			SharePointUploadDocumentUtil example = new SharePointUploadDocumentUtil();
			example.initialize(username,password);
			CopySoap p = example.getCopySoap(username, password, copyWsdl, copyEndpoint);
			resultCollection = example.uploadDocument(p, wsdl, endpoint, copyWsdl, copyLocation, sourceFile);
		} catch (Exception ex) {
			System.out.println("Error caught in main: "+ex.getMessage());
		}
		return resultCollection;
	}

	public Properties getProperties() {
		return properties;
	}

	protected void initialize(String userName, String password) throws Exception {
		logger.info("initialize()...");
		//properties.load(getClass().getResourceAsStream("/SharePointUploadDocumentExample.properties"));
		super.initialize(userName,password);		
	}
}
