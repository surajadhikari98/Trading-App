package io.reactivestax.utils;

public class Utility {

    private Utility(){}

    public static boolean checkValidity(String[] split) {
        return (split[0] != null && split[1] != null && split[2] != null && split[3] != null && split[4] != null && split[5] != null);
    }
}
