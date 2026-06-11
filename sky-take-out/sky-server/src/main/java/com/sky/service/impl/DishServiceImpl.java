package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMellDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetMellDishMapper setMellDishMapper;


    @Override
    @Transactional//添加事务
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        //属性拷贝
        BeanUtils.copyProperties(dishDTO,dish);
        //1.向菜品表插入一条数据
        dishMapper.insert(dish);

        //获取当前菜品id，下面插入的口味数据（dishId）需要依赖这个id
        Long dishId = dish.getId();

        //2.向口味表插入m条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        //为每个口味设置同一个菜品id
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishId);
        }
        if (flavors != null && flavors.size() > 0) {
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    //分页查询
    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        //使用pagehelper插件进行分页
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    //批量删除
    @Override
    @Transactional
    public void delete(List<Long> ids) {
        //1.菜品的状态为1不能删除
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //2.菜品有关联套餐不能删除
        List<Long> Ids = setMellDishMapper.getMellDishIdsBySetMealId(ids);
        if (Ids != null && Ids.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //3.删除菜品表中的数据，删除关联的口味数据
//        for (Long id : ids) {
//            dishMapper.deleteById(id);
//            dishFlavorMapper.deleteByDishId(id);
//        }
        dishMapper.deleteByIds(ids);

        dishFlavorMapper.deleteByDishIds(ids);
    }

    //根据id查询菜品和对应的口味
    @Override
    public DishVO getById(Long id) {
        //1.根据id查询菜品数据
        Dish dish = dishMapper.getById(id);

        //2.根据菜品id查询口味数据
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);

        //3.封装数据并返回
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);

        return dishVO;
    }

    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //1.修改菜品表
        dishMapper.update(dish);
        //2.删除菜品口味表中的口味数据
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        //3.添加新的口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        //为每个口味设置同一个菜品id
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishDTO.getId());
        }
        if (flavors != null && flavors.size() > 0) {
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    @Override
    public void updateStatus(Integer status, Long id) {
       //根据id为对应菜品设置状态
        Dish dish = Dish.builder()
                .status(status)
                .id(id)
                .build();
                dishMapper.update(dish);
    }

    @Override
    public List<DishVO> list(Long categoryId) {
        //
        List<DishVO> Dishlist = dishMapper.listByCategoryId(categoryId);
        return Dishlist;
    }


    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {

        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}
