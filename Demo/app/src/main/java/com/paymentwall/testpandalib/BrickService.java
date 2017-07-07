package com.paymentwall.testpandalib;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.paymentwall.pwunifiedsdk.brick.core.Brick;
import com.paymentwall.pwunifiedsdk.brick.core.BrickHelper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;

/**
 * Created by nguyen.anh on 4/8/2016.
 */
public class BrickService extends Service {

    private static final String URL_3DS = "http://testbed1.stuffio.com/bricktest/secure-form.php";

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(getPackageName() + Brick.BROADCAST_FILTER_MERCHANT)) {
                Log.i(getClass().getSimpleName(), intent.getStringExtra(Brick.KEY_BRICK_TOKEN));
                processBackend(intent.getStringExtra(Brick.KEY_BRICK_TOKEN));
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(getPackageName() + Brick.BROADCAST_FILTER_MERCHANT));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    private void processBackend(final String token) {
        Thread createTokenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                charge(token, "andy@paymentwall.com");
            }
        });
        createTokenThread.start();
    }

    public static String charge(String token, String email) {
        // Prepare SSL
        String originalDNSCacheTTL = null;
        boolean allowedToSetTTL = true;
        try {
            originalDNSCacheTTL = Security
                    .getProperty("networkaddress.cache.ttl");
            Security.setProperty("networkaddress.cache.ttl", "0");
        } catch (SecurityException se) {
            allowedToSetTTL = false;
        }
        try {
            // Create HTTP request
            Map<String, String> parameters = new HashMap<>();
            parameters.put("brick_token", token);
            parameters.put("email", email);
            String queryUrl = BrickHelper.urlEncodeUTF8(parameters);
            // Connect
            HttpURLConnection conn = createPostRequest(new URL(URL_3DS), queryUrl, 30000, 30000);
            // Get message
            String response = getResponseBody(conn.getInputStream());
            Log.i("RESPONSE", response + "");
            JSONObject object = new JSONObject(response);
            JSONObject objSecure = object.getJSONObject("secure");
            String redirect = objSecure.getString("redirect");
            Brick.getInstance().setResult(redirect);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (allowedToSetTTL) {
                if (originalDNSCacheTTL == null) {
                    Security.setProperty("networkaddress.cache.ttl", "-1");
                } else {
                    Security.setProperty("networkaddress.cache.ttl",
                            originalDNSCacheTTL);
                }
            }
        }
        return "";
    }

    private static HttpURLConnection createPostRequest(URL url, String queryUrl, int connectionTimeout, int readTimeout) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {

        }
        conn.setConnectTimeout(connectionTimeout);
        conn.setReadTimeout(readTimeout);
        conn.setUseCaches(false);
        conn.setDoOutput(true);
        try {
            conn.setRequestMethod("POST");
        } catch (ProtocolException e) {

        }
        conn.setRequestProperty("Content-Type", String.format(
                "application/x-www-form-urlencoded;charset=%s",
                new Object[]{"UTF-8"}));
        checkSSLCert(conn);
        OutputStream output = null;
        try {
            output = conn.getOutputStream();
            output.write(queryUrl.getBytes("UTF-8"));
            int statusCode = conn.getResponseCode();
            if (statusCode < 200 || statusCode >= 300) {
                String errorResponse;
                try {
                    errorResponse = getResponseBody(conn.getErrorStream());
                } catch (Exception e) {
                    errorResponse = null;
                }
            }
        } catch (UnsupportedEncodingException e) {
        } catch (IOException e) {
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                }
            }
        }
        return conn;
    }

    private static String getResponseBody(InputStream responseStream) {
        String rBody = getStringFromInputStream(responseStream);
        try {
            responseStream.close();
        } catch (IOException e) {
        }
        return rBody;
    }

    private static String getStringFromInputStream(InputStream is) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {}
        finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {}
            }
        }
        return sb.toString();
    }

    private static void checkSSLCert(HttpURLConnection con) {
        if ((!con.getURL().getHost().equals("pwgateway.com"))) {
            return;
        }

        HttpsURLConnection conn = (HttpsURLConnection) con;
        try {
            conn.connect();
        } catch (IOException ioexception) {
        }

        Certificate[] certs = null;
        try {
            certs = conn.getServerCertificates();
        } catch (SSLPeerUnverifiedException e) {
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            byte[] der = certs[0].getEncoded();
            md.update(der);
            byte[] digest = md.digest();
            byte[] revokedCertDigest = {5, -64, -77, 100, 54, -108, 71, 10,
                    -120, -116, 110, 127, -21, 92, -98, 36, -24, 35, -36, 83};
            if (Arrays.equals(digest, revokedCertDigest)) {
            }
        } catch (NoSuchAlgorithmException e) {
        } catch (CertificateEncodingException e) {
        }
    }
}
