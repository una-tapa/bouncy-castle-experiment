// tag::copyright[]
/*******************************************************************************
 * Copyright (c) 2018, 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
// end::copyright[]
package io.openliberty.guides.ui;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.NoSuchAlgorithmException;

import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.authentication.mechanism.http.FormAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
// This is only needed to get the string "BC"
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Provider;
import java.security.Security;

@WebServlet(urlPatterns = "/home")
// tag::AuthenticationMechanism[]
@FormAuthenticationMechanismDefinition(
    loginToContinue = @LoginToContinue(errorPage = "/error.html",
                                       loginPage = "/welcome.html"))
   //  @ServletSecurity(value = @HttpConstraint(rolesAllowed = { "user", "admin" }, transportGuarantee = ServletSecurity.TransportGuarantee.CONFIDENTIAL))
  @ServletSecurity(value = @HttpConstraint(rolesAllowed = { "user", "admin" }))
// tag::HomeServlet[]
public class HomeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Inject
    private SecurityContext securityContext;

    // tag::javaDoc1[]
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    // end::javaDoc1[]
    // tag::doGet[]
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

		PrintWriter out = response.getWriter();

		
		out.println("<HTML><BODY>");

        String msg = checkPolicy ();
        out.println("<P>" + msg + "</P>"); 

 
        Provider[] providers = Security.getProviders();
        out.println("<P>List of available security providers:</P>");

        for (Provider provider : providers) {
            out.println("<P> Provider: " + provider.getName() + ", Version: " + provider.getVersion() + "</P>");
            // List properties of each provider
            //provider.forEach((key, value) -> out.println("\t<P>" + key + ": " + value + "</P>"));
        }
        out.println("Loading the BouncyCastleProvider if not loaded.");
        //Security.addProvider(new BouncyCastleProvider());
        msg = LoadBouncyCastleConditionally ();
        out.println("<P>" + msg + "</P>"); 

        for (Provider provider : providers) {
            out.println("<P> Provider: " + provider.getName() + ", Version: " + provider.getVersion() + "</P>");
            // List properties of each provider
            //provider.forEach((key, value) -> out.println("\t<P>" + key + ": " + value + "</P>"));
        }
		
		String defaultResponse = "Hello, this is a sample program performing AES encryption inside Liberty. Encryption was successful." ;

        //AESKey aesKey = new AESKey();
        try {
            byte[] key = aesKeyGenerator(); 
            KeySpec spec = new SecretKeySpec(key, "AES");
            
            SecretKeyFactory factory = SecretKeyFactory.getInstance("AES");
            SecretKey secretKey = factory.generateSecret(spec);
            
            Cipher encryptor = Cipher.getInstance("AES");
            encryptor.init(Cipher.ENCRYPT_MODE, secretKey);
            
            byte[] encrypted = encryptor.doFinal("あいうえお".getBytes());
            
            out.println("<P>" + defaultResponse + "</P>");

        }
        catch (Exception e) {
            out.println("<P>" + "AES logic threw exception." + e.getMessage() + "</P>");
            out.println("<BR/>"); 
            out.println("<P>" + dumpStack(e) + "</P>");            
        }
		out.println("</BODY></HTML>");

    }
    // end::doGet[]

    static String dumpStack(Exception e) {
        StringBuffer sb = new StringBuffer("-- Error Stack ---");
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        e.printStackTrace(new java.io.PrintStream(baos));
        sb.append("\n");
        sb.append(baos);
        sb.append("--------END Dump stack ---------");
        return sb.toString();
}



    // tag::javaDoc2[]
    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    // end::javaDoc2[]
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        doGet(request, response);
    }

    //This is a sample Key
    public byte[] aesKeyGenerator() {

        byte[] keyBytes = null; 

        try {
            // Create a KeyGenerator instance for AES
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");

            // Initialize the KeyGenerator to a specified key size, e.g., 128, 192, or 256 bits
            keyGen.init(256); // You can choose 128, 192, or 256 based on your requirement

            // Generate the secret key
            SecretKey secretKey = keyGen.generateKey();

            // Get the binary encoding of the generated key (byte array)
            keyBytes = secretKey.getEncoded();

            // Print the key as a sequence of bytes for demonstration purposes
            //System.out.println("Generated AES Key (byte array format): ");
            //for (byte b : keyBytes) {
            //    System.out.printf("%02X ", b);
            //}

        } catch (NoSuchAlgorithmException e) {
            System.err.println("AES Key generation failed: " + e.getMessage());
        }
        catch (Exception ee) {
            System.err.println("AES Key generation failed 2: " + ee.getMessage());
        }
        return keyBytes; 
    }

   
    public String LoadBouncyCastleConditionally () {
        String msg = null; 
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            // If not registered, add the Bouncy Castle provider
            Security.addProvider(new BouncyCastleProvider());
            msg = "Bouncy Castle provider has been added.";
        } else {
            msg = "Bouncy Castle provider is already registered.";
        }
        return msg; 
    }

    public String checkPolicy() {
        String msg = null; 
        try {
            // Check the maximum allowed key length for AES
            int maxKeyLen = Cipher.getMaxAllowedKeyLength("AES");
            if (maxKeyLen > 128) {
                msg = "Unrestricted policy is in place. Max AES key length: " + maxKeyLen;
            } else {
                msg = "Restricted policy is in place. Max AES key length: " + maxKeyLen;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msg; 
    }

}
// end::HomeServlet[]
