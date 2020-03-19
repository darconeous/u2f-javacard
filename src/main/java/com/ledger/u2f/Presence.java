package com.ledger.u2f;

public interface Presence {
    /** A user is present.
     *
     * This means that someone interacted with the
     * authenticator while performing this operation.
     */
    byte FLAG_USER_PRESENT = (byte)0x01;

    /** The user was verified. From webauthn.
     *
     * This means that the user has verified themselves
     * to the authenticator using, for example, their
     * fingerprint or a PIN.
     */
    byte FLAG_USER_VERIFIED = (byte)0x04;

    /**
     * Waits for user presence, throwing an exception if that
     * is impossible or if there is a timeout.
     *
     * @return the value of the user presence byte
     *         in the FIDO U2F signature response.
     */
    byte enforce_user_presence();

    /**
     * Checks user presence. Should not throw an exception.
     * Returns immediately.
     *
     * @return the value of the user presence byte
     *         in the FIDO U2F signature response.
     */
    byte check_user_presence();
}
