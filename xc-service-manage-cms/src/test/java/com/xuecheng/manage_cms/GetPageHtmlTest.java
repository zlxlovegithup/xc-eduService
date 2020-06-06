package com.xuecheng.manage_cms;

import com.xuecheng.manage_cms.service.PageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class GetPageHtmlTest {

    @Autowired
    PageService pageService;

    @Test
    public void testGetPageHtml(){
        String html = pageService.getPageHtml("5e6df21a083c89ba84a0442e");
        System.out.println(html);
    }

}
