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
 * A set of accounts that can be saved to or read from a file.
 *<p>
 * An <code>AccountStore</code> provides the ability to add, remove,
 * update, and access individual {@link Account} objects. The entire
 * set of accounts can be saved in a file, encrypted, and later read
 * back to reconstruct the saved account store.
 */
class AccountStore {
    static int FORMAT_V0 = 0;

    static int FORMAT_V1 = 1;

    static int FORMAT_V2 = 2;

    private static final String[] MAGIC_VERSIONS = {
	"ACCTS.00",	// FORMAT_V0
	"ACCTS.01",	// FORMAT_V1
	"ACCTS.02",	// FORMAT_V2
    };

    private static int FORMAT_CURRENT = MAGIC_VERSIONS.length - 1;

    private static final String FILEMAGIC
	    = MAGIC_VERSIONS[FORMAT_CURRENT];

    private static final String PASSWORD_TYPE = "AES";

    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

    private static final int PASSWORD_COUNT = 0x10000;

    private static final int KEY_BITS = 128;

    private static final SecureRandom randomSource = new SecureRandom();

    private byte[] passwordSalt = new byte[8];
    private byte[] ivBlock = new byte[128 / 8];		// 1 128-bit AES block
    private PBEKeySpec fileKey;
    private Cipher fileCipher;

    /**
     * The set of accounts kept in this account store.  The accounts are
     * ordered by their description, and duplicate descriptions aren't
     * allowed.
     */
    private TreeMap<String, Account> myAccounts;

    /**
     * Construct an empty account store.
     */
    public AccountStore() {
	initialize();
    }

    /**
     * Construct an account store by reading it from a stream.
     *
     * @param rawInput An input stream from which the account data will
     *     be read.
     * @param password A character array holding the password that will
     *     decrypt the account data.
     * @throws GeneralSecurityException Indicates a failure during
     *     decryption.
     * @throws IOException Indicates a failure reading data.
     */
    public AccountStore(InputStream rawInput, char[] password)
	    throws GeneralSecurityException, IOException {
	readAccounts(rawInput, password);
    }

    /**
     * Clear out the contents of this account store.  Calling this method
     * sets this account store to state created by the no-arg constructor.
     */
    private void initialize() {
	myAccounts = new TreeMap<String, Account>();
	fileCipher = null;
    }

    /**
     * Add a new account to the account store.
     *
     * @param newAccount The account to be added to the account store.
     */
    public void addAccount(Account newAccount) {
	assert !myAccounts.containsKey(newAccount.getDescription());
	myAccounts.put(newAccount.getDescription(), newAccount);
    }

    /**
     * Update an existing account in the account store.
     *
     * @param account The account to be updated in the account store.
     * @param description The new description for <code>account</code>.
     * @param url The new URL for <code>account</code>.
     * @param username The new user name for <code>account</code>.
     * @param password The new password for <code>account</code>.
     */
    public void updateAccount(Account account,
			      String description, String url,
			      String username, String password) {
	myAccounts.remove(account.getDescription());
	account.update(description, url, username, password);
	addAccount(account);
    }

    /**
     * Create a new account and add it to the account store.
     * A new account will be created from the supplied parameters, and
     * added to the account store as for {@link addAccount}.
     *
     * @param description The new description for <code>account</code>.
     * @param url The new URL for <code>account</code>.
     * @param username The new user name for <code>account</code>.
     * @param password The new password for <code>account</code>.
     *
     * @return The newly created account.
     */
    public Account createAccount(String description, String url,
				 String username, String password) {
	Account account = new Account(description, url, username, password);
	addAccount(account);
	return account;
    }

    /**
     * Delete an account from the account store.
     *
     * @param account The account to be deleted from the account store.
     */
    public void deleteAccount(Account account) {
	myAccounts.remove(account.getDescription());
    }

    /**
     * Return the number of accounts in the account store.
     *
     * @return The number of accounts.
     */
    public int size() {
	return myAccounts.size();
    }

    /**
     * Return an <code>Iterable</code> over every account in the account
     * store.  Iteration returns the accounts sorted by their
     * description.
     *
     * @return An iterable over all the accounts.
     */
    public Iterable<Account> allAccounts() {
	return myAccounts.values();
    }

    /**
     * Read the file magic from the start of a saved file, to determine
     * its save format.  If the stream starts with a recognized magic
     * string, return the associated file format version number.  If the
     * magic isn't recognized as a supported version, throw an
     * <code>IOException</code>.
     *
     * @param in The input stream from which to read file magic.
     * @return The file format version number.
     * @throws IOException indicates that the file magic couldn't be read,
     *     or wasn't recognized.
     */
    private int readMagic(InputStream in) throws IOException {
	byte[] magic = new byte[FILEMAGIC.length()];
	int nRead = in.read(magic);
	String magicString = new String(magic, 0, nRead);
	if (nRead < magic.length) {
	    throw new IOException("Magic truncated: " + magicString);
	}
	int version = 0;
	for (String testMagic : MAGIC_VERSIONS) {
	    if (magicString.equals(testMagic)) {
		return version;
	    }
	    version++;
	}
	throw new IOException("Unknown file magic: " + magicString);
    }

