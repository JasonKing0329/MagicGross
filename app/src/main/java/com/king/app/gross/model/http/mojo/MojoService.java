package com.king.app.gross.model.http.mojo;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Desc:fetch data from Mojo website
 *
 * @author：Jing Yang
 * @date: 2019/4/21 18:49
 */
public interface MojoService {

    @GET
    Observable<ResponseBody> getMovieForeignPage(@Url String url);
}
