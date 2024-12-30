package database_course;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Arrays;

public class PasswordManager {

    // Method to hash the password (for storage in DB)
    public static byte[] hashPassword(char[] password) throws NoSuchAlgorithmException {
        try {

            //Generaating random salt
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            byte[] salt = new byte[16];  // 16 bytes = 128 bits
            sr.nextBytes(salt);

            //Specify PBKDF2 parameters: password, salt, iterations, and hash length
            int iterations = 10000;  // Number of iterations
            KeySpec spec = new PBEKeySpec(password, salt, iterations, 64 * 8);  // 64 bytes (512 bits) hash length
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

            // Generate the hash
            byte[] hash = skf.generateSecret(spec).getEncoded();

            // For storage: Return both the salt and the hash
            byte[] hashWithSalt = new byte[salt.length + hash.length];
            System.arraycopy(salt, 0, hashWithSalt, 0, salt.length);
            System.arraycopy(hash, 0, hashWithSalt, salt.length, hash.length);

            return hashWithSalt;  // This will be stored in the database

        } 
        catch (Exception e) {
            throw new NoSuchAlgorithmException("Error while hashing password: " + e.getMessage());
        }
    }

    // Method to validate the entered password during login
    public static boolean validatePassword(char[] enteredPassword, byte[] storedPasswordHash, byte[] storedSalt) throws NoSuchAlgorithmException {
        try {
            // Extract salt and hash from storedPasswordHash
            byte[] salt = Arrays.copyOfRange(storedPasswordHash, 0, 16);  // First 16 bytes are the salt
            byte[] storedHash = Arrays.copyOfRange(storedPasswordHash, 16, storedPasswordHash.length);  // The remaining bytes are the hash
    
            // Hash the entered password with the stored salt
            KeySpec spec = new PBEKeySpec(enteredPassword, salt, 10000, storedHash.length * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] enteredHash = skf.generateSecret(spec).getEncoded();

            // Compare the hashes (stored hash vs. generated hash)
            return Arrays.equals(storedHash, enteredHash);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;  // Return false if password validation fails due to an invalid key spec or algorithm issue
        }
    }
    
    public static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
