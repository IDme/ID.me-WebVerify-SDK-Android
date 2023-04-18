package me.id.webverifylib.listener;

import android.support.annotation.NonNull;

public interface IDmeEnvironmentConfig {
    @NonNull String getAccessTokenUri();
    @NonNull String getCommonUri();
    @NonNull String getLogoutUri();
    @NonNull String getProfileUri();
}
