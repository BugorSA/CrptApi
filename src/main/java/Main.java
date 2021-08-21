import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        final int MAX_REQUESTS_PER_SEC = 3;
        CrptApi crptApi = new CrptApi(TimeUnit.SECONDS, MAX_REQUESTS_PER_SEC);
        crptApi.setToken("dont worry 12345 #$%");

        Thread requestThread = new Thread(() -> {
            sendRequest(crptApi, 10, 1);
            sendRequest(crptApi, 20, 2);
            sendRequest(crptApi, 50, 5);
            sendRequest(crptApi, 100, 10);
            sendRequest(crptApi, 200, 20);
            sendRequest(crptApi, 250, 25);
            sendRequest(crptApi, 500, 50);
            sendRequest(crptApi, 1000, 100);
        });

        requestThread.start();
        requestThread.join();
    }

    private static void sendRequest(CrptApi crptApi, int totalCnt, int requestPerSec) {
        long startTime = System.currentTimeMillis();
        CountDownLatch doneSignal = new CountDownLatch(totalCnt);
        for (int i = 0; i < totalCnt; i++) {
            try {
                new Thread(() -> {
                    while (true) {
                        try {
                            if (!crptApi.introduceIntoCirculation(new JsonObject(), "asfa")) break;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            TimeUnit.MILLISECONDS.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    doneSignal.countDown();
                }).start();
                TimeUnit.MILLISECONDS.sleep(1000 / requestPerSec);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        double duration = (System.currentTimeMillis() - startTime) / 1000.0;
        System.out.println(totalCnt + " requests processed in " + duration + " seconds. "
                + "Rate: " + (double) totalCnt / duration + " per second");
    }
}