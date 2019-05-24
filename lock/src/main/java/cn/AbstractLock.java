package cn;

public abstract class AbstractLock {

    public void getLock(){
        //尝试获取锁资源
        if(tryLock()){
            System.out.println("##获取lock锁资源##");
        }else{
            //等待
            waitLock();
            //重新获取锁资源
            getLock();
        }
    }

    //尝试获取锁
    protected abstract boolean tryLock();

    //等待
    public abstract void waitLock();

}
