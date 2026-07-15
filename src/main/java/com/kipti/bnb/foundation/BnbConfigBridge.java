package com.kipti.bnb.foundation;

import com.kipti.bnb.mixin.ConfigChangeAccessor;
import net.createmod.catnip.config.ui.ConfigHelper;

public class BnbConfigBridge {

    public static boolean getPendingBoolean(final String path, final boolean fallback) {
        final ConfigHelper.ConfigChange change = ConfigHelper.changes.get(path);
        if (change != null) {
            return (Boolean) ((ConfigChangeAccessor) change).bits_n_bobs$getValue();
        }
        return fallback;
    }

    public static void setPendingBoolean(final String path, final boolean newValue, final boolean currentPersisted) {
        if (newValue == currentPersisted) {
            ConfigHelper.changes.remove(path);
        } else {
            ConfigHelper.changes.put(path, ConfigChangeAccessor.bits_n_bobs$create(newValue));
        }
    }
}
