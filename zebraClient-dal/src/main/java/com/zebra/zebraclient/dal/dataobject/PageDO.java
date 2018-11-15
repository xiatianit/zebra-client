package com.zebra.zebraclient.dal.dataobject;

import lombok.Data;


/**
 * 针对页码查询的
 * 
 * @author owen
 *
 */
public class PageDO {
    
    public static final int glabalRows  =10;
    
    private Integer pageIndex;

    private Integer offset;

    private Integer rows;

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public Integer getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(Integer pageIndex) {
        if(pageIndex == null){
            this.setOffset(0);
            this.setRows(PageDO.glabalRows);
        }
        else{
            this.setOffset((pageIndex-1) * PageDO.glabalRows);
            this.setRows(PageDO.glabalRows);
        }
        this.pageIndex = pageIndex;
    }

}
