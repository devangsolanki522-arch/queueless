package com.queueless.app;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME =
            "QueueLessSession";

    private static final String KEY_TOKEN =
            "token";

    private static final String KEY_ROLE =
            "role";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {

        preferences =
                context.getSharedPreferences(
                        PREF_NAME,
                        Context.MODE_PRIVATE
                );
    }

    public void saveToken(String token) {

        preferences.edit()
                .putString(KEY_TOKEN, token)
                .apply();
    }

    public String getToken() {

        return preferences.getString(
                KEY_TOKEN,
                null
        );
    }

    public void saveRole(String role) {

        preferences.edit()
                .putString(KEY_ROLE, role)
                .apply();
    }

    public String getRole() {

        return preferences.getString(
                KEY_ROLE,
                null
        );
    }

    public void clearSession() {

        preferences.edit()
                .clear()
                .apply();
    }
}