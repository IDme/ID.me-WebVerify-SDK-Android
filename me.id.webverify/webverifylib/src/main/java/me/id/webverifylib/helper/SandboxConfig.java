package me.id.webverifylib.helper;

import android.support.annotation.NonNull;

import me.id.webverifylib.listener.IDmeEnvironmentConfig;

public class SandboxConfig implements IDmeEnvironmentConfig {
    @NonNull
    @Override
    public String getAccessTokenUri() {
        return "https://api.idmelabs.com/oauth/token";
    }

    @NonNull
    @Override
    public String getCommonUri() {
        return "https://api.idmelabs.com/oauth/authorize";
    }

    @NonNull
    @Override
    public String getLogoutUri() {
        return "https://api.idmelabs.com/oauth/logout";
    }

    @NonNull
    @Override
    public String getProfileUri() {
        return "https://api.idmelabs.com/api/public/v2/data.json";
    }
}
