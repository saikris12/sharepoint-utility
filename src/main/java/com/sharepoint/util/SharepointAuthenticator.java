package com.sharepoint.util;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Properties;

public class SharepointAuthenticator extends Authenticator{
    
	private String userName;
	private String password;
	
	public SharepointAuthenticator(String userName, String password){
		this.userName = userName;
		this.password = password;
	}
    
    public PasswordAuthentication getPasswordAuthentication () {
	
	    return new PasswordAuthentication (        	
        	userName,password.toCharArray());
	}


}
