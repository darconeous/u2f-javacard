package com.ledger.u2f;

import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.Util;

/**
 * Simple incrementing counter.
 *
 * This class implements a simple unsigned 32-bit counter
 * that always counts up and cannot be decremented. Once
 * it reaches the value 0xFFFFFFFF, all future operations
 * throw ISOException with a value of ISO7816.SW_FILE_FULL.
 *
 * TODO: This should be rewritten to improve flash wear
 *       leveling behavior and device longevity.
 */
public class Counter {
    private byte[] counter;
    private boolean overflow;

    Counter() {
        counter = new byte[4];
        overflow = false;
    }

    public void inc() {
        boolean carry = false;

        if (overflow) {
            // Game over
            ISOException.throwIt(ISO7816.SW_FILE_FULL);
        }

        JCSystem.beginTransaction();

        for (byte i=0; i<4; i++) {
            short addValue = (i == 0 ? (short)1 : (short)0);
            short val = (short)((short)(counter[(short)(4 - 1 - i)] & 0xff) + addValue);
            if (carry) {
                val++;
            }
            carry = (val > 255);
            counter[(short)(4 - 1 - i)] = (byte)val;
        }

        if (carry) {
            // Game over
            overflow = true;
        }

        JCSystem.commitTransaction();

        if (overflow) {
            ISOException.throwIt(ISO7816.SW_FILE_FULL);
        }
    }

    public short writeValue(byte[] dest, short destOffset) {
        if (overflow) {
            ISOException.throwIt(ISO7816.SW_FILE_FULL);
        }
        return Util.arrayCopyNonAtomic(counter, (short)0, dest, destOffset, (short)4);
    }
}
