package cn.org.tpeach.nosql.lettuce;

import cn.org.tpeach.nosql.ApplicationTest;
import cn.org.tpeach.nosql.enums.RedisStructure;
import cn.org.tpeach.nosql.exception.ServiceException;
import cn.org.tpeach.nosql.framework.LarkFrame;
import cn.org.tpeach.nosql.redis.bean.RedisConnectInfo;
import cn.org.tpeach.nosql.redis.command.GetRedisLarkContextCommand;
import cn.org.tpeach.nosql.redis.command.RedisLarkContext;
import cn.org.tpeach.nosql.redis.command.RedisPubSubClientAdapt;
import cn.org.tpeach.nosql.redis.command.connection.PingCommand;
import cn.org.tpeach.nosql.redis.command.hash.HmSetHash;
import cn.org.tpeach.nosql.redis.command.list.RpushList;
import cn.org.tpeach.nosql.redis.command.pubsub.PsubscribeCommand;
import cn.org.tpeach.nosql.redis.command.pubsub.PubSubListenerComand;
import cn.org.tpeach.nosql.redis.command.pubsub.PublishCommand;
import cn.org.tpeach.nosql.redis.command.set.SAddSet;
import cn.org.tpeach.nosql.redis.command.string.SetnxString;
import cn.org.tpeach.nosql.redis.command.zset.ZmAddSet;
import cn.org.tpeach.nosql.redis.connection.RedisLark;
import cn.org.tpeach.nosql.redis.connection.RedisLarkPool;
import cn.org.tpeach.nosql.tools.DateUtils;
import cn.org.tpeach.nosql.tools.StringUtils;
import io.lettuce.core.ScoredValue;
import io.lettuce.core.api.StatefulRedisConnection;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.*;

public class RedisTest {
    static String id = "0e5f3c61-488e-4d3c-9df0-5f130416ac93";

