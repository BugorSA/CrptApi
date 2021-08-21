import com.google.gson.JsonObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CrptApi {

    // TODO: Clean up stale entries
    private final ConcurrentMap<Long, AtomicInteger> windows = new ConcurrentHashMap<>();

    protected final int requestLimit;

    private final TimeUnit timeUnit;

    boolean allow() {
        long windowKey = timeUnit.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        windows.putIfAbsent(windowKey, new AtomicInteger(0));
        return windows.get(windowKey).incrementAndGet() <= requestLimit;
    }

    private String token = "sdsdsdsdsXD";

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.requestLimit = requestLimit;
        this.timeUnit = timeUnit;
    }


    public boolean introduceIntoCirculation(JsonObject jsonDocument, String signature) {
        if (!allow()) {
            return introduceInCirculationUncheck(jsonDocument, signature);
        } else return false;
    }

    private boolean introduceInCirculationUncheck(JsonObject jsonDocument, String signature) {

        HttpClient httpClient = HttpClientBuilder.create().build();

        JsonObject requestBody = buildReqBody(jsonDocument, signature);

        try {

            HttpPost request = new HttpPost("https://ismp.crpt.ru/api/v3/lk/documents/create");

            StringEntity params = new StringEntity(requestBody.toString());
            request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);
            //System.out.println(response.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private JsonObject buildReqBody(JsonObject jsonDocument, String signature) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("document_format", "MANUAL");
        byte[] bytesEncoded = Base64.encodeBase64(jsonDocument.toString().getBytes());
        requestBody.addProperty("product_document", new String(bytesEncoded));
        bytesEncoded = Base64.encodeBase64(signature.getBytes());
        requestBody.addProperty("signature", new String(bytesEncoded));
        requestBody.addProperty("type", "LP_INTRODUCE_GOODS");
        return requestBody;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
