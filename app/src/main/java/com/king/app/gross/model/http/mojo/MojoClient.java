package com.king.app.gross.model.http.mojo;

import android.os.Build;

import com.king.app.gross.model.http.BaseHttpClient;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;

/**
 * Created by Administrator on 2016/9/5.
 */
public class MojoClient extends BaseHttpClient {

    private MojoService service;

    private MojoClient() {
        super();
    }

    @Override
    protected void extendBuilder(OkHttpClient.Builder builder) {
        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                request = request.newBuilder()
                        .header("User-Agent", getLocalUserAgent())
                        .build();
                return chain.proceed(request);
            }
        });
    }

    @Override
    protected void createService(Retrofit retrofit) {
        service = retrofit.create(MojoService.class);
    }

    public String getLocalUserAgent() {
        StringBuffer buffer = new StringBuffer();

        final String version = Build.VERSION.RELEASE;
        if (version.length() > 0) {
            if (Character.isDigit(version.charAt(0))) {
                buffer.append(version);
            }
            else {
                buffer.append("4.1.1");
            }
        }
        else {
            buffer.append("1.0");
        }
        buffer.append("; ");
        Locale locale = Locale.getDefault();
        final String language = locale.getLanguage();
        if (language != null) {
            buffer.append(convertObsoleteLanguageCodeToNew(language));
            String country = locale.getCountry();
            if (country != null) {
                buffer.append("-");
                buffer.append(country.toLowerCase());
            }
        }
        else {
            buffer.append("en");
        }
        buffer.append(";");
        if ("REL".equals(Build.VERSION.CODENAME)) {
            String model = Build.MODEL;
            buffer.append(" ");
            buffer.append(model);
        }
        String id = Build.ID;
        if (id.length() > 0) {
            buffer.append(" Build/");
            buffer.append(id);
        }
        String mobile = "Mobile ";
        String base = "Mozilla/5.0 (Linux; U; Android %s) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 %s Safara/534.30";
        return String.format(base, buffer, mobile);
    }

    private static String convertObsoleteLanguageCodeToNew(String lan) {
        if (lan == null) {
            return null;
        }
        if ("iw".equals(lan)) {
            return "he";
        }
        else if ("in".equals(lan)) {
            return "id";
        }
        else if ("ji".equals(lan)) {
            return "yi";
        }
        return lan;
    }

    //在访问HttpMethods时创建单例
    private static class SingletonHolder{
        private static final MojoClient INSTANCE = new MojoClient();
    }

    //获取单例
    public static MojoClient getInstance(){
        return SingletonHolder.INSTANCE;
    }

    public MojoService getService() {
        return service;
    }
}
