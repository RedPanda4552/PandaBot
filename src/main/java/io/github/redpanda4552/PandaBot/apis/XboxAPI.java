/**
 * This file is part of PandaBot, licensed under the MIT License (MIT)
 * 
 * Copyright (c) 2017 Brian Wood
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.redpanda4552.PandaBot.apis;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;

public class XboxAPI {

    private static String xboxAPIKey;
    
    public enum XboxAPIEndpoint {

        XUID_FROM_GAMERTAG("/v2/xuid/{var}"),
        ACTIVITY_FROM_XUID("/v2/{var}/activity"),
        PRESENCE_FROM_XUID("/v2/{var}/presence");
        
        private String url;
        
        private XboxAPIEndpoint(String url) {
            this.url = url;
        }
        
        /**
         * Get a Xbox API URL. Substitutes the var parameter for the variable part
         * of the URL.
         */
        public String getUrl(String var) {
            return "https://xboxapi.com" + url.replace("{var}", var);
        }
    }
    
    public static void setAPIKey(String str) {
        xboxAPIKey = str;
    }
    
    /**
     * Get a Future object for a HTTP POST to the Xbox API.
     */
    public static Future<HttpResponse<JsonNode>> request(String url) {
        if (xboxAPIKey == null)
            return null;
        
        Future<HttpResponse<JsonNode>> future = Unirest.post(url)
                .header("X-AUTH: " + xboxAPIKey, "application/json")
                .header("Accept-Language: en-US", "application/json")
                .asJsonAsync(new Callback<JsonNode>() {

                  public void failed(UnirestException e) {
                      System.out.println("The request has failed");
                  }

                  public void completed(HttpResponse<JsonNode> response) {
                       int code = response.getStatus();
                       Map<String, List<String>> headers = response.getHeaders();
                       JsonNode body = response.getBody();
                       InputStream rawBody = response.getRawBody();
                  }

                  public void cancelled() {
                      System.out.println("The request has been cancelled");
                  }

              });
        
        return future;
    }
}
