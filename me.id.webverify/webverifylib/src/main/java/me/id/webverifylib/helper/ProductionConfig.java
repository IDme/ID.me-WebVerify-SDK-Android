package me.id.webverifylib.helper;

import android.support.annotation.NonNull;

import me.id.webverifylib.listener.IDmeEnvironmentConfig;

public class ProductionConfig implements IDmeEnvironmentConfig {
    @NonNull
    @Override
    public String getAccessTokenUri() {
        return "https://api.id.me/oauth/token";
    }

    @NonNull
    @Override
    public String getCommonUri() {
        return "https://api.id.me/oauth/authorize";
    }

    @NonNull
    @Override
    public String getLogoutUri() {
        return "https://api.id.me/oauth/logout";
    }

    @NonNull
    @Override
    public String getProfileUri() {
        return "https://api.id.me/api/public/v2/data.json";
    }
}
