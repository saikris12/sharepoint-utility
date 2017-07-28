package com.sharepoint.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.Authenticator;
import java.net.URL;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.microsoft.sharepoint.webservices.Copy;
import com.microsoft.sharepoint.webservices.CopyErrorCode;
import com.microsoft.sharepoint.webservices.CopyResult;
import com.microsoft.sharepoint.webservices.CopyResultCollection;
import com.microsoft.sharepoint.webservices.CopySoap;
import com.microsoft.sharepoint.webservices.DestinationUrlCollection;
import com.microsoft.sharepoint.webservices.FieldInformation;
import com.microsoft.sharepoint.webservices.FieldInformationCollection;
import com.microsoft.sharepoint.webservices.FieldType;
import com.microsoft.sharepoint.webservices.Lists;
import com.microsoft.sharepoint.webservices.ListsSoap;
public abstract class SharePointBaseUtil {

	private static final Log logger = LogFactory.getLog(SharePointBaseUtil.class);

	protected abstract Properties getProperties();

	protected ListsSoap getListsSoap()
			throws Exception {
		logger.info("Creating a ListsSoap instance...");
		Lists service = new Lists(new URL(getProperties().getProperty("wsdl")),
				new QName("http://schemas.microsoft.com/sharepoint/soap/", "Lists"));
		ListsSoap port = service.getListsSoap();
		BindingProvider bp = (BindingProvider) port;
		bp.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, getProperties().getProperty("username"));
		bp.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, getProperties().getProperty("password"));
		bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getProperties().getProperty("endpoint"));
		return port;
	}

	protected CopySoap getCopySoap(String username, String password, String copyWsdl, String copyEndpoint) throws Exception {
		logger.info("Creating a CopySoap instance...");
		Copy service = new Copy(new URL(copyWsdl),
				new QName("http://schemas.microsoft.com/sharepoint/soap/", "Copy"));
		CopySoap copySoap = service.getCopySoap();
		BindingProvider bp = (BindingProvider) copySoap;
		bp.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, username);
		bp.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);
		bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, copyEndpoint);
		return copySoap;
	}

	protected static Node createSharePointCAMLNode(String theXML) throws Exception {
		logger.debug("createSharePointCAMLNode()...");
		logger.debug("CAML is: \n" + theXML);
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setValidating(false);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(new InputSource(new StringReader(theXML)));
		Node node = document.getDocumentElement();
		return node;
	}

	protected static void writeResult(Object result, OutputStream stream)
			throws Exception {

		if (result == null) {
			logger.warn("result was null...");
			return;
		}
		if (!(result instanceof Element)) {
			logger.warn("Not sure what to do with this response.  It should be Element, but was: " + result.getClass().getName());
			return;
		}

		Element e = (Element) result;
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		transformer.transform(new DOMSource(e.getOwnerDocument()),
				new StreamResult(new OutputStreamWriter(stream, "UTF-8")));
	}

	protected void initialize(String userName, String password) throws Exception {
		logger.debug("initialize()...");
		java.net.CookieManager cm = new java.net.CookieManager();
		java.net.CookieHandler.setDefault(cm);
		Authenticator.setDefault(new SharepointAuthenticator(userName,password));
	}

	protected static byte[] readAll(File file) throws IOException {
		logger.debug("readAll()..." + file.getAbsolutePath());
		ByteArrayOutputStream ous = null;
		InputStream ios = null;
		try {
			byte[] buffer = new byte[4096];
			ous = new ByteArrayOutputStream();
			ios = new FileInputStream(file);
			int read = 0;
			while ((read = ios.read(buffer)) != -1)
				ous.write(buffer, 0, read);
		} finally {
			try {
				if (ous != null)
					ous.close();
			} finally {
				if (ios != null)
					ios.close();
			}
		}
		return ous.toByteArray();
	}

	protected  Holder<CopyResultCollection> uploadDocument(CopySoap port, String wsdl, String endpoint, String copyWsdl, String copyLocation, 
			String sourceFile)	throws Exception {

		File f = new File(sourceFile);
		logger.debug("Uploading: " + f.getName());

		String url = copyLocation + f.getName();
		DestinationUrlCollection destinationUrlCollection = new DestinationUrlCollection();
		destinationUrlCollection.getString().add(url);
		/*if(getProperties().getProperty("copy.location2") != null){
			 url = getProperties().getProperty("copy.location2") + f.getName();
			 destinationUrlCollection.getString().add(url);
		}*/
		

		FieldInformation titleFieldInformation = new FieldInformation();
		titleFieldInformation.setDisplayName("Title");
		titleFieldInformation.setType(FieldType.TEXT);
		titleFieldInformation.setValue(f.getName());

		FieldInformationCollection fields = new FieldInformationCollection();
		fields.getFieldInformation().add(titleFieldInformation);

		CopyResultCollection results = new CopyResultCollection();
		Holder<CopyResultCollection> resultHolder = new Holder<CopyResultCollection>(results);
		Holder<Long> longHolder = new Holder<Long>(new Long(-1));
		
		//make the call to upload
		port.copyIntoItems(sourceFile, destinationUrlCollection, fields, readAll(f), longHolder,resultHolder);
		
		//does not seem to change based on different CopyResults
		logger.debug("Long holder: " + longHolder.value);
		
		//do something meaningful here
		for (CopyResult copyResult : resultHolder.value.getCopyResult()) {				
			logger.debug("Destination: " + copyResult.getDestinationUrl());
			logger.debug("Error Message: " + copyResult.getErrorMessage());
			logger.debug("Error Code: " + copyResult.getErrorCode());
			/*if(copyResult.getErrorCode() != CopyErrorCode.SUCCESS)
				throw new Exception("Upload failed for: " + copyResult.getDestinationUrl() + " Message: " 
						+ copyResult.getErrorMessage() + " Code: " +   copyResult.getErrorCode() );*/
		}
		
		return resultHolder;

	}
}
