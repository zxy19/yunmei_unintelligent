package cc.xypp.yunmeiui.utils;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cc.xypp.yunmeiui.eneity.Lock;

public class LockManageUtil {
    SecureStorage ssp;
    public LockManageUtil(Context context){
        ssp = new SecureStorage(context);
    }
    public List<Lock> getAll() {
        Set<String> lockSet = ssp.getVal("locks", new HashSet<>());
        List<Lock> locks = new ArrayList<>();
        lockSet.forEach(v -> {
            locks.add(new Lock(v));
        });
        return locks;
    }
    public Lock getDef(){
        String lockDat = ssp.getVal("lock_default", "");
        if(lockDat.equals(""))return null;
        return new Lock(lockDat);
    }
    public void add(Lock lock) throws RuntimeException{
        List<Lock> locks = getAll();
        for (Lock existLock : locks) {
            if(existLock.label.equals(lock.label)){
                throw new RuntimeException(String.format("%s 已存在",lock.label));
            }
        }
        Set<String> finLocks = new HashSet<>();
        locks.forEach(lock1 -> finLocks.add(lock1.toString()));
        finLocks.add(lock.toString());
        ssp.setVal("locks",finLocks);
    }
    public void remove(Lock lock) throws RuntimeException{
        remove(lock.label);
    }
    public void remove(String label) throws RuntimeException{
        List<Lock> locks = getAll();
        Set<String> finLocks = new HashSet<>();
        boolean exi = false;
        for (Lock existLock : locks) {
            if(existLock.label.equals(label)){
                exi = true;
            }else{
                finLocks.add(existLock.toString());
            }
        }
        if(!exi){
            throw new RuntimeException("未找到门锁");
        }
        ssp.setVal("locks",finLocks);
    }
    public void setMac(String label,String mac){
        List<Lock> locks = getAll();
        Set<String> finLocks = new HashSet<>();
        boolean exi = false;
        for (Lock existLock : locks) {
            if(existLock.label.equals(label)){
                exi = true;
                existLock.D_Mac=mac;
            }
            finLocks.add(existLock.toString());

        }
        if(!exi){
            throw new RuntimeException("未找到门锁");
        }
        ssp.setVal("locks",finLocks);
    }

    public void setDef(Lock o) {
        if(o == null){
            ssp.setVal("lock_default","");
        }else {
            ssp.setVal("lock_default", o.toString());
        }
    }
}
