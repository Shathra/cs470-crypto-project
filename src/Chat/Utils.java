package Chat;
import java.security.MessageDigest;
import java.util.Scanner;
import javax.xml.bind.DatatypeConverter;
import javax.crypto.Cipher;


public class Utils {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
	    Scanner sn = new Scanner(System.in);
	    System.out.print("Please enter data for which SHA256 is required:");
	    String data = sn.nextLine();		
	    System.out.println("The SHA256 (hexadecimal encoded) hash is:"+hash(data,"SHA-256"));
	    System.out.println("The SHA1 (hexadecimal encoded) hash is:"+hash(data,"SHA-1"));
	    System.out.println("The MD5 (hexadecimal encoded) hash is:"+hash(data,"MD5"));
	}
	//TODO: enum hashtype
	public static String hash(String str, String type){
	    String result = null;
	    try {
	        MessageDigest digest = MessageDigest.getInstance(type); //eg. type="SHA-256"
	        byte[] hash = digest.digest(str.getBytes("UTF-8"));
	        return DatatypeConverter.printHexBinary(hash); // make it printable
	    }catch(Exception ex) {
	        ex.printStackTrace();
	    }
	    return result;
	}

	public static String encryption_aes(String str,  key){
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		AlgorithmParameters params = cipher.getParameters();
		byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
		byte[] ciphertext = cipher.doFinal("Hello, World!".getBytes("UTF-8"));
	}
}
