package com.airfranceklm.amt.sidecar.identity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@NoArgsConstructor
class KeySet<TKey extends IdentifiableKey> {
    @Getter @Setter @Singular @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<TKey> keys;

    public TKey getKeyById(String id) {
        if (keys != null) {
            return keys.stream()
                    .filter(c -> id.equals(c.getKeyIdentifier().getKeyId()))
                    .findAny()
                    .orElse(null);
        } else {
            return null;
        }
    }

    public TKey getKeyByType(KeyType type) {
        if (keys != null && type != null) {
            return keys.stream()
                    .filter(c -> c.getKeyIdentifier() != null && type.getType().equals(c.getKeyIdentifier().getType()))
                    .findAny()
                    .orElse(null);
        } else {
            return null;
        }
    }

    public TKey getKey(String id, KeyType type) {
        if (keys != null) {
            return keys.stream()
                    .filter(c -> {
                        KeyIdentifier kid = c.getKeyIdentifier();
                        return kid != null && (Objects.equals(id, kid.getKeyId()) && Objects.equals(type, kid.getKeyType()));
                    })
                    .findAny()
                    .orElse(null);
        } else {
            return null;
        }
    }

    public void addKey(TKey pkd) {
        if (this.keys == null) {
            this.keys = new ArrayList<>();
        }
        this.keys.add(pkd);
    }
}
