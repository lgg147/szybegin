package com.szy.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.szy.entity.WaterLevelCollector;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
@DS("collector")
public interface WaterLevelCollectorMapper extends BaseMapper<WaterLevelCollector> {

    @Select("SELECT * FROM wr_mp_z_r WHERE (MP_CD, TS) IN (SELECT MP_CD, MAX(TS) FROM wr_mp_z_r GROUP BY MP_CD);")
    List<WaterLevelCollector> selectLastRecord();
}
