package io.corexchain.verifyservice;

import io.corexchain.verify4j.chainpoint.MerkleTree;
import io.corexchain.verifyservice.issuer.service.AESUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@SpringBootTest
class VerifyServiceApplicationTests {

	@Test
	void contextLoads() throws NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException {
		// encryption
		String encodedKey = "eUpGaWnufdjrI1DrbYWjMItlM/k28PD00NeSRrtw8ps=";
		String input = MerkleTree.calcHashFromStr("xx11223344", "SHA-256");
		input += "#" + "Бат1" + "#" + "Болд1" + "#" + "oid2";
		System.out.println(input);
		byte[] keyBytes = Base64.getDecoder().decode(encodedKey.getBytes(StandardCharsets.UTF_8));
		SecretKey key = new SecretKeySpec(keyBytes, "AES");
//        IvParameterSpec ivParameterSpec = AESUtil.generateIv();
		String algorithm = "AES/CBC/PKCS5Padding";
		IvParameterSpec ivParameterSpec = AESUtil.generateIv();
		byte[] cipherText = AESUtil.encrypt(algorithm, input, key, ivParameterSpec);
		byte[] cipherWithIv = new byte[cipherText.length + ivParameterSpec.getIV().length];
		System.arraycopy(ivParameterSpec.getIV(), 0, cipherWithIv, 0, ivParameterSpec.getIV().length);
		System.arraycopy(cipherText, 0, cipherWithIv, ivParameterSpec.getIV().length, cipherText.length);
		System.out.println((Base64.getEncoder()
				.encodeToString(cipherWithIv)));
		System.out.println(AESUtil.decrypt(algorithm, cipherText, key, ivParameterSpec));
	}

}
