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
import java.util.Iterator;

import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

import java.net.URL;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.UUID;

/**
 * A set of accounts that can be saved to or read from a file.
 *<p>
 * An <code>AccountStore</code> provides the ability to add, remove,
 * update, and access individual {@link Account} objects. The entire
 * set of accounts can be saved in a file, encrypted, and later read
 * back to reconstruct the saved account store.
 */
public class AccountStore implements Iterable<Account>, Cloneable {
    static int FORMAT_V1 = 1;

    static int FORMAT_V2 = 2;

    private static final String[] MAGIC_VERSIONS = {
	"ACCTS.00",	// FORMAT_V0
	"ACCTS.01",	// FORMAT_V1
	"ACCTS.02",	// FORMAT_V2
    };

    static int FORMAT_CURRENT = MAGIC_VERSIONS.length - 1;

    private static final String FILEMAGIC
	    = MAGIC_VERSIONS[FORMAT_CURRENT];

    private static final String PASSWORD_TYPE = "AES";

    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

    private static final int PASSWORD_COUNT = 0x10000;

    private static final int KEY_BITS = 128;

    private static final SecureRandom randomSource = new SecureRandom();

    /**
     * Password salt used to create an encryption key from a password.
     * The salt is actually temporary data that is only live while
     * initializing for {@link #readAccounts} or {@link #writeAccounts}.
     * The storage persists when not in use to avoid creating
     * unnecessary garbage.
     */
    private byte[] passwordSalt = new byte[8];

    /**
     * Initialization vector to start encryption and decryption
     * operations.  This block is actually temporary data that is only
     * live while initializing for {@link #readAccounts} or {@link
     * #writeAccounts}.  The storage persists when not in use to avoid
     * creating unnecessary garbage.
     */
    private byte[] ivBlock = new byte[128 / 8];		// 1 128-bit AES block

    /**
     * A key specification created from the user-supplied password.
     * This is remembered so that it can be re-used for {@link
     * #writeAccounts(OutputStream)}.
     */
    private PBEKeySpec fileKey;

    /**
     * A map containing all accounts in the store.  The account
     * description is used as the key to the map to facilitate iteration
     * order and to prevent duplicate descriptions.
     */
    private TreeMap<String, Account> myAccounts;

    /**
     * A map containing all accounts in the store.  The account
     * UUID is used as the key to the map to facilitate finding matching
     * accounts during merge operations.
     */
    private HashMap<UUID, Account> uuidMap;

    /**
     * Construct an empty account store.
     */
    public AccountStore() {
	initialize();
    }

    /**
     * Construct an account store by reading it from a stream.
     *
     * @param raw An input stream from which the account data will be
     *     read.
     * @param password A character array holding the password that will
     *     decrypt the account data.
     * @throws GeneralSecurityException Indicates a failure during
     *     decryption.
     * @throws IOException Indicates a failure reading data.
     */
    public AccountStore(InputStream raw, char[] password)
	    throws GeneralSecurityException, IOException {
	readAccounts(raw, password);
    }

    /**
     * Clear out the contents of this account store.  This method
     * restores this account store to the state created by the no-arg
     * constructor.
     */
    private void initialize() {
	myAccounts = new TreeMap<String, Account>();
	uuidMap = new HashMap<UUID, Account>();
    }

