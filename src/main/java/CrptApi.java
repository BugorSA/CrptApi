import com.google.gson.JsonObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CrptApi {

    private final ConcurrentMap<Long, AtomicInteger> windows = new ConcurrentHashMap<>();

    protected final int requestLimit;

    private final TimeUnit timeUnit;

    private String token = "sdsdsdsdsXD";

    boolean allow() {
        long windowKey = timeUnit.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        windows.putIfAbsent(windowKey, new AtomicInteger(0));
        return windows.get(windowKey).incrementAndGet() <= requestLimit;
    }

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.requestLimit = requestLimit;
        this.timeUnit = timeUnit;
    }

    public boolean introduceIntoCirculation(JsonObject jsonDocument, String signature) throws IOException {
        if (!allow()) {
            introduceInCirculationUncheck(jsonDocument, signature);
            return true;
        } else return false;
    }

    private void introduceInCirculationUncheck(JsonObject jsonDocument, String signature) throws IOException {
        JsonObject requestBody = buildReqBody(jsonDocument, signature, "LP_INTRODUCE_GOODS");
        HTTPService httpService = new HTTPService();
        httpService.connect("https://ismp.crpt.ru/api/v3/lk/documents/create", token, requestBody.toString());
    }

    private JsonObject buildReqBody(JsonObject jsonDocument, String signature, String type) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("document_format", "MANUAL");
        byte[] bytesEncoded = Base64.encodeBase64(jsonDocument.toString().getBytes());
        requestBody.addProperty("product_document", new String(bytesEncoded));
        bytesEncoded = Base64.encodeBase64(signature.getBytes());
        requestBody.addProperty("signature", new String(bytesEncoded));
        requestBody.addProperty("type", type);
        return requestBody;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

interface RequestBuilder {
    void reset(String uri);

    void setContentType(String contentType);

    void setToken(String token);

    void setEntity(String params) throws UnsupportedEncodingException;
}

class HTTPService {
    public HTTPService() {
    }

    public void connect(String uri, String token, String params) throws IOException {
        Director director = new Director();
        HttpClient httpClient = HttpClientBuilder.create().build();
        GIS_MP_API_Builder requestBuilder = new GIS_MP_API_Builder();
        director.createMinJsonRequestWithToken(requestBuilder, uri, token, params);
        HttpPost request = requestBuilder.getResult();
        httpClient.execute(request);
    }
}

class Director {
    public void createMinJsonRequestWithToken(RequestBuilder requestBuilder, String uri, String token,
                                              String params) throws UnsupportedEncodingException {
        requestBuilder.reset(uri);
        requestBuilder.setContentType("application/json");
        requestBuilder.setToken(token);
        requestBuilder.setEntity(params);
    }
}

class GIS_MP_API_Builder implements RequestBuilder {

    private HttpPost request;

    public HttpPost getResult() {
        return request;
    }

    @Override
    public void setContentType(String contentType) {
        request.setHeader(HttpHeaders.CONTENT_TYPE, contentType);
    }

    @Override
    public void setToken(String token) {
        request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    @Override
    public void setEntity(String params) throws UnsupportedEncodingException {
        StringEntity stringEntity = new StringEntity(params);
        request.setEntity(stringEntity);
    }

    @Override
    public void reset(String uri) {
        request = new HttpPost(uri);
    }
}
