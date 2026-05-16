package com.szy.mapper;

import com.szy.entity.InspectionRecords;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author l
 * @since 2022-02-24
 */
public interface InspectionRecordsMapper extends BaseMapper<InspectionRecords> {
    void add(String project, BigDecimal longitude, BigDecimal latitude, String type, String abnormal, String situation, String solve, String image, String person, Date date, Date createTime, Date updateTime, String geom);

    void addPosition(Long id, String geom);

    /**
     * 获取所有工程站点
     * @return java.util.List<java.lang.String>
     * @author admin
     * @date 2024/07/08 19:25
     */
    List<String> getAllProjects();

}
