package com.ledger.u2f;

import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;

/**
 * User presence detection that works based on the
 * presentation of the token rather than an interaction
 * with the token.
 *
 * This means that, for a presented token,
 * only a single operation requiring user presence can
 * be performed until the card is reset.
 *
 * This is suitable for NFC-based authenticators, and
 * USB authenticators that don't have buttons.
 *
 * Note that in many cases the card can be reset by
 * software without any physical user action, rendering
 * this protection moot. Without a physical button this
 * is the best that can be done.
 */
public class OneShotPresence implements Presence {
    private byte[] did_verify_flag;

    OneShotPresence() {
        did_verify_flag = JCSystem.makeTransientByteArray((short)1, JCSystem.CLEAR_ON_RESET);
    }

    @Override
    public byte enforce_user_presence() {
        byte presence = check_user_presence();

        if ((presence & FLAG_USER_PRESENT) != FLAG_USER_PRESENT) {
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }

        return presence;
    }

    @Override
    public byte check_user_presence() {
        if (did_verify_flag[0] != 0) {
            return 0;
        }
        did_verify_flag[0] = 1;
        return FLAG_USER_PRESENT;
    }
}
