import com.lagou.edu.SpringConfig;
import com.lagou.edu.dao.AccountDao;

import com.lagou.edu.pojo.Account;
import com.lagou.edu.service.TransferService;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;


import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;


/**
 * @author 应癫
 */
public class IoCTest {


    @Test
    public void testIoC() throws Exception {

        // 通过读取classpath下的xml文件来启动容器（xml模式SE应用下推荐）
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(SpringConfig.class);



        AccountDao accountDao = (AccountDao) applicationContext.getBean("accountDao");
        TransferService transferService = (TransferService) applicationContext.getBean("transferService");
        Account abc = accountDao.queryAccountByCardNo("abc");
        Account def = accountDao.queryAccountByCardNo("def");
        System.out.println("转账前abc money="+String.valueOf(abc.getMoney()));
        System.out.println("转账前def money="+String.valueOf(def.getMoney()));
        try {
            transferService.transfer("abc","def",100);
        }catch (Exception e){
            System.out.println("转账发生异常");
        }

        System.out.println("转账后abc money="+String.valueOf(abc.getMoney()));
        System.out.println("转账后def money="+String.valueOf(def.getMoney()));



    }

}
