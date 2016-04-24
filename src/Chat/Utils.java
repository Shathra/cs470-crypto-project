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
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.DatatypeConverter;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;

import javax.crypto.Mac;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;


public class Utils {
	public enum HashType {
		
	    MD5("MD5"), SHA1("SHA-1"), SHA256("SHA-256");

	    private final String value;
	    HashType(String value) { this.value = value; }
	    public String getValue() { return value; }
	}
	
	//hashes the given message with given hash algorithm
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
	
	//hashes the given message with SHA-1
	public static String hash( String str) {
		
		return hash( str, HashType.SHA1);
	}
	
	//encrypts given message with given key and given IV
	public static String encrypt_aes(String key, String initVector, String message){

        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted_bytes = cipher.doFinal(message.getBytes());
            String encrypted = DatatypeConverter.printBase64Binary(encrypted_bytes);
            
            return encrypted;
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
	}
	
	//decrypts given message with given key and given IV
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
    
    //converts a Key variable into a String variable
    public static String keyToString( Key key) {
    	
    	return Base64.getEncoder().encodeToString( key.getEncoded());
    }
    
    //converts a String variable into a Key variable under given algorithm
    public static Key stringToKey( String str, String algorithm) {
    	
    	byte[] decodedKey = Base64.getDecoder().decode(str);
    	Key key = new SecretKeySpec(decodedKey, 0, decodedKey.length, algorithm);
    	return key;
    }  
    
    //returns the key of the given alias from the given key-store
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
    		keyStore.load(is, keyStorePassword.toCharArray());
    	} catch (KeyStoreException e) {
			System.out.println("Error on getting key");
			e.printStackTrace();
			System.exit(-1);
		} catch (MalformedURLException e) {
			System.out.println("Error on getting key");
			e.printStackTrace();
			System.exit(-1);
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Error on getting key");
			e.printStackTrace();
			System.exit(-1);
		} catch (CertificateException e) {
			System.out.println("Error on getting key");
			e.printStackTrace();
			System.exit(-1);
		} catch (IOException e) {
			System.out.println("Error on getting key");
			e.printStackTrace();
			System.exit(-1);
		} finally {
    		if (null != is) {
    			try {
					is.close();
				} catch (IOException e) {
					System.out.println("Error on getting key");
					e.printStackTrace();
					System.exit(-1);
				}
    		}
    	}
    	
    	try {
			if( !keyStore.isKeyEntry( alias)) {
				
				System.out.println( "no key alias found");
				return null;
			}
			
			Key key = keyStore.getKey(alias, password.toCharArray());
			return Utils.keyToString(key);
		} catch (KeyStoreException e) {
			System.out.println("Error on getting key");
			e.printStackTrace();
			System.exit(-1);
		} catch (UnrecoverableKeyException e) {
			System.out.println("Error on getting key");
			e.printStackTrace();
			System.exit(-1);
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Error on getting key");
			e.printStackTrace();
			System.exit(-1);
		}
    	
    	return null;
    }
    
    //returns the current time-stamp
    public static String getCurrentTimestamp() {
    	
    	SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    	String time  = dateFormat.format(new Date());
    	
    	return time;
    }
    
    //compares the given time-stamp with current time-stamp and checks if the difference is less then one hour
    public static boolean checkTimestamp( String timestamp) {
    	
    	SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    	Date time = null;
		try {
			time = dateFormat.parse( timestamp);
		} catch (ParseException e) {
			System.out.println("Timestamp check error");
			e.printStackTrace();
			System.exit(-1);
		}
    	Date current = new Date();
    	
    	long diffInMillies = current.getTime() - time.getTime();
    	TimeUnit timeUnit = TimeUnit.MINUTES;
        long diff = timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    	
    	if( diff < 60)
    		return true;
    	
    	return false;
    }
    
    //generates a MAC for given message and key
    public static String generateMAC(String message, String key_str){

    	
		try {
			 
			Key key = stringToKey(key_str, "HmacSHA256");
   
			// create a MAC and initialize with the above key
			Mac mac = Mac.getInstance(key.getAlgorithm());
			mac.init(key);
		 
			// get the string as UTF-8 bytes
			byte[] b = message.getBytes("UTF-8");
			 
			// create a digest from the byte array
			byte[] digest = mac.doFinal(b);
			
			//convert from byte array into string
			String s = new String(digest);
			
			return Utils.hash( s);
	 
		}
		catch (NoSuchAlgorithmException e) {
			System.out.println("No Such Algorithm:" + e.getMessage());
			return null;
		}
		catch (UnsupportedEncodingException e) {
			System.out.println("Unsupported Encoding:" + e.getMessage());
			return null;
		}
		catch (InvalidKeyException e) {
			System.out.println("Invalid Key:" + e.getMessage());
			return null;
		}

    }
}
