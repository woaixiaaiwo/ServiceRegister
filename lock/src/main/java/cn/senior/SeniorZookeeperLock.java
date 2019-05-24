package cn.senior;

import cn.AbstractLock;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class SeniorZookeeperLock extends AbstractLock {

    private static String ZK_SERVER = "192.168.31.237:2181";
    private static int SESSION_TIMEOUT = 5000;
    private static String LOCK_PATH = "/lock2";
    private CountDownLatch countDownLatch;

    private static ZkClient zkClient = new ZkClient(ZK_SERVER,SESSION_TIMEOUT);

    private String beforePath;
    private String currentPath;

    public SeniorZookeeperLock(){
        if(!zkClient.exists(LOCK_PATH)){
            zkClient.createEphemeral(LOCK_PATH);
        }
    }



    @Override
    protected boolean tryLock() {
        //如果currentPath为空则为第一次尝试加锁，第一次加锁赋值currentPath
        if(currentPath == null || currentPath.length() <= 0){
            currentPath = zkClient.createEphemeralSequential(LOCK_PATH+"/","lock");
        }
        //获取LOCK_PATH子节点
        List<String> children = zkClient.getChildren(LOCK_PATH);
        Collections.sort(children);
        //当前节点是第一个，获取到锁
        if(currentPath.equals(LOCK_PATH + '/'+children.get(0))){
            return true;
        }else{
            //如果当前节点在所有节点中排名中不是排名第一，则获取前面的节点名称，并赋值给beforePath
            if(beforePath == null){
                //获取前一个数据
                int index = Collections.binarySearch(children,currentPath.substring(7));
                beforePath = LOCK_PATH+"/"+children.get(index-1);
            }
        }
        return false;
    }

    @Override
    public void waitLock() {

        IZkDataListener dataListener = new IZkDataListener() {
            @Override
            public void handleDataChange(String dataPath, Object data) throws Exception {
                if(countDownLatch != null){
                    countDownLatch.countDown();
                }
            }

            @Override
            public void handleDataDeleted(String dataPath) throws Exception {
            }
        };
        //给排在前面的的节点增加数据删除的watcher,本质是启动另外一个线程去监听前置节点
        zkClient.subscribeDataChanges(beforePath, dataListener);

        if(zkClient.exists(beforePath)){
            countDownLatch = new CountDownLatch(1);
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        zkClient.unsubscribeDataChanges(beforePath, dataListener);

    }

    public void unLock() {
        //删除当前临时节点
        zkClient.delete(currentPath);
        zkClient.close();
    }

}
