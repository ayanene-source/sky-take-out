package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetMellDishMapper {

    public List<Long> getMellDishIdsBySetMealId(List<Long> dishIds );

    void insertBatch(List<SetmealDish> setmealDisheslist);

    void deleteBySetmealIds(List<Long> ids);

    // 根据套餐id查询套餐菜品关系数据
    @Select("select * from setmeal_dish where setmeal_id = #{id}")
    List<SetmealDish> getMealDishBydishid(Long id);
}
