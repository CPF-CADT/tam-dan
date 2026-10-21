package com.tamdan.exception;

public class ReplayedPairingCodeException extends BadRequestException {

    public ReplayedPairingCodeException() {
        super("Pairing code is no longer usable");
    }
}