    /**
     * Add a new account to the account store.
     *
     * @param newAccount The account to be added to the account store.
     */
    private void addAccount(Account newAccount) {
	assert !uuidMap.containsKey(newAccount.getUUID());
	assert !myAccounts.containsKey(newAccount.getDescription());
	myAccounts.put(newAccount.getDescription(), newAccount);
	uuidMap.put(newAccount.getUUID(), newAccount);
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
	myAccounts.put(account.getDescription(), account);
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
     * Merge account entries from another account store, adding anything
     * not already in this account store.
     *
     * @param mergeSource The source account store to be merged into
     *     this account store.
     */
    public void mergeAccounts(AccountStore mergeSource) {
	for (Account account : mergeSource) {
	    Account existing = uuidMap.get(account.getUUID());
	    // FIXME: These operations will fail if it tries to create
	    // a duplicate description.
	    if (existing == null) {
		addAccount(account);
	    } else {
		myAccounts.remove(existing.getDescription());
		existing.mergeHistory(account);
		myAccounts.put(existing.getDescription(), existing);
	    }
	}
    }

    /**
     * Delete an account from the account store.
     *
     * @param account The account to be deleted from the account store.
     */
    public void deleteAccount(Account account) {
	uuidMap.remove(account.getUUID());
	myAccounts.remove(account.getDescription());
    }

    /**
     * Return the number of accounts in the account store.
     *
     * @return The number of accounts in this store.
     */
    public int size() {
	return myAccounts.size();
    }

    /**
     * Return an <code>Iterator</code> over every account.  Iteration
     * returns the accounts ordered by their description.
     *
     * @return An iterator over all the accounts.
     */
    public Iterator<Account> iterator() {
	return myAccounts.values().iterator();
    }

    /**
     * Read the file magic from the start of a saved file, to determine
     * its format version.  If the stream starts with a recognized magic
     * string, return the associated file format version number.  If the
     * magic isn't recognized as a supported version, throw an
     * <code>IOException</code>.
     *
     * @param in The input stream from which to read version magic.
     * @return The file format version number.
     * @throws IOException indicates that the file magic couldn't be
     *     read, or wasn't recognized.
     */
    private int readMagic(InputStream in) throws IOException {
	byte[] magic = new byte[FILEMAGIC.length()];
	int nRead = in.read(magic);
	String magicString = new String(magic, 0, nRead);
	if (nRead < magic.length) {
	    throw new AccountFileFormatException(
		    "Magic truncated: " + magicString);
	}
	int version = 0;
	for (String testMagic : MAGIC_VERSIONS) {
	    if (magicString.equals(testMagic)) {
		return version;
	    }
	    version++;
	}
	throw new AccountFileFormatException(
		"Unknown file magic: " + magicString);
    }

    /**
     * Create an encryption key from a password.  The key is used to
     * read or write a save file.  The password specification object
     * used to create the secret key is remembered, and can be re-used
     * in later operations.
     *
     * @param password A character array holding the password to be used
     *     for constructing the key.
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
     * Create an input stream that reads and decrypts account data from
     * a raw input stream.
     *<p>
     * Parameters needed to initialize the decryption are read
     * unencrypted from the raw input stream.
     *<p>
     * The password used to generate the encryption key is remembered,
     * and may be re-used in subsequent write operations.
     *
     * @param raw Input stream from which encrypted data will be read
     *     and decrypted.
     * @param password A character array holding the password that will
     *     decrypt the account data.
     * @return A data input stream that can supply decrypted account
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
	Cipher fileCipher = Cipher.getInstance(CIPHER_ALGORITHM);
	fileCipher.init(Cipher.DECRYPT_MODE,
			makeKey(password),
			new IvParameterSpec(ivBlock));
	InputStream decryptedStream =
	    new CipherInputStream(raw, fileCipher);
	return new DataInputStream(decryptedStream);
    }

    /**
     * Create an output stream that encrypts and writes account data to
     * a raw output stream.
     *<p>
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
	Cipher fileCipher = Cipher.getInstance(CIPHER_ALGORITHM);
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
     * @param raw The input stream from which encrypted data will be
     *     read.
     * @param password A character array holding the password that will
     *     decrypt the account data.
     * @throws GeneralSecurityException Indicates a failure during
     *     decryption.
     * @throws IOException Indicates a failure reading data.
     */
    public void readAccounts(InputStream raw, char[] password)
	    throws GeneralSecurityException, IOException {
	int formatVersion = readMagic(raw);
	if (formatVersion < FORMAT_V1) {
	    throw new AccountFileFormatException(
		    "File format V" + formatVersion
		    + " is not supported");
	}
	initialize();
	DataInputStream in = makeInput(raw, password);
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
     * @param raw The output stream to which encrypted data will be
     *     written.
     * @param password A character array holding the password that will
     *     encrypt the account data.
     * @throws GeneralSecurityException Indicates a failure during
     *     encryption.
     * @throws IOException Indicates a failure writing data.
     */
    public void writeAccounts(OutputStream raw, char[] password)
	    throws GeneralSecurityException, IOException {
	raw.write(FILEMAGIC.getBytes());
	DataOutputStream out = makeOutput(raw, password);
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
    protected AccountStore clone() {
	try {
	    AccountStore accts = (AccountStore) super.clone();
	    accts.passwordSalt = passwordSalt.clone();
	    accts.ivBlock = ivBlock.clone();
	    // FIXME: These constructors presumably create shallow
	    // copies, so changes in the original accounts will show up
	    // in the clone as well, and vice versa.
	    accts.myAccounts = new TreeMap<String, Account>(myAccounts);
	    accts.uuidMap = new HashMap<UUID, Account>(uuidMap);
	    return accts;
	} catch (CloneNotSupportedException ex) {
	    return null;
	}
    }

    @Override
    public boolean equals(Object o) {
	if (!(o instanceof AccountStore)) {
	    return false;
	}
	return myAccounts.equals(((AccountStore) o).myAccounts);
    }
}
