package com.yy.servlet;

import com.yy.annotion.*;
import com.yy.controller.FishController;
import com.yy.vo.PostClas;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DispatcherServlet {

    private static final long serialVersionUID = 1L;

    private String classPath="D:\\hzzmysoft\\myspace\\springIoc-master\\springioc\\target\\classes\\";


    private List<String> classNames = new ArrayList<String>();

    private Map<String, Object> instanceMap = new HashMap<String,Object>();

    private Map<String, Method> methodMap = new HashMap<String,Method>();
    private List<PostClas> beginList = new ArrayList<>();


    public DispatcherServlet() {

    }





    /**
     *
     *  找到bean ， 生成bean载入bean ， 注入bean
     *
     */
    public void init()  {
        //找到bean
        scanBase(classPath);
        try {
            //生成并注册bean
            filterAndInstance();
            //注入bean
            springDi();
            //获取url对应的method
            mvc();
            begin();//初始化执行
            Method method = methodMap.get("/fish/get");
            Object fish = instanceMap.get("fish");
            //调用实例
            try {
                method.invoke(fish);



            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public void begin(){
        for (PostClas postClas : beginList) {
            postClas.runBegin();
        }
    }

    /**
     *
     * 找到bean
     *
     * */

    /**
     * TODO:递归扫描指定文件夹下面的指定文件
     *
     * @return ArrayList<Object>
     * @throws FileNotFoundException
     */
    public  void scanBase(String folderPath) {

        try {
            File directory = new File(folderPath);
            if (!directory.isDirectory()) {
                throw new FileNotFoundException('"' + folderPath + '"' + " input path is not a Directory , please input the right path of the Directory. ^_^...^_^");
            }
            if (directory.isDirectory()) {
                File[] filelist = directory.listFiles();
                for (int i = 0; i < filelist.length; i++) {
                    /**如果当前是文件夹，进入递归扫描文件夹**/
                    if (filelist[i].isDirectory()) {
                        /**递归扫描下面的文件夹**/
                       scanBase(filelist[i].getAbsolutePath()) ;
                    }
                    /**非文件夹**/
                    else {
                        String filename=filelist[i].getName();
                        String filetype = filename.substring(filename.lastIndexOf("."));
                        if(".class".equalsIgnoreCase(filetype)){
                            classNames.add(filelist[i].getPath().replace(classPath,"").replaceAll("\\\\","."));
                        }
                    }
                }
            }

        } catch (FileNotFoundException e) {

        }
    }


    /**
     *
     * 生成并注册bean
     *
     * */
    //把classNames循环一遍，看里面有哪些类，然后把它生成出来加载进去
    private void filterAndInstance() throws Exception {
        if( classNames.size() == 0) {
            return;
        }
        //循环获取类名
        for(String className : classNames) {
            Class clazz = Class.forName(className.replace(".class", ""));
            if(clazz.isAnnotationPresent(Controller.class)) {  //如果clazz字节码上面带了一个Annotation,并且Annotion是controller,就实例化一个controller对象出来
                //获取bean实例
                Object instance = clazz.newInstance();
                //获取注解的value---将controller上的名字取出来（fish）
                String key = ((Controller)clazz.getAnnotation(Controller.class)).value();
                //将bean交付给IOC
                instanceMap.put(key, instance);
            }else if(clazz.isAnnotationPresent(Service.class)) { //如果clazz字节码上面带了一个Annotation,并且Annotion是Service
                //获取bean实例
                Object instance = clazz.newInstance();
                //获取注解的value
                String key = ((Service)clazz.getAnnotation(Service.class)).value();
                //将bean交付给IOC
                instanceMap.put(key, instance);
            }else {
                continue;
            }
        }
    }



    /**
     *
     * 注入bean
     *
     * */
    //把ioc容器里的bean注入到指定地方（instanceMap中的值注入到@qualifier）p--DI
    //把instanceMap循环一遍，把它每一个对象字节码中的file取出来，看看里面有没有Qualifier的注解，如果有的话，把qualifier中的value取出来，然后把value对应的对象注入到这里
    private void springDi() throws IllegalArgumentException, IllegalAccessException {

        if(instanceMap.size() == 0) {
            return;
        }
        /**
         * 循环获取实例
         * */
        for(Map.Entry<String, Object> entry: instanceMap.entrySet()) {
            //获取所有的类变量
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            //查看上面有没有qualifer的标识,如果有qualifer的标识，把它的value取出来，通过这个值我们就可以拿到它的实例
            for(Field  field: fields) {
                //包含Qualifer注解
                if(field.isAnnotationPresent(Qualifier.class)){
                    String key = ((Qualifier)field.getAnnotation(Qualifier.class)).value();
                    field.setAccessible(true);
                    field.set(entry.getValue(), instanceMap.get(key));    //注入qualifier----即把fishService注入进去了
                }//autowired
            }
        }


    }



    //把路径给生成出来（springMVC）
    private void mvc() {
        if(instanceMap.size() == 0) {
            return;
        }
        /** 循环获取实例*/
        for(Map.Entry<String, Object> entry: instanceMap.entrySet()) {
            //将含有controller的所有实例都取出来
            if(entry.getValue().getClass().isAnnotationPresent(Controller.class)) {//entry得到对象,得到字节码，看看是否含有注解
                String ctlUrl = ((Controller)entry.getValue().getClass().getAnnotation(Controller.class)).value();
                Method[] methods = entry.getValue().getClass().getMethods();//将controller中的所有方法取出来
                for(Method method :methods) {            //循环遍历方法，看看方法上面是否有@RequestMapping
                    //含有RequestMapping注解
                    if(method.isAnnotationPresent(RequestMapping.class)) {
                        String reqUrl = ((RequestMapping)method.getAnnotation(RequestMapping.class)).value();  //url全部生成完毕
                        String dispatchUrl = "/"+ ctlUrl +"/" + reqUrl;
                        methodMap.put(dispatchUrl, method);         //每个url对应的方法存好
                    }
                    if(method.isAnnotationPresent(PostConstruct.class)) {
                        PostClas postClas=new PostClas();
                        postClas.setMethod(method);
                        postClas.setObject(entry.getValue());
                        beginList.add(postClas);
                    }
                }
            }else if(entry.getValue().getClass().isAnnotationPresent(Service.class)){
                Method[] methods = entry.getValue().getClass().getMethods();//将controller中的所有方法取出来
                for(Method method :methods) {            //循环遍历方法，看看方法上面是否有@RequestMapping
                    if(method.isAnnotationPresent(PostConstruct.class)) {
                        PostClas postClas=new PostClas();
                        postClas.setMethod(method);
                        postClas.setObject(entry.getValue());
                        beginList.add(postClas);
                    }
                }
            }
            else {
                continue;
            }
        }
    }




    //把发送过来的url取到，然后和methodMap里面的url相比较，如果有，就把url对应的method执行即可
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // localhost:8080/springioc/fish/get
        String uri = request.getRequestURI();   //这个ur是完整的url,我们只要得到/fish/get即可
        String projetname = request.getContextPath();
        String path = uri.replaceAll(projetname, "");

        Method method = methodMap.get(path);
        //把controller的value所对应的实例取出来（fish）
        String className = uri.split("/")[2];
        //把实例生成出来
        FishController fishController = (FishController)instanceMap.get(className);
        //调用实例
        try {
            method.invoke(fishController, new Object[] {request,response,null});



        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub
        doGet(request, response);
    }


}
