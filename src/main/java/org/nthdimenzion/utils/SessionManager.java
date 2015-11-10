package org.nthdimenzion.utils;

import com.sun.xml.internal.xsom.impl.scd.Iterators;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Asus on 7/9/2015.
 */
public class SessionManager {
    static SessionManager instance = null;
    Map<String,String> map = new HashMap();

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void addSession(String token,String userName) {
        map.put(token,userName);
    }

    public String getSession(String token) {
        return map.get(token);
    }
}
