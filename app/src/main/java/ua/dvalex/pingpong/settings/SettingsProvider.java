package ua.dvalex.pingpong.settings;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsProvider {
    private static SettingsProvider ourInstance = new SettingsProvider();

    public static SettingsProvider getInstance() {
        return ourInstance;
    }

    private static final String SP_PING_PONG = "SP_PingPong";
    private SharedPreferences sharedPreferences;

    private SettingsProvider() {
    }

    public void setup(Context context) {
        sharedPreferences = context.getSharedPreferences(SP_PING_PONG, Context.MODE_PRIVATE);
    }

    public void set(String key, Object value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (value == null) {
            editor.remove(key);
        } else if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else {
            throw new RuntimeException();
        }
        editor.apply();
    }

    public String getString(String key, String defValue) {
        return sharedPreferences.getString(key, defValue);
    }

    public Long getLong(String key, long defValue) {
        return sharedPreferences.getLong(key, defValue);
    }

    public Boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }
}
