package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    public RedisTemplate redisTemplate;

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        //记录耗时
        long start = System.currentTimeMillis();
        log.info("开始查询菜品");

        log.info("根据分类id查询菜品：{}", categoryId);
        String key = "dish_" + categoryId;
        //如果缓存中有数据，则直接返回，不用查询数据库
        List<DishVO> list = (List<DishVO>) redisTemplate.opsForValue().get(key);
        if (list != null) {
            long end = System.currentTimeMillis();
            log.info("查询完成，耗时：{}毫秒", end - start);
            return Result.success(list);
        }
        //缓存中没有数据，则查询数据库

        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品

         list = dishService.listWithFlavor(dish);

        // 5. 处理数据库返回null的情况，转为空列表
        List<DishVO> cacheList = list == null ? Collections.emptyList() : list;

        // 6. 回写缓存（30-60分钟随机过期，避免雪崩）
        long randomTime = 30 + (long) (Math.random() * 30);
        redisTemplate.opsForValue().set(key, cacheList, randomTime, TimeUnit.MINUTES);

        long end = System.currentTimeMillis();
        log.info("查询完成，耗时：{}毫秒", end - start);

        // 7. 返回结果
        return Result.success(cacheList);
    }

}
