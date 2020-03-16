package com.airfranceklm.amt.sidecar.dsl;

public interface ReplyCommandDSL extends PayloadCreatorDSL {

    ReplyCommandDSL statusCode(int code);
}
