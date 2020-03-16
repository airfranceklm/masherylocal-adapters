package com.airfranceklm.amt.sidecar.model;

import java.util.Map;

/**
 * Termination command.
 */
public interface TerminateCommand extends ReplyCommand {
    String getMessage();
}
