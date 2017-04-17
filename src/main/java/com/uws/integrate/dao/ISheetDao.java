package com.uws.integrate.dao;

import com.uws.core.hibernate.dao.IBaseDao;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.domain.integrate.SheetModel;

/**
 * 
* @ClassName: ISheetDao 
* @Description: 预警管理 DAO 接口
* @author 联合永道
* @date 2015-12-30 上午9:46:21 
*
 */
public interface ISheetDao extends IBaseDao
{
	/**
	 * 
	 * @Title: querySheetPage
	 * @Description: 分页列表查询
	 * @param pageNo
	 * @param pageSize
	 * @param warningForward
	 * @return
	 * @throws
	 */
	public Page querySheetPage(int pageNo,int pageSize,SheetModel warningForward);
	
	/**
	 * 
	 * @Title: queryByConditions
	 * @Description: 按照查询条件查询, 组合主键
	 * @param college
	 * @param year
	 * @param term
	 * @param warningType
	 * @return
	 * @throws
	 */
	public SheetModel queryByConditions(String college,String year,String term,String warningType);
	
}
