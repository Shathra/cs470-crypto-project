package Chat;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import javax.xml.bind.DatatypeConverter;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class Utils {
	public enum HashType {
		
	    MD5("MD5"), SHA1("SHA-1"), SHA256("SHA-256");

	    private final String value;
	    HashType(String value) { this.value = value; }
	    public String getValue() { return value; }
	}
	
	public static void main(String[] args) {
		/*
	    Scanner sn = new Scanner(System.in);
	    System.out.print("Please enter data for which SHA256 is required:");
	    String data = sn.nextLine();		
	    System.out.println("The SHA256 (hexadecimal encoded) hash is:"+hash(data,HashType.MD5));
		
        String key = "Bar12345Bar12345"; // 128 bit key
        String initVector = "RandomInitVector"; // 16 bytes IV
		Scanner sn = new Scanner(System.in);
	    System.out.print("Please enter data to encrypte:");
	    String data = sn.nextLine();
	    String encrypted = encrypt_aes(key,initVector,data);
	    System.out.println("encrypted string: "+ encrypted);
	    String decrypted = decrypt_aes(key,initVector,encrypted);
	    System.out.println("decrypted string: "+ decrypted);
        System.out.println(decrypt_aes(key, initVector,
                encrypt_aes(key, initVector, "Hello World")));
		*/
	}
	
	public static String hash(String str, HashType type) {
		
	    String result = null;
	    try {
	        MessageDigest digest = MessageDigest.getInstance(type.getValue()); //eg. type="SHA-256"
	        byte[] hash = digest.digest(str.getBytes("UTF-8"));
	        return DatatypeConverter.printHexBinary(hash); // make it printable
	    }catch(Exception ex) {
	        ex.printStackTrace();
	    }
	    return result;
	}
	
	public static String hash( String str) {
		
		return hash( str, HashType.SHA1);
	}

	public static String encrypt_aes(String key, String initVector, String input){
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted_bytes = cipher.doFinal(input.getBytes());
            String encrypted = DatatypeConverter.printBase64Binary(encrypted_bytes);
            
            return encrypted;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
	}
    public static String decrypt_aes(String key, String initVector, String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(DatatypeConverter.parseBase64Binary(encrypted));

            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
    
    public static String keyToString( Key key) {
    	
    	return String.valueOf( key.getEncoded());
    }
    
    public static String getKey( String keyStoreFileName, String keyStorePassword, String alias, String password) {
    	
    	File keyStoreFile = new File( keyStoreFileName);
    	KeyStore keyStore = null;
    	System.out.println("Initializing key store: " + keyStoreFile.getAbsolutePath());
    	URI keyStoreUri = keyStoreFile.toURI();
    	InputStream is = null;
    	try {
    		
    		URL keyStoreUrl = keyStoreUri.toURL();
        	keyStore = KeyStore.getInstance( "JCEKS");
    		is = keyStoreUrl.openStream();
    		keyStore.load(is, null == password ? null : password.toCharArray());
    		System.out.println("Loaded key store");
    	} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
    		if (null != is) {
    			try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	}
    	
    	try {
			if( !keyStore.isKeyEntry( alias)) {
				
				System.out.println( "no key alias found");
				return null;
			}
			
			Key key = keyStore.getKey(alias, password.toCharArray());
			return String.valueOf( key.getEncoded());
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return null;
    }
}
