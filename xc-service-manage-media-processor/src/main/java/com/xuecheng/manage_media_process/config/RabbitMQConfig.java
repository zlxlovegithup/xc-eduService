package com.xuecheng.manage_media_process.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EX_MEDIA_PROCESSTASK ="ex_media_processor";

    //视频处理队列
    @Value("${xc-service-manage-media.mq.queue-media-video-processor}")
    public String queue_media_video_processtask;

    //视频处理路由
    @Value("${xc-service-manage-media.mq.routingkey-media-video}")
    public String routingkey_media_video;

    //消费者并发队列
    public static final int DEFAULT_CONCURRENT = 10;

    /**
     * 交换机配置
     * @return
     */
    @Bean(EX_MEDIA_PROCESSTASK)
    public Exchange EX_MEDIA_VIDEOTASK() {
        return ExchangeBuilder.directExchange(EX_MEDIA_PROCESSTASK).durable(true).build();
    }

    /**
     * 声明队列
     * @return
     */
    @Bean("queue_media_video_processtask")
    public Queue QUEUE_PROCESSTASK(){
        Queue queue = new Queue(queue_media_video_processtask,true,false,true);
        return queue;
    }

    /**
     * 绑定队列到交换机
     * @return
     */
    @Bean
    public Binding building_queue_media_processtask(@Qualifier("queue_media_video_processtask")Queue queue,@Qualifier(EX_MEDIA_PROCESSTASK)Exchange exchange){
        return BindingBuilder.bind(queue)
                             .to(exchange)
                             .with(routingkey_media_video)
                             .noargs();
    }

    /**
     * 配置mq的容器工厂参数，增加并发处理数量即可实现多线程处理监听队列，实现多线程处理消息
     * @param configurer
     * @param connectionFactory
     * @return
     */
    @Bean("customContainerFactory")
    public SimpleRabbitListenerContainerFactory containerFactory(SimpleRabbitListenerContainerFactoryConfigurer configurer,
                                                                 ConnectionFactory connectionFactory){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConcurrentConsumers(DEFAULT_CONCURRENT);
        factory.setMaxConcurrentConsumers(DEFAULT_CONCURRENT);
        configurer.configure(factory,connectionFactory);
        return factory;
    }

}
