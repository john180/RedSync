package de.lenic.redsync.objects;

public enum DataKey {

    INV("INV"),
    ARMOR("ARMOR"),
    ENDERCHEST("ENDERCHEST"),
    POTION("POTION"),
    LEVEL("LEVEL"),
    EXP("EXP"),
    HEALTH("HEALTH"),
    HUNGER("HUNGER"),
    GAMEMODE("GAMEMODE"),
    FIRETICKS("FIRETICKS"),
    SLOT("SLOT");

    private final String _strings;

    DataKey(String value) {
        _strings = value;
    }

    public String value() {
        return _strings;
    }

}
