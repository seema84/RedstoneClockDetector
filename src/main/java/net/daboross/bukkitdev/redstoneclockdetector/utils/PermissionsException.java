package net.daboross.bukkitdev.redstoneclockdetector.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class PermissionsException extends Exception {

    @Getter
    private final String deniedPermission;

    private static final long serialVersionUID = 1L;
}
