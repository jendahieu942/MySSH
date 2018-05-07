package encrypt;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Encipher {
	private String key;
	private SecretKey sekey;

	public Encipher(String key) {
		this.key = key;
		try {
			SecretKeySpec sekeysp = new SecretKeySpec(key.getBytes(), "DES");
			SecretKeyFactory sekeyfa = SecretKeyFactory.getInstance("DES");
			sekey = sekeyfa.generateSecret(sekeysp);
		} catch (Exception e) {
			System.out.println("Error: Constructor encipher");
		}
	}

	public String encrypted(String plaintext) {
		try {
			Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, sekey);
			byte[] encrypted = cipher.doFinal(plaintext.getBytes());
			String encry = Base64.getEncoder().encodeToString(encrypted);
			return encry;
		} catch (Exception e) {
			System.out.println("Error: Encrypted");
			return null;
		}

	}

	public String decrypted(String ciphertext) {
		try {
			Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, sekey);
			byte[] deco = Base64.getDecoder().decode(ciphertext);
			byte[] decrypted = cipher.doFinal(deco);
			String decry = new String(decrypted);
			return decry;
		} catch (Exception e) {
			System.out.println("Error: Decrypted");
			return null;
		}
	}

}
