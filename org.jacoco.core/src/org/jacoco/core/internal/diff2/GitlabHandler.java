package org.jacoco.core.internal.diff2;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public class GitlabHandler {
    private final String token;
    private final String project_id;
    private final String host;
    private final String http_type;
    private OkHttpClient http_client;
    private List<String> headers;

    public GitlabHandler(String host, String project_id, String token) {
        this.token = token;
        this.project_id = project_id;
        this.host = host;
        this.http_type = "http";
        this.init();
    }
    public GitlabHandler(String host, String project_id, String token,String http_type) {
        this.token = token;
        this.project_id = project_id;
        this.host = host;
        this.http_type = http_type;
        this.init();
    }
    private void init(){
        http_client = new OkHttpClient();
        headers = Arrays.asList("PRIVATE-TOKEN", "ArW2x6am9FiZVUizt6M7");
    }


    public JSONObject get(String uri, Map<String,String> params, List<String> headers) {
        HttpUrl.Builder url_builder = new HttpUrl.Builder()
                .scheme(http_type)
                .host(host);
        for (String item:uri.split("/")) {
            url_builder.addEncodedPathSegment(item);
        }
        for (Map.Entry<String,String> item:params.entrySet()) {
            url_builder.addQueryParameter(item.getKey(),item.getValue());
        }
        HttpUrl url = url_builder.build();

        Request.Builder request_builder = new Request.Builder()
                .url(url);
        if (headers != null){
            request_builder.addHeader(headers.get(0),headers.get(1));
        }
        Request request = request_builder.build();
        String response_str = "";
        try (Response response = http_client.newCall(request).execute()){
            response_str = Objects.requireNonNull(response.body()).string();
        } catch (IOException |NullPointerException e) {
            e.printStackTrace();
        }
        return JSON.parseObject(response_str);
    }

    /**
     *获取java源码的内容
     * @param file_path URL encoded full path to new file. Ex. lib%2Fclass%2Erb
     * @param ref The name of branch, tag or commit
     * @return file_path content
     */
    public String get_file_content(String file_path, String ref){
        Map<String,String> params = new HashMap<String,String>();
        params.put("ref",ref);
        String file_path_encode;
        try {
            file_path_encode = URLEncoder.encode(file_path,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            file_path_encode = file_path;
        }
        String uri = "/api/v4/projects/" + this.project_id + "/repository/files/" + file_path_encode;
        JSONObject res = get(uri, params,this.headers);
        String res_content_base64 = res.get("content").toString();
        byte[] res_content = Base64.getDecoder().decode(res_content_base64);
        String content = new String(res_content);
        return content;
    }

    /**
     *  @param from The commit SHA or branch name.
     * @param to The commit SHA or branch name.
     */
    public JSONArray get_commit_diff(String from, String to){
        String uri = "/api/v4/projects/" + this.project_id + "/repository/compare";
        Map<String, String> params = new HashMap<>();
        params.put("from",from);
        params.put("to",to);
        JSONObject res = get(uri,params,this.headers);
        return (JSONArray) res.get("diffs");
    }

}
