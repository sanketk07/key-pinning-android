package keypinning.demo.com.key_pinning_demo;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

import java.net.URL;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HttpGetRequest getRequest = new HttpGetRequest();
        try {
            String result = getRequest.execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

    }


    private boolean validatePinning(HttpsURLConnection conn, Set<String> validPins) {
        try {
            Certificate[] certs = conn.getServerCertificates();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            for (Certificate cert : certs) {
                //System.out.println("certs-->"+ cert);
                X509Certificate x509Certificate = (X509Certificate) cert;
                //System.out.println("x509Certificate-->"+x509Certificate.getPublicKey());
                byte[] key = x509Certificate.getPublicKey().getEncoded();
                md.update(key, 0, key.length);
                byte[] hashBytes = md.digest();
                String base64String = Base64.encodeToString(hashBytes, Base64.NO_WRAP);
                /*StringBuffer hexHash = new StringBuffer();
                for (int i = 0; i < hashBytes.length; i++) {
                    int k = 0xFF & hashBytes[i];
                    String tmp = (k<16)? "0" : "";
                    tmp += Integer.toHexString(0xFF & hashBytes[i]);
                    hexHash.append(tmp);
//                    System.out.println("tmp--->"+tmp);
                }*/
                if (validPins.contains(base64String.toString())) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    private static final Set<String> PINS = new HashSet<String>(Arrays.asList(
            new String[]{
                    "asMiZzD598/uDdSq2AREjX+zwE/Zf/rW6C3qZmSQFBk=", //leaf
                    "WoiWRyIOVNa9ihaBciRSC7XHjliYS9VwUGOIud4PB18=", //root
                    "k2v657xBsOVe1PQRwOsHsw3bsGT2VzIqz5K+59sNQws=" //intermediate
            }));

    public class HttpGetRequest extends AsyncTask<String, Object, String> {

        TextView textView = (TextView) findViewById(R.id.textView);
        String dest = "https://www.stage2d0065.stage.paypal.com";
        //String dest = "https://www.gmail.com";

        @Override
        protected String doInBackground(String... urls) {
            try {
                /** Test pinning given the target URL **/
                /** for now use pre-defined endpoint URL instead or urls[0] **/
                Log.d("In doInBackground", "==> PinningTestTask launched.");

                URL targetURL = new URL(dest);
                HttpsURLConnection targetConnection = (HttpsURLConnection) targetURL.openConnection();
                targetConnection.connect();
                if (validatePinning(targetConnection, PINS)) {
                    final String updateText = "Key pinning succeded for: " + dest;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText(updateText);
                        }
                    });
                } else {
                    final String updateText = "Key pinning failed for: " + dest;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText(updateText);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                final String updateText = "Key pinning failed for: " + dest + "\n" + e.toString();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(updateText);
                    }
                });
            }
            return "";
        }

    }
}
