package com.uws.integrate.service;

import com.uws.core.base.IBaseService;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.domain.integrate.SheetModel;
import com.uws.user.model.User;

/**
 * 
* @ClassName: ISheetService 
* @Description: 预警管理 service 接口 
* @author 联合永道
* @date 2015-12-30 上午9:42:14 
*
 */
public interface ISheetService extends IBaseService {
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
	 * @Title: findById
	 * @Description: query by primaryKey
	 * @param id
	 * @return
	 * @throws
	 */
	public SheetModel findById(String id);
	/*
	*//**
	 * 
	 * @Title: saveOrUpdateSheet
	 * @Description: save the sumitforward infos
	 * @param model
	 * @param fileIds
	 * @throws
	 *//*
	public void saveOrUpdateSheet(SheetModel model,String[] fileIds,User currentUser);*/
	
	/**
	 * 
	 * @Title: deleteById
	 * @Description: delete infos (Physical deletion)
	 * @param id
	 * @throws
	 */
	public void deleteById(String id);
	
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

	public void updateSheetModel(SheetModel sheetModelPo, String[] fileId);

	public void saveSheetModel(SheetModel sheet, String[] fileId);
	
}
