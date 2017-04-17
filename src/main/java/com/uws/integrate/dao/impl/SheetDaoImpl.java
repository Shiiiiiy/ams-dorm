package com.uws.integrate.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import com.uws.core.hibernate.dao.impl.BaseDaoImpl;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.domain.integrate.SheetModel;
import com.uws.integrate.dao.ISheetDao;

/**
 * 
* @ClassName: SheetDaoImpl 
* @Description: 预警管理 
* @author 联合永道
* @date 2015-12-30 上午9:47:19 
*
 */
@Repository("com.uws.warning.dao.impl.SheetDaoImpl")
public class SheetDaoImpl extends BaseDaoImpl implements ISheetDao
{

	/**
	 * 描述信息: Sheet infos paged query  
	 * @param pageNo
	 * @param pageSize
	 * @param sheet
	 * @return
	 * 2015-12-30 上午10:15:47
	 */
	@Override
    public Page querySheetPage(int pageNo, int pageSize, SheetModel sheet)
    {
		List<Object> values = new ArrayList<Object>();
		
		StringBuffer hql = new StringBuffer( "from SheetModel s where 1=1 ");
		if(null != sheet)
		{
			if(null != sheet.getTitle() && StringUtils.isNotBlank(sheet.getTitle()))
			 {   
				 hql.append(" and s.title like ? ");
				 values.add("%" + sheet.getTitle() + "%");
			 }
			 if(sheet.getYear()!=null && StringUtils.isNotBlank(sheet.getYear().getId()))
			 {
				 hql.append(" and s.year.id = ?");
				 values.add(sheet.getYear().getId());
			 }
		}
		
		if (values.size() == 0)
			return this.pagedQuery(hql.toString(), pageNo, pageSize);
		else
			return this.pagedQuery(hql.toString(), pageNo, pageSize, values.toArray());
    }

	/**
	 * 描述信息: 按照条件查询
	 * @param college
	 * @param year
	 * @param term
	 * @param warningType
	 * @return
	 * 2015-12-30 下午4:16:10
	 */
	@Override
	@SuppressWarnings("unchecked")
    public SheetModel queryByConditions(String college, String year, String term, String warningType){
		StringBuffer hql = new StringBuffer( "from SheetModel where college.id=? and yearDic.id=? and termDic.id=? and warningType.id=? ");
	    List<SheetModel>  list = this.query(hql.toString(), new Object[]{college, year,term, warningType});
	    return null == list||list.size()==0 ? null : list.get(0);
    }

}
