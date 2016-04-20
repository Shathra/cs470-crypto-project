package Chat;
import java.security.MessageDigest;
import java.util.Scanner;
import javax.xml.bind.DatatypeConverter;
import java.security.Security;
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
	}
	
	//TODO: enum hashtype
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
}
