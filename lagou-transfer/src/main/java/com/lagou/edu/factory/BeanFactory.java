package com.lagou.edu.factory;

import com.lagou.edu.annotation.Autowired;
import com.lagou.edu.annotation.Service;
import com.lagou.edu.annotation.Transactional;
import com.lagou.edu.utils.AnnotationUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author 应癫
 *
 * 工厂类，生产对象（使用反射技术）
 */
public class BeanFactory {

    /**
     * 任务一：读取解析xml，通过反射技术实例化对象并且存储待用（map集合）
     * 任务二：对外提供获取实例对象的接口（根据id获取）
     */

    private static Map<String,Object> map = new HashMap<>();  // 存储对象


    static {
        // 任务一：读取解析xml，通过反射技术实例化对象并且存储待用（map集合）
        // 加载xml
        InputStream resourceAsStream = BeanFactory.class.getClassLoader().getResourceAsStream("beans.xml");
        // 解析xml
        SAXReader saxReader = new SAXReader();
        try {
            Document document = saxReader.read(resourceAsStream);
            Element rootElement = document.getRootElement();
            List<Element> beanList = rootElement.selectNodes("//bean");
            for (int i = 0; i < beanList.size(); i++) {
                Element element =  beanList.get(i);
                // 处理每个bean元素，获取到该元素的id 和 class 属性
                String id = element.attributeValue("id");        // accountDao
                String clazz = element.attributeValue("class");  // com.lagou.edu.dao.impl.JdbcAccountDaoImpl
                // 通过反射技术实例化对象
                Class<?> aClass = Class.forName(clazz);
                Object o = aClass.newInstance();  // 实例化之后的对象

                // 存储到map中待用
                map.put(id,o);

            }

            // 实例化完成之后维护对象的依赖关系，检查哪些对象需要传值进入，根据它的配置，我们传入相应的值
            // 有property子元素的bean就有传值需求
            List<Element> propertyList = rootElement.selectNodes("//property");
            // 解析property，获取父元素
            for (int i = 0; i < propertyList.size(); i++) {
                Element element =  propertyList.get(i);   //<property name="AccountDao" ref="accountDao"></property>
                String name = element.attributeValue("name");
                String ref = element.attributeValue("ref");

                // 找到当前需要被处理依赖关系的bean
                Element parent = element.getParent();

                // 调用父元素对象的反射功能
                String parentId = parent.attributeValue("id");
                Object parentObject = map.get(parentId);
                // 遍历父对象中的所有方法，找到"set" + name
                Method[] methods = parentObject.getClass().getMethods();
                for (int j = 0; j < methods.length; j++) {
                    Method method = methods[j];
                    if(method.getName().equalsIgnoreCase("set" + name)) {  // 该方法就是 setAccountDao(AccountDao accountDao)
                        method.invoke(parentObject,map.get(ref));
                    }
                }

                // 把处理之后的parentObject重新放到map中
                map.put(parentId,parentObject);

            }

            //扫描注解类 并初始化
            List<String> packageNames = new ArrayList<>();
            List<Element> components = rootElement.selectNodes("//component-scan");
            for (int i = 0; i < components.size(); i++) {
                Element element =  components.get(i);
                packageNames.add(element.attributeValue("package"));
            }
//            packageNames.add("com.lagou.edu.service");
            Set<Class<?>> clazzs = new HashSet<>();
            for (String packageName : packageNames) {
                clazzs.addAll(AnnotationUtils.getClasses(packageName));
            }
            if (!clazzs.isEmpty()) {
                for (Class<?> clazz : clazzs) {

                    // 获取类上的注解
                    Annotation[] annos = clazz.getAnnotations();
                    for (Annotation anno : annos) {
                        if(anno.annotationType() == Service.class){
                            Object newInstance = clazz.newInstance();
                            populateBean(clazz, newInstance);
                            Service annotation = clazz.getAnnotation(Service.class);
                            Transactional transanctionalAnnotationObject = getTransanctionalAnnotationObject(clazz, annos);
                            //存在事务注解时`
                            if(transanctionalAnnotationObject != null){

                                Object proxy;
                                ProxyFactory p = (ProxyFactory)getBean("proxyFactory");
                                Class<?>[] interfaces = clazz.getInterfaces();
                                if(interfaces != null && interfaces.length >0){
                                    proxy = p.getJdkProxy(newInstance);
                                }else{
                                    proxy = p.getCglibProxy(newInstance);
                                }
                                map.put(annotation.value(),proxy);

                            }else{
                                map.put(annotation.value(), newInstance);
                            }
                        }

                    }

                }

            }

        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    private static void populateBean(Class clazz ,Object bean) throws InvocationTargetException, IllegalAccessException {
        // 获取属性的注解
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            for (Annotation an : field.getAnnotations()) {
                if(an.annotationType() == Autowired.class){
                    String ref = field.getName();
                    Method[] methods = clazz.getMethods();
                    for (int j = 0; j < methods.length; j++) {
                        Method method = methods[j];
                        if(method.getName().equalsIgnoreCase("set" + ref)) {  // 该方法就是 setAccountDao(AccountDao accountDao)
                            method.invoke(bean,map.get(ref));
                        }
                    }
                }
            }
        }
    }

    private static Transactional getTransanctionalAnnotationObject(Class<?> clazz,Annotation[] annos) {
        for (Annotation anno : annos) {
            if(anno.annotationType() == Transactional.class){
                return clazz.getAnnotation(Transactional.class);
            }
        }
        return null;
    }


    // 任务二：对外提供获取实例对象的接口（根据id获取）
    public static  Object getBean(String id) {
        return map.get(id);
    }

}
