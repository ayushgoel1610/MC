package com.iiitd.mcproject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Formatter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
/**
 * Created by Shubham on 30 Oct 14.
 */
public class quickbloxRequest extends AsyncTask<Void, Void, String> {

    private String urlQB = "https://api.quickblox.com/session.json";
    private String appIdQB = "15476";
    private String authKeyQB = "GeO3pbwR999HM9w";
    private String authSecretQB = "h9LvUs4uLShEG7S";
    private JSONObject cred = new JSONObject();
    private JSONObject loginCred = new JSONObject();
    private StringBuilder sb = new StringBuilder();
    private String tokenQB;

    private String userLogin;
    private String userEmail;
    //private String userFBid;
    private String userFullName;
    private String userPassword;

    private ProgressDialog pDialog;
    private Context context;

    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    public quickbloxRequest(String a, String b, String c, Context cnt)
    {
        this.userLogin = a;
        this.userPassword = b;
        this.userEmail = c;
        this.context = cnt;
    }

    private String getTimeStamp()
    {
        long unixTime = System.currentTimeMillis() / 1000L;
        Log.d("Timestamp:"," "+unixTime);
        return String.valueOf(unixTime);
    }

    private String getNonce() {
        SecureRandom random = new SecureRandom();
        random.generateSeed(10);
        int rand = Math.abs(random.nextInt());
        rand = Math.abs(rand);
        Log.d("random:"," " +String.valueOf(rand));
        return String.valueOf(rand);
    }

    public static String hmacSha1(String value, String key)
    {
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
        Mac mac = null;
        try {
            mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return toHexString(mac.doFinal(value.getBytes()));
    }

    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();

        for (byte b : bytes) {
            formatter.format("%02x", b);
        }

        return formatter.toString();
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Authorising user...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    @Override
    protected String doInBackground(Void... params) {
        URL object = null;
        try {
            object = new URL(this.urlQB);
            HttpURLConnection con = (HttpURLConnection) object.openConnection();

            String nonce = getNonce();
            String timeStamp = getTimeStamp();

            con.setDoOutput(true);

            con.setDoInput(true);

            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("QuickBlox-REST-API-Version", "0.1.0");

            con.setRequestMethod("POST");

            cred.put("application_id", appIdQB);
            cred.put("auth_key", authKeyQB);
            cred.put("timestamp", timeStamp);
            cred.put("nonce", nonce);

            String str =  "application_id="+appIdQB+"&auth_key="+authKeyQB+"&nonce="+nonce+"&timestamp="+timeStamp;
//            String str =  "application_id="+appIdQB+"&auth_key="+authKeyQB+"&nonce="+nonce+"&timestamp="+timeStamp+"&user[login]="+userLogin+"&user[password]="+userPassword;
            Log.d("string",str);
            cred.put("signature",hmacSha1(str,authSecretQB));

            Log.d("debug", cred.toString());

            OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());

            wr.write(cred.toString());

            wr.flush();

            int HttpResult = con.getResponseCode();

            Log.d("response code"," "+HttpResult);

            if (HttpResult == 201)
            {

                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));

                String line = null;

                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }

                br.close();

                Log.d("debug", "" + sb.toString());

            } else {
                System.out.println(con.getResponseMessage());
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    @Override
    protected void onPostExecute(String str)
    {
        pDialog.dismiss();
        if(str != null)
        {
            str = str.substring(1,str.length()-3);
            String[] strings = str.split(",");
            for(String pqr : strings )
            {
                String[] strings1 = pqr.split(":");
                if(strings1[0].equals("\"token\""))
                {
                    tokenQB = strings1[1];
                    tokenQB = tokenQB.substring(1,tokenQB.length()-1);
                    break;
                }
            }
            if(tokenQB != null)
            {
                quickbloxLogin newLogin = new quickbloxLogin(userLogin,userPassword,userEmail,tokenQB,context);
                newLogin.execute();
            }
            else
            {
                Log.d("token","is null");
            }
        }
        else
            Log.d("system response","is null");

    }

}
