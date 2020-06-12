package fz.okhttplib.tool;

import android.util.Log;

import com.google.gson.Gson;

import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fz.okhttplib.BuildConfig;
import fz.okhttplib.base.Params;


/**
 * Create by JFZ
 * date: 2019-04-22 14:24
 **/
public class OkhttpUtil {
    private static boolean DEBUG = BuildConfig.DEBUG;
    public static final Gson GSON = new Gson();

    public static void LogDebug(boolean debug) {
        DEBUG = debug;
    }

    public static void log(String tag, String msg) {
        if (DEBUG) {
//            String result = "\n";
//            StackTraceElement thisMethodStack = (new Exception()).getStackTrace()[1];
//            result += thisMethodStack.getClassName() + "."; // 当前的类名（全名）
//            result += thisMethodStack.getMethodName();
//            result += "(" + thisMethodStack.getFileName();
//            result += ":" + thisMethodStack.getLineNumber() + ")  \n";
//            Log.e(tag, result + "\n" + msg);

            String result = "\n";
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            StackTraceElement thisMethodStack = stackTrace[5];
            String className = thisMethodStack.getClassName();
            String[] classNameInfo = className.split("\\.");
            if (classNameInfo.length > 0) {
                className = classNameInfo[classNameInfo.length - 1] + ".java";
            }
            if (className.contains("$")) {
                className = className.split("\\$")[0] + ".java";
            }
            result += className + "."; // 当前的类名（全名）
            result += thisMethodStack.getMethodName();
            result += "(" + thisMethodStack.getFileName();
            result += ":" + thisMethodStack.getLineNumber() + ")  \n";
            Log.e(tag, result + "\n" + msg);
        }
    }

    //添加参数
    public static String appendParams(Params[] params) {
        if (params.length == 0) {
            return "";
        }
        int pos = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("?");
        for (Params p : params) {
            if (pos > 0) {
                sb.append("&");
            }
            sb.append(p.key).append("=").append(p.value);
            pos++;
        }
        return sb.toString();
    }

    //map转换params
    public static Params[] map2Params(HashMap<String, Object> map) {
        Params[] params = null;
        if (map != null) {
            Iterator iter = map.entrySet().iterator();
            List<Params> paramsList = new ArrayList<Params>();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                Object key = entry.getKey();
                Object val = entry.getValue();
                if (val == null) {
                    val = "";
                }
                paramsList.add(new Params(key.toString(), val.toString()));
            }
            params = new Params[paramsList.size()];
            for (int i = 0; i < paramsList.size(); i++) {
                params[i] = paramsList.get(i);
            }
        } else {
            params = new Params[0];
        }
        return params;
    }

    public static Params[] validateParam(Params[] params) {
        if (params == null)
            return new Params[0];
        else
            return params;
    }

    public static String guessMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(path);
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
    }

    public static String reqParams(Object src) {
        String json = "";
        if (src != null && src instanceof String) {
            json = (String) src;
        } else {
            if (src == null) {
            } else {
                json = GSON.toJson(src);
            }
        }
        return json;
    }

}
