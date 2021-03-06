package com.zlab.btcmonitor._API;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zlab.btcmonitor.bm_Main;
import org.apache.commons.codec.binary.Hex;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class btce_doTrade {
    public static JsonObject doTrade(String pair, String type, String rate, String amount) {
        long nonce_param = System.currentTimeMillis() / 1000;
        String method = "Trade";

        Mac mac;
        SecretKeySpec key;

        String postData = "method="+method+"&nonce="+nonce_param+"&type="+type+"&pair="+pair+"&amount="+amount+"&rate="+rate;

        // Create a new secret key
        try {
            key = new SecretKeySpec( bm_Main.API_SECRET.getBytes( "UTF-8"), "HmacSHA512" );
        } catch( UnsupportedEncodingException uee) {
            //System.err.println( "Unsupported encoding exception: " + uee.toString());
            return null;
        }

        // Create a new mac
        try {
            mac = Mac.getInstance( "HmacSHA512" );
        } catch( NoSuchAlgorithmException nsae) {
            //System.err.println( "No such algorithm exception: " + nsae.toString());
            return null;
        }

        // Init mac with key.
        try {
            mac.init( key);
        } catch( InvalidKeyException ike) {
            //System.err.println( "Invalid key exception: " + ike.toString());
            return null;
        }

        Connection.Response response = null;
        try {
            String urlToJson = bm_Main.API_URL_PRIVATE;
            if(bm_Main.prefs_use_proxy){ urlToJson = bm_Main.API_ZLAB_PROXY_URL+urlToJson; }

            response = Jsoup.connect(urlToJson)
                    .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                    .ignoreContentType(true)
                    .header("Key", bm_Main.API_KEY)
                    .header("Sign", new String(Hex.encodeHex(mac.doFinal(postData.getBytes("UTF-8")))))
                    .data("method", "" + method)
                    .data("nonce", "" + nonce_param)
                    .data("type", "" + type)
                    .data("pair", "" + pair)
                    .data("amount", "" + amount)
                    .data("rate", "" + rate)
                    .method(Connection.Method.POST)
                    .execute();
        } catch (Exception e) {
//            Log.e("ERR", e.getMessage());

        }

        if(response!=null){
            JsonElement jelement = new JsonParser().parse(response.body());
            JsonObject json_all = jelement.getAsJsonObject();
            return json_all;
        } else {
            return null;
        }
    }

    public static boolean getSuccess(JsonObject doTrade_Json){
        if(doTrade_Json.get("success").toString().equals("1")){
            return true;
        } else {
            return false;
        }
    }

    public static JsonObject getReturn(JsonObject doTrade_Json){
        return doTrade_Json.get("return").getAsJsonObject();
    }
    public static String getReceived(JsonObject doTradeReturn){
        return doTradeReturn.get("received").toString();
    }
    public static String getRemain(JsonObject doTradeReturn){
        return doTradeReturn.get("remains").toString();
    }
    public static String getOrderID(JsonObject doTradeReturn){
        return doTradeReturn.get("order_id").toString();
    }

    public static String getFunds(JsonObject doTradeReturn, String currency){
        JsonObject jarray_funds = doTradeReturn.get("funds").getAsJsonObject();
        return jarray_funds.get(currency.toLowerCase()).toString();
    }
}