    /**
     * Create an encryption key from password characters.  The key is
     * used to encrypt or decrypt a single save file.  The password
     * specification used to create the secret key is remembered as a
     * default key that can be re-used in later operations.
     *
     * @param password A character array holding the password that will
     *     used for a single save or restore operation.
     * @return A cryptographic key usable in <code>Cipher</code>
     *     methods.
     * @throws GeneralSecurityException Indicates a failure constructing
     *     the key or cipher.
     */
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

    /**
     * Create an input stream for reading and decrypting account data.
     * Parameters needed to initialize the decryption are read
     * unencrypted from the raw input stream.
     *
     * @param raw Output to which encrypted data will be written.
     * @param password A character array holding the password that will
     *     encrypt the account data.
     * @return A data input stream that can supply unencrypted account
     *     data.
     * @throws GeneralSecurityException Indicates a failure constructing
     *     the key or cipher.
     * @throws IOException Indicates a failure when reading the decryption
     *     parameters.
     */
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

    /**
     * Create an output stream for encrypting and writing account data.
     * Parameters needed to initialize the encryption are written
     * unencrypted to the raw output stream.
     *
     * @param raw Output to which encrypted data will be written.
     * @param password A character array holding the password that will
     *     encrypt the account data.
     * @return A data output stream that can receive unencrypted account
     *     data.
     * @throws GeneralSecurityException Indicates a failure constructing
     *     the key or cipher.
     * @throws IOException Indicates a failure when writing the encryption
     *     parameters.
     */
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

    /**
     * Read account data from the given stream using the given password.
     * The password will be remembered, and can be reused for subsequent
     * write operations.
     *
     * @param rawInput The input stream from which encrypted data will
     *     be read.
     * @param password A character array holding the password that will
     *     decrypt the account data.
     * @throws GeneralSecurityException Indicates a failure during
     *     decryption.
     * @throws IOException Indicates a failure reading data.
     */
    public void readAccounts(InputStream rawInput, char[] password)
	    throws GeneralSecurityException, IOException {
	initialize();
	DataInputStream in;
	int formatVersion = readMagic(rawInput);
	if (formatVersion > FORMAT_V0) {
	    fileCipher = Cipher.getInstance(CIPHER_ALGORITHM);
	    in = makeInput(rawInput, password);
	} else {
	    fileKey = new PBEKeySpec(password);
	    in = new DataInputStream(rawInput);
	}
	int nElements = in.readInt();
	for (int i = 0; i < nElements; i++) {
	    addAccount(new Account(in, formatVersion));
	}
	in.close();
    }

    /**
     * Write account data to the given stream using the given password.
     * The password will be remembered, and can be reused for subsequent
     * write operations.
     *
     * @param rawOutput The output stream to which encrypted data will
     *     be written.
     * @param password A character array holding the password that will
     *     encrypt the account data.
     * @throws GeneralSecurityException Indicates a failure during
     *     encryption.
     * @throws IOException Indicates a failure writing data.
     */
    public void writeAccounts(OutputStream rawOutput, char[] password)
	    throws GeneralSecurityException, IOException {
	fileCipher = Cipher.getInstance(CIPHER_ALGORITHM);
	rawOutput.write(FILEMAGIC.getBytes());
	DataOutputStream out = makeOutput(rawOutput, password);
	out.writeInt(myAccounts.size());
	for (Account acct : myAccounts.values()) {
	    acct.writeAccount(out);
	}
	out.close();
    }

    /**
     * Write account data to the given stream using the saved password.
     * The password used is from the most recent read or write operation
     * that provided one.  If there is no saved password, a
     * <code>NullPointerException</code> exception will be thrown.
     *
     * @param outStream The output stream to which encrypted data will
     *     be written.
     * @throws GeneralSecurityException Indicates a failure during
     *     encryption.
     * @throws IOException Indicates a failure writing data.
     * @throws NullPointerException There is no saved password.
     */
    public void writeAccounts(OutputStream outStream)
	    throws GeneralSecurityException, IOException {
	writeAccounts(outStream, fileKey.getPassword());
    }

    @Override
    public boolean equals(Object o) {
	if (!(o instanceof AccountStore)) {
	    return false;
	}
	return myAccounts.equals(((AccountStore) o).myAccounts);
    }
}
