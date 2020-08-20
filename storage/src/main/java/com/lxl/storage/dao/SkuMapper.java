package com.lxl.storage.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lxl.common.entity.storage.Sku;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface SkuMapper extends BaseMapper<Sku> {

    @Select("update sku set stock = stock - #{num} where id = #{id}")
    void reduceStock(@Param("id") Long id, @Param("num") Integer num);

}
