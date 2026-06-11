package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.context.BaseContext;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetMellDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetMealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.val;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class SetMealServiceImpl implements SetMealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetMellDishMapper setMellDishMapper;


    // 新增套餐
    @Override
    @Transactional//添加事务
    public void save(SetmealDTO setmealDTO) {
       //逻辑：1.保存套餐信息到setmeal，2.保存套餐和菜品的关联关系setmeal_dish
        //1.保存套餐信息到setmeal
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);

        setmealMapper.insert(setmeal);
        //获取当前保存的套餐id
        Long Id = setmeal.getId();

        //2.保存套餐和菜品的关联关系setmeal_dish
        List<SetmealDish> SetmealDisheslist = setmealDTO.getSetmealDishes();

        //遍历SetmealDisheslist为每一个SetmealDish都设置setmealId为同一个Id
        for (SetmealDish setmealDish : SetmealDisheslist) {
            setmealDish.setSetmealId(Id);
        }

        setMellDishMapper.insertBatch(SetmealDisheslist);
    }

    // 套餐分页查询
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());

    }

    // 批量删除套餐
    @Override
    @Transactional//添加事务
    public void delete(List<Long> ids) {
        //1.删除套餐信息
        setmealMapper.delete(ids);
        //2.删除套餐和菜品的关联关系
        setMellDishMapper.deleteBySetmealIds(ids);


    }

    //根据id查询套餐（回显）
    @Override
    public SetmealVO getById(Long id) {

        //1.根据id查询套餐基本数据
        SetmealVO setmealVO = setmealMapper.getById(id);
        //2.根据id查询套餐和菜品的关联关系
        List<SetmealDish> setmealDishes = setMellDishMapper.getMealDishBydishid(id);

        //3.将setmealDishes设置到setmealVO中
        setmealVO.setSetmealDishes(setmealDishes);

        return setmealVO;

    }

    @Override
    public void update(SetmealDTO setmealDTO) {
        //逻辑：1.修改套餐信息到setmeal，
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.update(setmeal);
        //获取当前保存的套餐id
        Long Id = setmeal.getId();


        // 2.修改套餐和菜品的关联关系setmeal_dish：
        // 2.1.删除当前套餐id对应的所有关联数据（setmeal_dish）
        setMellDishMapper.deleteBySetmealIds(Arrays.asList(new Long[]{Id}));
        // 2.2.重新插入新的关联数据
        List<SetmealDish> SetmealDisheslist = setmealDTO.getSetmealDishes();
        //遍历SetmealDisheslist为每一个SetmealDish都设置setmealId为同一个Id
        for (SetmealDish setmealDish : SetmealDisheslist) {
            setmealDish.setSetmealId(Id);
        }
        setMellDishMapper.insertBatch(SetmealDisheslist);

    }

    // 套餐起售停售
    @Override
    public void updateStatus(Integer status, Long id) {
        //根据id更改起售停售状态
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
                setmealMapper.updateStatus(setmeal);

    }

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
