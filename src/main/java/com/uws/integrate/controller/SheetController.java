package com.uws.integrate.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.uws.common.service.IBaseDataService;
import com.uws.core.base.BaseController;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.core.session.SessionFactory;
import com.uws.core.session.SessionUtil;
import com.uws.domain.base.BaseAcademyModel;
import com.uws.domain.integrate.SheetModel;
import com.uws.integrate.service.ISheetService;
import com.uws.log.Logger;
import com.uws.log.LoggerFactory;
import com.uws.sys.model.Dic;
import com.uws.sys.model.UploadFileRef;
import com.uws.sys.service.DicUtil;
import com.uws.sys.service.FileUtil;
import com.uws.sys.service.impl.DicFactory;
import com.uws.sys.service.impl.FileFactory;
import com.uws.user.model.User;
import com.uws.util.CheckUtils;
import com.uws.util.ProjectSessionUtils;

/**
* @ClassName: SheetController 
* @Description: 报表管理控制Controller
* @author 联合永道
* @date 2015-12-29 下午3:05:13 
 */
@Controller
public class SheetController extends BaseController {
	
	@Autowired
	private ISheetService sheetService;
	@Autowired
	private IBaseDataService baseDataService;
	
	private Logger logger = new LoggerFactory(this.getClass());
	private DicUtil dicUtil = DicFactory.getDicUtil();
	private FileUtil fileUtil = FileFactory.getFileUtil();
	private SessionUtil sessionUtil = SessionFactory.getSession("/integrate/sheet");
	
	//日期格式批量转换
	@InitBinder
    protected void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }
	
	/**
	 * @Title: sheetManage
	 * @Description: 管理列表方法
	 * @param request
	 * @param response
	 * @param model
	 * @param sheet
	 * @return
	 * @throws
	 */
	@RequestMapping("/integrate/sheet/opt-query/sheetList")
	public String sheetManage(HttpServletRequest request,HttpServletResponse response, ModelMap model, SheetModel sheet){
		int pageNo = request.getParameter("pageNo") != null ? Integer .valueOf(request.getParameter("pageNo")) : 1;
		Page page = sheetService.querySheetPage(pageNo, Page.DEFAULT_PAGE_SIZE, sheet);
		List<Dic> yearList = dicUtil.getDicInfoList("YEAR");
		model.addAttribute("sheet", sheet);
		model.addAttribute("yearList", yearList);
		model.addAttribute("page", page);
		return "/integrate/sheet/sheetManageList";
	}

	
	
	
	
	/**
	 * 
	 * @Title: editSheet
	 * @Description：上报 编辑
	 * @param request
	 * @param response
	 * @param model
	 * @param sheet
	 * @return
	 * @throws
	 */
	@RequestMapping(value={"/integrate/sheet/opt-add/addSheet"})
	public String editSheet(HttpServletRequest request,HttpServletResponse response, ModelMap model,String id)
	{
		if(com.uws.core.util.StringUtils.hasText(id)){
			logger.debug("编辑报表信息");
			SheetModel sheetModel = sheetService.findById(id);
			model.addAttribute("uploadFileRefList", this.fileUtil.getFileRefsByObjectId(sheetModel.getId()));
			model.addAttribute("sheet", sheetModel);
		}else{
			logger.debug("新增报表信息，赋值默认的系统时间");
			model.addAttribute("sheet", new SheetModel());
			model.addAttribute("uploadFileRefList", this.fileUtil.getFileRefsByObjectId(null));
		}   
		 
		     model.addAttribute("currentUserId",sessionUtil.getCurrentUserId());
			model.addAttribute("yearList", dicUtil.getDicInfoList("YEAR"));

		   return "/integrate/sheet/addSheet";
	}
	
	/**
	 * 
	 * @Title: saveSheet
	 * @Description: save SheetModel infos 
	 * @param request
	 * @param response
	 * @param model
	 * @param id
	 * @param fileIds
	 * @return
	 * @throws
	 */
	@RequestMapping(value={"/integrate/sheet/opt-submit/saveSheet","/integrate/sheet/opt-edit/editSheet"})
	public String saveSheet(HttpServletRequest request,HttpServletResponse response, ModelMap model,SheetModel sheet,String[] fileId)
	{
    	if(com.uws.core.util.StringUtils.hasText(sheet.getId())){
			//报表信息修改
    		SheetModel sheetModelPo = sheetService.findById(sheet.getId());
			BeanUtils.copyProperties(sheet,sheetModelPo,new String[]{"createTime"});
			this.sheetService.updateSheetModel(sheetModelPo,fileId);
			logger.info("报表信息修改成功!");
		}else{
			this.sheetService.saveSheetModel(sheet,fileId);
			logger.info("报表信息新增成功!");
		}
    	 return "redirect:/integrate/sheet/opt-query/sheetList.do";
	}
	
	/**
	 * 
	 * @Title: saveSheet
	 * @Description: 报表信息删除  (物理删除)
	 * @param request
	 * @param response
	 * @param id
	 * @return
	 * @throws
	 */
	@ResponseBody
	@RequestMapping("/integrate/sheet/opt-del/delSheet")
	public String saveSheet(HttpServletRequest request,HttpServletResponse response,String id)
	{
		/*
		 * 只删除表中的记录,附件记录和对应的文件没有删除
		 */
		if(!StringUtils.isEmpty(id))
			sheetService.deleteById(id);
		return "success";
	}
	
	/**
	 * 
	 * @Title: viewSheet
	 * @Description:  查看详细信息
	 * @param request
	 * @param response
	 * @param id
	 * @return
	 * @throws
	 */
	@RequestMapping("/integrate/sheet/view/viewSheet")
	public String viewSheet(HttpServletRequest request,HttpServletResponse response, String id,ModelMap model)
	{
		SheetModel sheet = new SheetModel();
		if(!StringUtils.isEmpty(id))
		{
			sheet = sheetService.findById(id);
			List<UploadFileRef> fileList = fileUtil.getFileRefsByObjectId(id);
			model.addAttribute("uploadFileRefList", fileList);
		}
		model.addAttribute("sheet", sheet);
		model.addAttribute("yearList", dicUtil.getDicInfoList("YEAR"));
		return "/integrate/sheet/viewSheet";
	}
	
	/**
	 * 
	 * @Title: checkSheet
	 * @Description: 判断查询
	 * @param request
	 * @param college
	 * @param year
	 * @param term
	 * @param integrateType
	 * @return
	 * @throws
	 */
	@ResponseBody
	@RequestMapping("/integrate/sheet/opt-check/checkSheet")
	public String checkSheet(HttpServletRequest request,String college,String year,String term,String integrateType){
		String result = "";
		if(!StringUtils.isEmpty(college) &&!StringUtils.isEmpty(year) &&!StringUtils.isEmpty(term) &&!StringUtils.isEmpty(integrateType)){
			SheetModel sheet = null;
//			SheetModel sheet = sheetService.queryByConditions(college, year, term, integrateType);
			if(null != sheet && !"".equals(sheet.getId()))
				result = sheet.getId();
		}
		return result;
	}
	
	
}
