package com.ledger.u2f;

/**
 * Null user presence detection.
 * Suitable for testing only.
 */
public class NullPresence implements Presence {
    @Override
    public void verify_user_presence() {
    }
}
