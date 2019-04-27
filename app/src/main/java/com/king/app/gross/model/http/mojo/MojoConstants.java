package com.king.app.gross.model.http.mojo;

public class MojoConstants {

    public static final String MOJO_URL = "https://www.boxofficemojo.com/movies/";
    /**
     * https://www.boxofficemojo.com/movies/?page=intl&id=avatar.htm
     */
    public static final String FOREIGN_URL = MOJO_URL + "?page=intl&id=";
    /**
     * https://www.boxofficemojo.com/movies/?page=daily&view=chart&id=marvel2018a.htm
     */
    public static final String DAILY_URL = MOJO_URL + "?page=daily&view=chart&id=";
    public static final String URL_END = ".htm";
}
