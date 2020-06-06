package com.xuecheng.test.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;

public class Consumer01 {

    //队列名称
    private static final String QUEUE = "helloworld";

    /**
     * 1）创建连接
     * 2）创建通道
     * 3）声明队列
     * 4）监听队列
     * 5）接收消息
     * @param args
     */
    public static void main(String[] args) {
        ConnectionFactory factory = new ConnectionFactory();
        //设置MabbitMQ所在服务器的ip和端口
        factory.setHost("localhost");
        factory.setPort(5672);
        //设置用户名
        factory.setUsername("guest");
        //设置密码
        factory.setPassword("guest");
        //rabbitmq默认虚拟机名称为“/”，虚拟机相当于一个独立的mq服务器
        factory.setVirtualHost("/");


        Connection connection = null;
        Channel channel = null;
        try {
            //1）创建连接
            connection = factory.newConnection();
            //2）创建通道
            channel = connection.createChannel();
            //3）声明队列
            channel.queueDeclare(QUEUE,true,false,false,null);
            //5）接收消息
            //定义消费方法
            DefaultConsumer consumer = new DefaultConsumer(channel) {
             /**              
              * 消费者接收消息调用此方法              
              *  @param consumerTag 消费者的标签，在channel.basicConsume()去指定              
              *  @param envelope 消息包的内容，可从中获取消息id，消息routingkey，交换机，消息和重传标志 (收到消息失败后是否需要重新发送)              
              *  @param properties              
              *  @param body              
              *  @throws IOException
              */
             @Override
             public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                 //交换机
                 String exchange = envelope.getExchange();
                 //路由key
                 String routingKey = envelope.getRoutingKey();
                 //消息id
                 long deliveryTag = envelope.getDeliveryTag();
                 //消息内容
                 String msg = new String(body,"utf-8");
                 System.out.println("receive message.." + msg);
             }
            };

            //4）监听队列
            /**          
             * 监听队列String queue, boolean autoAck,Consumer callback          
             * 参数明细          
             * queue:    1、队列名称          
             * autoAck:  2、是否自动回复，设置为true为表示消息接收到自动向mq回复接收到了，mq接收到回复会删除消息，设置 为false则需要手动回复          
             * callback: 3、消费消息的方法，消费者接收到消息后调用此方法          
             */
            channel.basicConsume(QUEUE, true, consumer);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
