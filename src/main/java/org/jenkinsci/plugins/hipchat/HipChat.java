package org.jenkinsci.plugins.hipchat;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jenkinsci.plugins.hipchat.NotifyMessage;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;

/**
 * Created with IntelliJ IDEA.
 * User: me-me-
 * Date: 2013/11/26
 * Time: 16:08
 * To change this template use File | Settings | File Templates.
 */
public class HipChat {
    private static final String NOTIFY_URL = "%s/v2/room/%s/notification?auth_token=%s";
    private final String token;
    private final String server;
    private final String proxy_host;
    private final String proxy_port;
    private CloseableHttpClient client = null;
    
    public HipChat(String token, String server, String proxy_host, String proxy_port) {
        this.token = token;
        this.server = server;
        this.proxy_host = proxy_host;
        this.proxy_port = proxy_port;
    }

    public boolean notify(String room, NotifyMessage message) {
        try {
        	
        	if (proxy_host != "") {
        		HttpHost proxy = new HttpHost(proxy_host, Integer.valueOf(proxy_port));
        		
            	client = HttpClients.custom()
            		    .useSystemProperties()
            		    .setProxy(proxy)
            		    .build();        		
        	} else {
        		client = HttpClients.createDefault();
        	}
        	
        	HttpPost post = new HttpPost(String.format(NOTIFY_URL, this.server, encode(room), encode(this.token)));
            post.setEntity(new StringEntity(message.toJson().toString(), ContentType.create("application/json", Charset.defaultCharset())));
            
			
			CloseableHttpResponse res = client.execute(post);

            if (res.getStatusLine().getStatusCode() > 400) {
                System.out.println(String.format("HipChat Notify Status Code : %d", res.getStatusLine().getStatusCode()));
                return false;
            }
            return true;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String encode(String v) {
        try {
            return URLEncoder.encode(v, "UTF-8")
                    .replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
