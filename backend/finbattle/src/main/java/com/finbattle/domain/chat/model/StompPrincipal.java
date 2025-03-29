package com.finbattle.domain.chat.model;

import java.security.Principal;
import lombok.Getter;

@Getter
public class StompPrincipal implements Principal {

    private final Long memberId;

    public StompPrincipal(Long memberId) {
        this.memberId = memberId;
    }

    @Override
    public String getName() {
        return String.valueOf(memberId);
    }
}
