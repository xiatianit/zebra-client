package com.zebra.zebraclient.dal.mapper;

import java.util.List;

import com.zebra.zebraclient.dal.dataobject.SiteDO;
import com.zebra.zebraclient.dal.dataobject.businessObject.SiteNearBO;

public interface SiteMapper {

    /****************** 基础的增删改查 ********************/

    /**
     * 获取投放点信息个数
     * 
     * @param siteDO
     * @return
     */
    List<SiteDO> selectNearSiteByPoint(SiteNearBO siteNearBO);

}
