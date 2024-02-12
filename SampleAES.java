/*******************************************************************************
 * Copyright (c) 2018, 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;


public class SampleAES {

    private static final long serialVersionUID = 1L;

   public static void main(String[] args) {
       System.out.println("Calling AES encryption code!");
       SampleAES sampleAES = new SampleAES();
       try {
	   sampleAES.doWork(); 
       }
       catch (Exception e) {
	   System.out.println("main()  exception." + e.getMessage() );
            System.out.println(dumpStack(e));            
       }

   }
    
   public void doWork() throws Exception {

       String defaultResponse = "Encryption successful";

        //AESKey aesKey = new AESKey();
        try {
            byte[] key = aesKeyGenerator(); 
            KeySpec spec = new SecretKeySpec(key, "AES");
            
            SecretKeyFactory factory = SecretKeyFactory.getInstance("AES");
            SecretKey secretKey = factory.generateSecret(spec);
            
            Cipher encryptor = Cipher.getInstance("AES");
            encryptor.init(Cipher.ENCRYPT_MODE, secretKey);
            
            byte[] encrypted = encryptor.doFinal("あいうえお".getBytes());
            
            System.out.println("Encryption successful");

        }
        catch (Exception e) {
	    System.out.println("AES logic threw exception." + e.getMessage() );
            System.out.println(dumpStack(e));            
        }
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
}

