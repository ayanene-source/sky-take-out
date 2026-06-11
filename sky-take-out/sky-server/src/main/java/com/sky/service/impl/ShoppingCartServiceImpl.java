package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    //添加购物车
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        //判断当前加入购物车的商品是否已存在
        ShoppingCart ShoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, ShoppingCart);
        Long userId = BaseContext.getCurrentId();
        ShoppingCart.setUserId(userId);

        List<ShoppingCart> list= shoppingCartMapper.list(ShoppingCart);

        //存在，则执行updatad操作，数量+1
        if (list != null && list.size() > 0) {
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updataNumberById(cart);
        }else{ //不存在，insert添加到购物车，数量默认为1，绑定用户id
            //判断添加的是菜品还是套餐
            Long dishid = shoppingCartDTO.getDishId();
            if (dishid != null) {
                //添加的是菜品
               Dish dish = dishMapper.getById(dishid);
               ShoppingCart.setName(dish.getName());
               ShoppingCart.setImage(dish.getImage());
               ShoppingCart.setAmount(dish.getPrice());
            }else{
                //添加的是套餐
                Long setmealid = shoppingCartDTO.getSetmealId();
                SetmealVO setmealVO = setmealMapper.getById(setmealid);
                ShoppingCart.setName(setmealVO.getName());
                ShoppingCart.setImage(setmealVO.getImage());
                ShoppingCart.setAmount(setmealVO.getPrice());
            }
            ShoppingCart.setNumber(1);
            ShoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(ShoppingCart);
        }




    }

    //展示购物车列表
    @Override
    public List<ShoppingCart> showShoppingCart() {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart ShoppingCart = new ShoppingCart();
        ShoppingCart.setUserId(userId);
        List<ShoppingCart> list = shoppingCartMapper.list(ShoppingCart);
        return list;
    }

    //清空购物车
    @Override
    public void clean() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteALLByUserId(userId);

    }

    //删除购物车中的一个商品
    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart ShoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, ShoppingCart);
        ShoppingCart.setUserId(BaseContext.getCurrentId());
        //获取当前要删除的购物车数据
        List<ShoppingCart> list = shoppingCartMapper.list(ShoppingCart);
        //判断当前购物车数据
        if (list != null && list.size() > 0) {
            ShoppingCart cart = list.get(0);
            if (cart.getNumber() == 1) { //如果当前数据数量为1，则直接删除
                shoppingCartMapper.deleteById(cart.getId());
            }else{ //数量不为1，则数量-1
                cart.setNumber(cart.getNumber() - 1);
                shoppingCartMapper.updataNumberById(cart);
            }
        }



    }
}
