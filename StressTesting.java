package com.lydsec.keypasco.api;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ResourceBundle;

import javax.xml.ws.BindingProvider;

public class StressTesting {
	
	public ApiCustomerServiceService30 askService = new ApiCustomerServiceService30();
	public ApiCustomerService30 port = askService.getApiCustomerServicePort30();
	public BindingProvider prov = (BindingProvider)port;

	public static void main(String[] args) throws Exception {
		StressTesting service = new StressTesting();
		
		// 登入方式 - 1:Certificate , 2:Basic Auth
		service.loginAuthenticate(2);
		// 變更 SOAP Endpoint
		service.setEndpoint("http://localhost/api");
		
		String customerId = "tstest";
		String userId = "TestUser";
		String ip = "192.168.1.1";
		String sessionId = "" + System.currentTimeMillis();
		
		long startTime, endTime = 0, calTime = 0;
		int countErr = 0;
		int loop = 100;
		
		for(int i=0;i<loop;i++){
			startTime = System.currentTimeMillis();
			RegisterResponse registerResponse = null;
			try{
				registerResponse = service.register(userId, sessionId);
				endTime = System.currentTimeMillis() - startTime;
				calTime = calTime + endTime;
			}catch (Exception e) {
				countErr++;
			}
			System.out.println( "Register run-" + i + " ResultCode is : " + registerResponse.getResultCode() + "－" + endTime/1000.0 + "秒" );
		}
		System.out.println("平均花費時間：" + calTime/loop/1000.0 + " 秒，錯誤次數：" + countErr );
	}
	
	
	private void setEndpoint(String endpoint) {
		String serviceAddress;
		if(endpoint.isEmpty()){
			ResourceBundle rb = ResourceBundle.getBundle("application");
			serviceAddress = rb.getString("serviceAddress");
		}else{
			serviceAddress = endpoint;
		}
		prov.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, serviceAddress);
	}

	private void loginAuthenticate(int mode){
		
		switch (mode) {
			case 1:
				String keyStore = StressTesting.class.getResource("/keystore.jks").getPath();
				String keyStorePassword = "password";
				String trustStore = StressTesting.class.getResource("/keystore.jks").getPath();
				String trustStorePassword = "password";
				
				String keyStoreType = "jks";
				String protocols = "TLSv1.2";
				System.out.println( "keyStore Path : " + keyStore );
				
				// SSL/Certificates
				System.setProperty("javax.net.ssl.keyStore", keyStore);
				System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);
				System.setProperty("javax.net.ssl.keyStoreType", keyStoreType);
				System.setProperty("javax.net.ssl.trustStore", trustStore);
				System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
				System.setProperty("https.protocols", protocols);
				break;
			case 2:
				final String authName = "user";
				final String authPassword = "password";
				
				// Proxy
//				System.setProperty("http.proxyUser", authName);
//				System.setProperty("http.proxyPassword", authPassword);
				
				// SOCKSv5 proxy <Ref: http://stackabuse.com/how-to-configure-network-settings-in-java/>
//				String encoded = new String (Base64.encodeBase64(new String(authName+":"+authPassword).getBytes()));
//				System.setProperty("Proxy-Authorization", "Basic " + encoded);
//				System.setProperty("java.net.socks.username" , authName);
//				System.setProperty("java.net.socks.password" , authPassword);
				
				// Java-WS
//				prov.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, authName);
//				prov.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, authPassword);
				
				Authenticator myAuth = new Authenticator() 
				{
				    @Override
				    protected PasswordAuthentication getPasswordAuthentication()
				    {
				        return new PasswordAuthentication(authName, authPassword.toCharArray());
				    }
				};
				Authenticator.setDefault(myAuth);
				
				
				break;
		}
		
	}
	
	/**
	 * Register / 註冊使用者
	 */
	public RegisterResponse register(String userId, String sessionId) {
		RegisterRequest registerRequest = new RegisterRequest();
		registerRequest.setUserId(userId);
		registerRequest.setSessionId(sessionId);
		
		RegisterResponse registerResponse = port.register(registerRequest);
		return registerResponse;
	}

}
