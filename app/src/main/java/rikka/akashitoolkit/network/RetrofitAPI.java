package rikka.akashitoolkit.network;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import rikka.akashitoolkit.model.Avatars;
import rikka.akashitoolkit.model.CheckUpdate;
import rikka.akashitoolkit.model.Event;
import rikka.akashitoolkit.model.LatestAvatar;
import rikka.akashitoolkit.model.ShipVoice;
import rikka.akashitoolkit.model.Twitter;
import rikka.akashitoolkit.model.Seasonal;

/**
 * Created by Rikka on 2016/3/8.
 */
public class RetrofitAPI {
    public interface TwitterService {
        @GET("/")
        Call<Twitter> get(
                @Query("json") int json,
                @Query("count") int count);

        @GET("/avatar/latest")
        Call<LatestAvatar> getLatestAvatarUrl();

        @GET("/avatars")
        Call<Avatars> getAvatars();
    }

    public interface CheckUpdateService {
        @GET("/Akashi/info.php")
        Call<CheckUpdate> get(
                @Query("api_version") int api_version,
                @Query("channel") int channel);

        @GET("/Akashi/json/{filename}")
        @Streaming
        Call<ResponseBody> download(@Path("filename") String filename);
    }

    public interface SeasonalAPI {
        @GET("/Akashi/seasonal.php")
        Call<List<Seasonal>> get(
                @Query("api_version") int api_version);
    }

    public interface EventAPI {
        @GET("/Akashi/event_list.php")
        Call<List<String>> getList(
                @Query("api_version") int api_version);

        @GET("/Akashi/event.php")
        Call<Event> get(
                @Query("api_version") int api_version);
    }

    public interface Voice {
        @GET("subtitle/detail/{shipId}")
        Call<List<ShipVoice>> get(
                @Path("shipId") int shipId);
    }

}
