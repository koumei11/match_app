package jp.gr.java_conf.datingapp.notification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA0Qg4Swk:APA91bFzRASU5F594XY0bXRZd59mnvtSW380XznBMHfhePygrbz2fE9I8gyl5VjGny0TdN61rRn3vrb26xzGRnLNfpOWXPlN2vJfyLpptmXtH5AS3fpwOIAIIqmKuNVRYYAOFjiJ6jPt"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
