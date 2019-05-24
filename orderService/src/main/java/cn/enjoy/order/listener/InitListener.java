package cn.enjoy.order.listener;

import cn.enjoy.order.utils.LoadBalance;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by VULCAN on 2018/7/28.
 */


public class InitListener implements ServletContextListener {

    private  static final String BASE_SERVICES = "/services";
    private  static final String  SERVICE_NAME="/products";
    private static final String ZK_ADDRESS = "192.168.31.237:2181";
    private static final int SESSION_OUTTIME = 10000;//ms

    private ZkClient zkc;
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            zkc = new ZkClient(ZK_ADDRESS,SESSION_OUTTIME);
            //对父节点添加监听子节点变化。
            zkc.subscribeChildChanges(BASE_SERVICES+SERVICE_NAME, new IZkChildListener() {
                @Override
                public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                    updateServiceList(currentChilds);
                }
            });

            updateServiceList(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void updateServiceList(List<String> currentChilds) {
       try{
           List<String> newServerList = new ArrayList<String>();
           if(currentChilds == null){
               currentChilds = zkc.getChildren(BASE_SERVICES  + SERVICE_NAME);
           }
            for(String subNode:currentChilds) {
               System.out.println("子节点："+subNode);
               String host = zkc.readData(BASE_SERVICES+SERVICE_NAME+"/"+subNode);
               System.out.println("host:"+host);
               newServerList.add(host);
           }
           LoadBalance.SERVICE_LIST = newServerList;
       }catch (Exception e) {
           e.printStackTrace();
       }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
