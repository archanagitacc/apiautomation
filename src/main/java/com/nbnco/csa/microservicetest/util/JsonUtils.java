package com.nbnco.csa.microservicetest.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class JsonUtils {

    public static JSONObject replaceJsonValues(JSONObject obj, String keyMain, String newValue) throws JSONException {
        // We need to know keys of Jsonobject
        JSONObject json = new JSONObject();
        Iterator iterator = obj.keys();
        String key = null;
        while (iterator.hasNext()) {
            key = (String) iterator.next();
            // TODO: Vineel: diagnosticDetails is causing json issues as the content is list, for now esc because its static content.
            if(key.equalsIgnoreCase("diagnosticDetails")){
                continue;
            }
            // if object is just string we change value in key
            if ((obj.optJSONArray(key)==null) && (obj.optJSONObject(key)==null)) {
                if (key.equals(keyMain)) {
                    // put new value
                    obj.put(key, newValue);
                    return obj;
                }
            }

            // if it's jsonobject
            if (obj.optJSONObject(key) != null) {
                replaceJsonValues(obj.getJSONObject(key), keyMain, newValue);
            }

            // if it's jsonarray
            if (obj.optJSONArray(key) != null) {
                JSONArray jArray = obj.getJSONArray(key);
                for (int i=0;i<jArray.length();i++) {
                    try {
                        replaceJsonValues(jArray.getJSONObject(i), keyMain, newValue);
                    }
                    catch (JSONException e){
                        System.out.println("Warning: org.json.JSONException: JSONArray[0] is not a JSONObject.");
                    }
                }
            }
        }
        return obj;
    }

}
