package com.jason.reggie.dto;

import com.jason.reggie.entity.Setmeal;
import com.jason.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
