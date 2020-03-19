package com.ledger.u2f;

/**
 * Null user presence detection.
 * Suitable for testing only.
 */
public class NullPresence implements Presence {
    @Override
    public byte enforce_user_presence() {
        return FLAG_USER_PRESENT;
    }

    @Override
    public byte check_user_presence() {
        return 0;
    }
}
