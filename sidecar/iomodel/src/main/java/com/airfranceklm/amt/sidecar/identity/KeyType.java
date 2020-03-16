package com.airfranceklm.amt.sidecar.identity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Key type, to make sure that the application wouldn't confuse key types for key ids.
 */
@EqualsAndHashCode
public class KeyType {
    @Getter
    @Setter
    private String type;

    public KeyType(@NonNull String type) {
        this.type = type;
    }
}
