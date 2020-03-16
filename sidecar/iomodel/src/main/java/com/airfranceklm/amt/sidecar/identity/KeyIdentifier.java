package com.airfranceklm.amt.sidecar.identity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KeyIdentifier {
    @JsonProperty("kid") @Getter @Setter
    private String keyId;

    @JsonProperty("kty") @Getter
    private String type;

    @Getter @JsonIgnore
    KeyType keyType;

    public KeyIdentifier(String keyId, String type) {
        this.keyId = keyId;
        this.type = type;

        if (type != null) {
            this.keyType = new KeyType(type);
        }
    }

    public KeyIdentifier(String keyId, KeyType keyType) {
        this.keyId = keyId;
        this.keyType = keyType;
        if (this.keyType != null) {
            this.type = keyType.getType();
        }
    }

    public static KeyIdentifier typedKey(String type) {
        return new KeyIdentifier(null, type);
    }


    public static KeyIdentifier typedKey(@NonNull KeyType type) {
        return new KeyIdentifier(null, type);
    }

    public static KeyIdentifier identifiedKey(String id) {
        return new KeyIdentifier(id, (String)null);
    }

    public void setType(String type) {
        this.type = type;
        this.keyType = new KeyType(type);
    }
}
