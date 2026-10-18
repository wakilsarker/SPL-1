package hospital.auth;

import hospital.util.CsvUtil;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/** Stores local SPL demo accounts as salted SHA-256 hashes in data/users.csv. */
public final class AuthService {
    private static final String FILE = "data/users.csv";

    public void seedDefaultAccounts() {
        if (!CsvUtil.read(FILE).isEmpty()) return;
        save("admin", "admin123", "ADMIN");
        save("doctor", "doctor123", "DOCTOR");
        save("reception", "reception123", "RECEPTIONIST");
    }

    public UserAccount authenticate(String username, String password) {
        for (String[] row : CsvUtil.read(FILE)) {
            if (row.length >= 4 && row[0].equalsIgnoreCase(username)
                    && matches(row[1], row[2], password)) {
                return new UserAccount(row[0], row[3]);
            }
        }
        return null;
    }

    private void save(String username, String password, String role) {
        byte[] saltBytes = new byte[16];
        new SecureRandom().nextBytes(saltBytes);
        String salt = Base64.getEncoder().encodeToString(saltBytes);
        CsvUtil.append(FILE, new String[]{username, salt, hash(salt, password), role});
    }

    private boolean matches(String salt, String expectedHash, String password) {
        return MessageDigest.isEqual(expectedHash.getBytes(StandardCharsets.UTF_8),
                hash(salt, password).getBytes(StandardCharsets.UTF_8));
    }

    private String hash(String salt, String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] value = digest.digest((salt + ":" + password).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(value);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}
