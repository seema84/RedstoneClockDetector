package net.daboross.bukkitdev.redstoneclockdetector.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class UsageException extends Exception {

    private static final long serialVersionUID = 1L;
    @Getter
    private final String usage;
    @Getter
    private final String message;

}
