package com.airfranceklm.amt.sidecar.dsl;

import com.airfranceklm.amt.sidecar.model.json.JsonReplyCommand;

class ReplyCommandDSLImpl
        extends PayloadCreatorDSLImpl<JsonReplyCommand>
        implements ReplyCommandDSL {

    public ReplyCommandDSLImpl(JsonReplyCommand target) {
        super(target);
    }

    @Override
    public ReplyCommandDSLImpl statusCode(int code) {
        target.setStatusCode(code);
        return this;
    }
}