    public static LinkedList<String> ids = new LinkedList<>();
    static {
        ids.add(id);
//        for (int i = 1; i < 50; i++) {
//            ids.add(StringUtils.getUUID());
//        }
    }
    /**
     *
     */
    @Before
    public void connectSingle(){
        LarkFrame.run(ApplicationTest.class);
        for (String s : ids) {

            //构建连接信息
            RedisConnectInfo conn = new RedisConnectInfo();
            conn.setId(s);
//            conn.setStructure(RedisStructure.CLUSTER.getCode());
//            conn.setHost("127.0.0.1:7000,127.0.0.1:7001,127.0.0.1:7002,127.0.0.1:7003,127.0.0.1:7004,127.0.0.1:7005");
            conn.setStructure(RedisStructure.SINGLE.getCode());
            conn.setHost("127.0.0.1");
            conn.setPort(6379);
            conn.setAuth("123456");
            RedisLarkPool.addOrUpdateConnectInfo(conn);
            String ping = new PingCommand(id).execute();
            if(!"PONG".equals(ping)){
                throw new ServiceException("Ping命令执行失败");
            }
            System.out.println("连接成功");
        }

    }
    @Test
    public void testList()   {
        try {
            int index = 0;
            byte[][] list = new byte[100000][];
            for (int i = 0; i < 100000; i++) {
                list[i] = ("测试" + index).getBytes("UTF-8");
                index++;
                try {
//				TimeUnit.MICROSECONDS.sleep(100);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            new RpushList(id, 0, "list".getBytes("UTF-8"), list).execute();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Test
    public void testString(){
        for (int i = 0; i < 10000; i++) {

//                    System.out.println("测试bigger_" +i);

            try {
//				TimeUnit.MICROSECONDS.sleep(100);
                SetnxString setnxString = new SetnxString(id, 6, ("测试" + i).getBytes("UTF-8"), (i + "").getBytes("UTF-8"));
                setnxString.setPrintLog(false);
                setnxString.execute();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
    @Test
    public void testBatchString(){
        int num = 100000000 /50;
        ExecutorService executorService = Executors.newCachedThreadPool();
        CountDownLatch countDownLatch = new CountDownLatch(50);
        for(int x = 0;x<50;x++){
            int finalX = x;
            String remove = ids.removeFirst();
            executorService.execute(()->{
                try {
                    for (int i = finalX * num; i < (finalX + 1) * num; i++) {

//                    System.out.println("测试bigger_" +i);

                        try {
//				TimeUnit.MICROSECONDS.sleep(100);
                            SetnxString setnxString = new SetnxString(remove, 5, ("测试很珍惜" + i).getBytes("UTF-8"), (i + "").getBytes("UTF-8"));
                            setnxString.setPrintLog(false);
                            setnxString.execute();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }finally {
                    countDownLatch.countDown();
                }
            });

        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    @Test
    public void testSet(){
        try {
            byte[][] set = new byte[100000][];
            int index = 0;
            for (int i = 0; i < 100000; i++) {
                set[i] = ("测试set" +index).getBytes("UTF-8");
                index++;
            }
            new SAddSet(id,0,"set".getBytes("UTF-8"),set).execute();
        } catch (  Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    @Test
    public void testHash(){
        try {
            Map<byte[], byte[]> hash = new HashMap<>();
            int index = 0;
            for (int i = 0; i < 100000; i++) {
                hash.put(("hash" + index).getBytes("UTF-8"), ("测试hash" + index).getBytes("UTF-8"));
                index++;
            }

            new HmSetHash(id, 0, "hash".getBytes("UTF-8"), hash).execute();
        } catch (  Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    @Test
    public void testZset(){
        try {
            ScoredValue<byte[]>[] scoreValues = new ScoredValue[100000];
            int index = 0;
            for (int i = 0; i < 100000; i++) {
                scoreValues[i] = ScoredValue.fromNullable(index, ("测试zset" + index).getBytes("UTF-8"));
                index++;
            }


            new ZmAddSet(id, 0, "zset".getBytes("UTF-8"), scoreValues).execute();
        } catch (  Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testPubSub(){
        byte[] chanel = StringUtils.strToByte("redisMsg");
        byte[] chanel2 = StringUtils.strToByte("redisMsg1");
        PsubscribeCommand redisMeg = new PsubscribeCommand(id, chanel);
        redisMeg.execute();
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);

        PubSubListenerComand pubSubListenerComand = new PubSubListenerComand(id, new RedisPubSubClientAdapt<byte[], byte[]>() {

            @Override
            public void message(byte[] channel, byte[] message) {
                System.out.println("message(byte[] channel, byte[] message) ");
            }

            @Override
            public void message(byte[] pattern, byte[] channel, byte[] message) {
                System.out.println(StringUtils.byteToStr(message));
            }

            @Override
            public void subscribed(byte[] channel, long count) {
                System.out.println("subscribed(byte[] channel, long count) ");
            }

            @Override
            public void psubscribed(byte[] pattern, long count) {
                System.out.println("psubscribed(byte[] pattern, long count) ");
            }

            @Override
            public void unsubscribed(byte[] channel, long count) {
                System.out.println("unsubscribed(byte[] channel, long count) ");
            }

            @Override
            public void punsubscribed(byte[] pattern, long count) {
                System.out.println("punsubscribed(byte[] pattern, long count) ");
            }
        });
        pubSubListenerComand.execute();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        executorService.scheduleAtFixedRate(()->{
            new PublishCommand(id,chanel,StringUtils.strToByte("发送消息："+ DateUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss"))).execute();
            new PublishCommand(id,chanel2,StringUtils.strToByte("你好")).execute();
            },10,10, TimeUnit.SECONDS);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void testGetConnect(){
        RedisLarkContext<byte[], byte[]> larkContext = new GetRedisLarkContextCommand(id).execute();
        RedisLark<byte[], byte[]> redisLark = larkContext.getRedisLark();
        StatefulRedisConnection statefulConnection = (StatefulRedisConnection) redisLark.getStatefulConnection();

    }
}
