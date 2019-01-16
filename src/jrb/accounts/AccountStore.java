/*
 * Copyright 2018, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

/**
 */
class AccountStore {
    private static final String FILEMAGIC0 = "ACCTS.00";

    private static final String FILEMAGIC = "ACCTS.01";

    private static final String PASSWORD_TYPE = "AES";

    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

    private static final int PASSWORD_COUNT = 0x10000;

    private static final int KEY_BITS = 128;

    private static final SecureRandom randomSource = new SecureRandom();

    private TreeMap<String, Account> myAccounts;

    private byte[] passwordSalt = new byte[8];
    private byte[] ivBlock = new byte[128 / 8];		// 1 128-bit AES block
    private PBEKeySpec fileKey;
    private Cipher fileCipher;

    public AccountStore() {
	myAccounts = new TreeMap<String, Account>();
	fileCipher = null;
    }

    public AccountStore(InputStream rawInput, char[] password)
	    throws GeneralSecurityException, IOException {
	this();
	readAccounts(rawInput, password);
    }

    public void addAccount(Account newAccount) {
	assert !myAccounts.containsKey(newAccount.getDescription());
	myAccounts.put(newAccount.getDescription(), newAccount);
    }

    public void updateAccount(Account account,
			      String description, String url,
			      String username, String password) {
	myAccounts.remove(account.getDescription());
	account.update(description, url, username, password);
	addAccount(account);
    }

    public void createAccount(String description, String url,
			      String username, String password) {
	addAccount(new Account(description, url, username, password));
    }

    public int size() {
	return myAccounts.size();
    }

    public Iterable<Account> allAccounts() {
	return myAccounts.values();
    }

    private boolean readMagic(InputStream in) throws IOException {
	byte[] magic = new byte[FILEMAGIC.length()];
        int nRead = in.read(magic);
	String magicString = new String(magic, 0, nRead);
	if (nRead < magic.length) {
	    throw new IOException("Magic truncated: " + magicString);
	}
	// XXX:  returning a boolean to indicate "current version"
	// vs. "old version" is, well..., not safe for the future.
	if (magicString.equals(FILEMAGIC)) {
	    return true;
	} else if (magicString.equals(FILEMAGIC0)) {
	    return false;
	} else {
	    throw new IOException("Unknown file format: " + magicString);
	}
    }

    private Key makeKey(char[] password)
	    throws GeneralSecurityException {
	fileKey = new PBEKeySpec(password, passwordSalt,
				 PASSWORD_COUNT, KEY_BITS);
	for (int i = 0; i < password.length; i++) {
	    password[i] = ' ';
	}
	SecretKeyFactory factory =
		SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
	SecretKey passwordSecret = factory.generateSecret(fileKey);
	return new SecretKeySpec(passwordSecret.getEncoded(),
				 PASSWORD_TYPE);
    }

    private DataInputStream makeInput(InputStream raw, char[] password)
	    throws GeneralSecurityException, IOException {
	raw.read(passwordSalt);
	raw.read(ivBlock);
	fileCipher.init(Cipher.DECRYPT_MODE,
			makeKey(password),
			new IvParameterSpec(ivBlock));
	InputStream encryptedStream =
	    new CipherInputStream(raw, fileCipher);
	return new DataInputStream(encryptedStream);
    }

    private DataOutputStream makeOutput(OutputStream raw, char[] password)
	    throws GeneralSecurityException, IOException {
	randomSource.nextBytes(passwordSalt);
	randomSource.nextBytes(ivBlock);
	raw.write(passwordSalt);
	raw.write(ivBlock);
	fileCipher.init(Cipher.ENCRYPT_MODE,
			makeKey(password),
			new IvParameterSpec(ivBlock));
	OutputStream encryptedStream =
	    new CipherOutputStream(raw, fileCipher);
	return new DataOutputStream(encryptedStream);
    }

    public void readAccounts(InputStream raw, char[] password)
	    throws GeneralSecurityException, IOException {
	DataInputStream in;
	if (readMagic(raw)) {
	    fileCipher = Cipher.getInstance(CIPHER_ALGORITHM);
	    in = makeInput(raw, password);
	} else {
	    in = new DataInputStream(raw);
	}
	int nElements = in.readInt();
	for (int i = 0; i < nElements; i++) {
	    addAccount(new Account(in));
	}
	in.close();
    }

    public void writeAccounts(OutputStream raw, char[] password)
	    throws GeneralSecurityException, IOException {
	fileCipher = Cipher.getInstance(CIPHER_ALGORITHM);
	raw.write(FILEMAGIC.getBytes());
	DataOutputStream out = makeOutput(raw, password);
	out.writeInt(myAccounts.size());
	for (Account acct : myAccounts.values()) {
	    acct.writeAccount(out);
	}
	out.close();
    }

    public void writeAccounts(OutputStream raw)
	    throws GeneralSecurityException, IOException {
	writeAccounts(raw, fileKey.getPassword());
    }
}
