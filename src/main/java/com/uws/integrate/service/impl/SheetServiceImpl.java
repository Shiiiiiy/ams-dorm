package com.uws.integrate.service.impl;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uws.core.base.BaseServiceImpl;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.domain.integrate.SheetModel;
import com.uws.integrate.dao.ISheetDao;
import com.uws.integrate.service.ISheetService;
import com.uws.sys.model.UploadFileRef;
import com.uws.sys.service.FileUtil;
import com.uws.sys.service.impl.FileFactory;
import com.uws.user.model.User;

/**
 * 
* @ClassName: SheetServiceImpl 
* @Description: 预警管理 service 
* @author 联合永道
* @date 2015-12-30 上午9:44:27 
*
 */
@Service("sheetService")
public class SheetServiceImpl extends BaseServiceImpl implements ISheetService
{
	@Autowired
	private ISheetDao  sheetDao;
	private FileUtil fileUtil = FileFactory.getFileUtil();
	
	/**
	 * 描述信息: Sheet infos paged query 
	 * @param pageNo
	 * @param pageSize
	 * @param sheet
	 * @return
	 * 2015-12-30 上午10:13:33
	 */
	@Override
    public Page querySheetPage(int pageNo, int pageSize,SheetModel sheet)
    {
	    return sheetDao.querySheetPage(pageNo,pageSize,sheet);
    }

	/**
	 * 描述信息: find by primaryKey 
	 * @param id
	 * @return
	 * 2015-12-30 上午10:58:11
	 */
	@Override
    public SheetModel findById(String id)
    {
	    if(!StringUtils.isEmpty(id))
	    	return (SheetModel) sheetDao.get(SheetModel.class, id);
	    return null;
    }



	/**
	 * 描述信息: 修改 保存 提交的信息
	 * @param model
	 * @param fileIds
	 * 2015-12-30 上午11:46:43
	 *//*
	@Override
    public void saveOrUpdateSheet(SheetModel model, String[] fileIds,User currentUser)
    {
	    
	     * 1、预警信息对象保存
	     
		if (ArrayUtils.isEmpty(fileIds)){
			fileIds = new String[0];
		}
		String id = model.getId();
	    if(!StringUtils.isEmpty(id)){
	    	model.setCreator(currentUser);
	    	this.updateSheetModel(model);
	    }else{
	    	model.setCreator(currentUser);
	    	this.saveSheetModel(model);
	    	id = model.getId();
	    }
	    
	     * 2、预警附件对象处理 对应的objectId  为 第一步保存的ID
	     
	    List<UploadFileRef> list = fileUtil.getFileRefsByObjectId(id);
	    for(UploadFileRef ufr : list) {
	       if(!ArrayUtils.contains(fileIds, ufr.getUploadFile().getId())){
	    	   fileUtil.deleteFormalFile(ufr);  
	       }
	    }
	    for (String fileId : fileIds){
	    	fileUtil.updateFormalFileTempTag(fileId, id);
	    }
    }
*/
	/**
	 * 描述信息: delete infos (Physical deletion)
	 * @param id
	 * 2015-12-30 下午2:37:26
	 */
	@Override
    public void deleteById(String id)
    {
	    if(!StringUtils.isEmpty(id))
	    	sheetDao.deleteById(SheetModel.class, id);
    }

	/**
	 * 描述信息: 按照条件查询
	 * @param college
	 * @param year
	 * @param term
	 * @param warningType
	 * @return
	 * 2015-12-30 下午4:15:45
	 */
	@Override
    public SheetModel queryByConditions(String college, String year, String term, String warningType){
	    return sheetDao.queryByConditions(college, year, term, warningType);
    }
	
	/**
	 * 
	 * @Description:保存报表信息
	 * @author LiuChen  
	 * @date 2016-1-15 下午3:54:21
	 */
	@Override
	public void saveSheetModel(SheetModel sheet, String[] fileId)
	{
		 this.sheetDao.save(sheet);
		    // 上传的附件进行处理
	 		if (!ArrayUtils.isEmpty(fileId))
	 		{
	 			for (String id : fileId)
	 				this.fileUtil.updateFormalFileTempTag(id,sheet.getId());
	 		}
	}
	
	/**
	 * 
	 * @Description: 修改学生信息
	 * @author LiuChen  
	 * @date 2016-1-15 下午3:54:47
	 */
	@Override
	public void updateSheetModel(SheetModel sheetModelPo, String[] fileId)
	{
		  this.sheetDao.update(sheetModelPo);
		    //上传的附件进行处理
	  		if (ArrayUtils.isEmpty(fileId))
	  		       fileId = new String[0];
	  		     List<UploadFileRef> list = this.fileUtil.getFileRefsByObjectId(sheetModelPo.getId());
	  		     for (UploadFileRef ufr : list) {
	  		       if (!ArrayUtils.contains(fileId, ufr.getUploadFile().getId()))
	  		         this.fileUtil.deleteFormalFile(ufr);
	  		    }
	  		     for (String id : fileId){
	  		       this.fileUtil.updateFormalFileTempTag(id, sheetModelPo.getId());
	  		  }
	}
	
}
