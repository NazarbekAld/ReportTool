package me.nazarxexe.job.admintool.impl;

public interface PlayerLockable {

    void lock(String name);
    void unlock(String name);
    boolean isLocked(String name);


}
