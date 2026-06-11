package com.sky.aspect;


import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){}

     @Before("autoFillPointCut()")
      public void autoFill(JoinPoint joinPoint){
        log.info("开始进行公共字段数据填充");
        //1.获取当前被拦截的方法的数据库操作类型
         MethodSignature signature = (MethodSignature) joinPoint.getSignature();
         Method method = signature.getMethod();//获取当前被拦截的方法
         AutoFill autoFill = method.getAnnotation(AutoFill.class);//获取当前方法上的数据库操作类型


         //2.获取当前被拦截的方法的参数
         Object[] args = joinPoint.getArgs();
         if(args == null || args.length == 0){
             return;
         }
         Object entity = args[0];//获取当前参数
         //3.准备赋值的数据
         LocalDateTime now = LocalDateTime.now();
         long currentId = BaseContext.getCurrentId();


         //4.根据当前不同的数据库操作类型，为对应的参数赋值
         if(autoFill.value() == OperationType.INSERT){
             //为四个公共字段赋值
             try {
                 Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                 Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                 Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                 Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);

                 setCreateTime.invoke(entity, now);
                 setUpdateTime.invoke(entity, now);
                 setCreateUser.invoke(entity, currentId);
                 setUpdateUser.invoke(entity, currentId);

             } catch (Exception e) {
                 log.error("公共字段赋值失败: {}", e.getMessage());
             }
         } else if (autoFill.value() == OperationType.UPDATE) {
             //为两个公共字段赋值
             try {
                 Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                 Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                 setUpdateTime.invoke(entity, now);
                 setUpdateUser.invoke(entity, currentId);

             } catch (Exception e) {
                 log.error("公共字段赋值失败: {}", e.getMessage());
             }
         }
     }

}
