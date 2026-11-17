package com.aiproj.educar;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Data Access Object (DAO) for authenticating and registering users with a Supabase database.
 * <p>
 * NOTE: All network operations are synchronous and must be invoked on a background thread.
 */
@SuppressWarnings("ALL")
public class UserDao {

    private static final String TAG = "UserDao";

    // Placeholders - replace with your actual Supabase credentials and database table name
    private static final String SUPABASE_URL = "https://bhmwecedimacvsffaxdk.supabase.co";
    private static final String SUPABASE_KEY = "sb_publishable_MbMgiCCG2HytlVoLZbYy6g_nRQyxejl";
    private static final String TABLE_NAME = "users";

    /**
     * Encrypts plain text locally using the SHA-256 algorithm.
     *
     * @param input Plain text string to hash
     * @return Hexadecimal representation of SHA-256 hash
     */
    public static String hashSHA256(String input) {
        if (input == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "SHA-256 algorithm not available", e);
            return null;
        }
    }

    /**
     * Checks if a username is available (i.e. does not already exist in the database).
     *
     * @param user Username to check
     * @return true if username is available, false if it already exists or if an error occurs
     */
    public boolean isUserAvailable(String user) {
        if (user == null || user.trim().isEmpty()) {
            return false;
        }
        try {
            String queryUrl = SUPABASE_URL + "/rest/v1/" + TABLE_NAME
                    + "?user=eq." + URLEncoder.encode(user, StandardCharsets.UTF_8.name())
                    + "&select=user";

            HttpURLConnection conn = createConnection(queryUrl, "GET");
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                String response = readStream(conn.getInputStream());
                conn.disconnect();
                return "[]".equals(response.trim());
            }
            conn.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "Error checking user availability", e);
        }
        return false;
    }

    /**
     * Authenticates a user against the Supabase database.
     * The password is encrypted locally with SHA-256 before accessing the database.
     *
     * @param user   Username
     * @param passwd Plain text password
     * @return true if user credentials match a record, false otherwise
     */
    public boolean authenticate(String user, String passwd) {
        if (user == null || passwd == null || user.trim().isEmpty() || passwd.trim().isEmpty()) {
            return false;
        }
        try {
            String hashedPasswd = hashSHA256(passwd);
            String queryUrl = SUPABASE_URL + "/rest/v1/" + TABLE_NAME
                    + "?user=eq." + URLEncoder.encode(user, StandardCharsets.UTF_8.name())
                    + "&passwd=eq." + URLEncoder.encode(hashedPasswd, StandardCharsets.UTF_8.name())
                    + "&select=user";

            HttpURLConnection conn = createConnection(queryUrl, "GET");
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                String response = readStream(conn.getInputStream());
                conn.disconnect();
                String trimmed = response.trim();
                return !"[]".equals(trimmed) && trimmed.length() > 2;
            }
            conn.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "Error authenticating user", e);
        }
        return false;
    }

    /**
     * Registers a new user in the Supabase database.
     * Checks if the username is available first, encrypts the password locally using SHA-256,
     * and inserts the new user into the database.
     *
     * @param user   Username
     * @param passwd Plain text password
     * @return true if registration succeeded, false if user exists or operation failed
     */
    public boolean register(String user, String passwd) {
        if (user == null || passwd == null || user.trim().isEmpty() || passwd.trim().isEmpty()) {
            return false;
        }

        // User available check: verify if the user already exists
        if (!isUserAvailable(user)) {
            Log.w(TAG, "User registration failed: username already exists or invalid.");
            return false;
        }

        try {
            String hashedPasswd = hashSHA256(passwd);
            String targetUrl = SUPABASE_URL + "/rest/v1/" + TABLE_NAME;

            HttpURLConnection conn = createConnection(targetUrl, "POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Prefer", "return=minimal");
            conn.setDoOutput(true);

            String jsonPayload = "{\"user\":\"" + escapeJson(user) + "\",\"passwd\":\"" + escapeJson(hashedPasswd) + "\"}";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            conn.disconnect();

            return responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            Log.e(TAG, "Error registering user", e);
        }
        return false;
    }

    private HttpURLConnection createConnection(String urlString, String method) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("apikey", SUPABASE_KEY);
        conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_KEY);
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        return conn;
    }

    private String readStream(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }

    private String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
