package com.dong.urlshortener.mapper;

import com.dong.urlshortener.model.Link;
import org.apache.ibatis.annotations.Mapper;

@Mapper // 关键注解，声明这是一个 MyBatis Mapper 接口
public interface LinkMapper {

    /**
     * 插入一条新的链接记录。
     * 注意：这个方法会修改传入的 link 对象的 id 属性，
     * 将数据库生成的自增 ID 回填进去。
     * @param link 包含 longUrl 的 Link 对象
     * @return 影响的行数
     */
    int insertLink(Link link);

    /**
     * 根据 ID 更新 short_code。
     * @param link 包含 id 和 shortCode 的 Link 对象
     */
    void updateShortCode(Link link);

    /**
     * 根据 short_code 查找链接。
     * @param shortCode 短码
     * @return 对应的 Link 对象
     */
    Link findByShortCode(String shortCode);

    Link findActiveByLongUrlHash(String longUrlHash);
}