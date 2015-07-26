package de.lenic.redsync;

public class RedSyncConfig {

    /* LOAD DELAY */
    private static int loadDelay = 10;

    public static int getLoadDelay(){
        return loadDelay;
    }

    public static void setLoadDelay(int delay){
        loadDelay = delay;
    }


    /* LOCK PLAYER */
    private static boolean lockPlayer = true;

    public static boolean getLockPlayer(){
        return lockPlayer;
    }

    public static void setLockPlayer(boolean status){
        lockPlayer = status;
    }


    /* UPDATE MODE */
    private static boolean updateMode = false;

    public static boolean isUpdateMode(){
        return updateMode;
    }

    public static void setUpdateMode(boolean status){
        updateMode = status;
    }

}
