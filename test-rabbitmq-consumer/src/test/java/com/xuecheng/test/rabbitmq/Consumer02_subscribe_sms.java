package com.xuecheng.test.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;

/**
 * 邮件发送消费者
 */
public class Consumer02_subscribe_sms {
    //队列名称
    //SMS短信队列
    private static final String QUEUE_INFORM_SMS = "queue_inform_sms";

    //交换机
    private static final String EXCHANGE_FANOUT_INFORM="inform_exchange_fanout";

    public static void main(String[] args) {
        //创建一个与MQ的连接
        ConnectionFactory factory = new ConnectionFactory();
        factory.setVirtualHost("/");
        factory.setPort(5672);
        factory.setHost("localhost");
        factory.setUsername("guest");
        factory.setPassword("guest");

        try {
            //创建一个TCP连接
            Connection connection = factory.newConnection();
            //创建与交换机的通道，每个通道代表一个会话
            Channel channel = connection.createChannel();

            //声明交换机
            //参数:  String exchange, BuiltinExchangeType type
            /**
             * 参数明细:
             *  1、交换机名称
             *  2、交换机类型 fanout、topic、direct、headers
             */
            channel.exchangeDeclare(EXCHANGE_FANOUT_INFORM, BuiltinExchangeType.FANOUT);

            //声明队列
            //参数:  (String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String,  Object> arguments)
            /**
             * 参数明细:
             *   queue: 1、队列名称
             *   durable: 2、是否持久化
             *   exclusive: 3、是否独占此队列
             *   autoDelete: 4、队列不用是否自动删除
             *   arguments: 5、队列参数
             */
            channel.queueDeclare(QUEUE_INFORM_SMS, true, false, false, null);

            //交换机和队列绑定
            /**
             * 参数: String queue, String exchange, String routingKey
             * 参数明细:
             *   queue: 1、队列名称
             *   exchange: 2、交换机名称
             *   routingKey: 3、路由key
             */
            channel.queueBind(QUEUE_INFORM_SMS,EXCHANGE_FANOUT_INFORM,"");

            //定义消费方法
            DefaultConsumer defaultConsumer = new DefaultConsumer(channel){
                /** 消费者接收消息调用此方法
                 * @param consumerTag 消费者的标签，在channel.basicConsume()去指定   
                 * @param envelope 消息包的内容，可从中获取消息id，消息routingkey，交换机，消息和重传标志 (收到消息失败后是否需要重新发送)
                 * @param properties
                 * @param body
                 * @throws IOException
                 */
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    long deliveryTag = envelope.getDeliveryTag();
                    String exchange = envelope.getExchange();
                    //消息内容                 
                    String message = new String(body, "utf-8");
                    System.out.println(message);
                }
            };

            /**         
             *  监听队列
             *  参数: String queue, boolean autoAck,Consumer callback          
             *  参数明细          
             *      1、队列名称         
             *      2、是否自动回复，设置为true为表示消息接收到自动向mq回复接收到了，mq接收到回复会删除消息，设置 为false则需要手动回复          
             *      3、消费消息的方法，消费者接收到消息后调用此方法          
             */
            channel.basicConsume(QUEUE_INFORM_SMS, true, defaultConsumer);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
