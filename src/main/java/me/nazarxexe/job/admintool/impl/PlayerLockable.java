package me.nazarxexe.job.admintool.impl;

public interface PlayerLockable {

    void lock(String name);
    void unlock(String name);

    /**
     *
     * Истина если игрок замарожен.
     * Ложь если игрок не замарожен.
     *
     * @param name
     * @return boolean
     */
    boolean isLocked(String name);


}
