package com.szy.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.szy.entity.RtuBaseInfoExtend;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DS("collector")
public interface RtuBaseInfoExtendMapper extends BaseMapper<RtuBaseInfoExtend> {
}