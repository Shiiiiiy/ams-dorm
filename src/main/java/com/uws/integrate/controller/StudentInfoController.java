package com.uws.integrate.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.uws.apw.model.ApproveResult;
import com.uws.apw.service.IFlowInstanceService;
import com.uws.apw.util.JsonUtils;
import com.uws.common.service.IActivityService;
import com.uws.common.service.IBaseDataService;
import com.uws.common.service.ICommonConfigService;
import com.uws.common.service.ICommonSponsorService;
import com.uws.common.service.IEvaluationCommonService;
import com.uws.common.service.IRewardCommonService;
import com.uws.common.service.IStuJobTeamSetCommonService;
import com.uws.common.service.IStudentCommonService;
import com.uws.core.base.BaseController;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.core.session.SessionFactory;
import com.uws.core.session.SessionUtil;
import com.uws.core.util.DataUtil;
import com.uws.core.util.StringUtils;
import com.uws.domain.association.AssociationMemberModel;
import com.uws.domain.base.StudentRoomModel;
import com.uws.domain.integrate.StudentGuardianUpdateModel;
import com.uws.domain.integrate.StudentUpdateInfoModel;
import com.uws.domain.orientation.StudentGuardianModel;
import com.uws.domain.orientation.StudentInfoModel;
import com.uws.domain.reward.CollegeAwardInfo;
import com.uws.domain.reward.CountryBurseInfo;
import com.uws.domain.reward.PunishInfo;
import com.uws.domain.reward.StudentApplyInfo;
import com.uws.domain.sponsor.DifficultStudentInfo;
import com.uws.domain.teacher.TeacherInfoModel;
import com.uws.integrate.service.IStudentManageService;
import com.uws.integrate.util.IntegrateConstant;
import com.uws.log.Logger;
import com.uws.log.LoggerFactory;
import com.uws.sys.model.Dic;
import com.uws.sys.model.SysConfig;
import com.uws.sys.service.DicUtil;
import com.uws.sys.service.ISysConfigService;
import com.uws.sys.service.impl.DicFactory;
import com.uws.user.model.User;
import com.uws.util.ProjectConstants;
import com.uws.util.ProjectSessionUtils;

/**
 * 
 * @ClassName: StudentInfoController
 * @Description: 学生信息查询管理Controller
 * @author 联合永道
 * @date 2015-7-27 上午9:40:18
 * 
 */
@Controller
public class StudentInfoController extends BaseController {

	// 日志
	private Logger log = new LoggerFactory(StudentInfoController.class);
	// 学生管理service
	@Autowired
	private IStudentManageService studentManageService;
	@Autowired
	private IStudentCommonService studentCommonService;
	// 基础数据service
	@Autowired
	private IBaseDataService baseDataService;
	//奖惩助信息
	@Autowired
	private IRewardCommonService rewardCommonService;
	//社团信息
	@Autowired
	private IActivityService activityService;
	//贫困生信息
	@Autowired
	private ICommonSponsorService commonSponsorService;
	// 时间设置service
	@Autowired
	private ICommonConfigService commonConfigService;
	@Autowired
	private ISysConfigService sysConfigService;
	//数据字典
	private DicUtil dicUtil = DicFactory.getDicUtil();
	// sessionUtil工具类
	private SessionUtil sessionUtil = SessionFactory.getSession(IntegrateConstant.INTEGRATE_STUDENT_INFO);
	//教工信息工具类
	@Autowired
	private IStuJobTeamSetCommonService istuJobTeamSetCommonService;
	//综合测评
	@Autowired
	private IEvaluationCommonService evaluationCommonService;
	@InitBinder
    protected void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }
	
	/**
	 * 
	 * @Title: queryStudentInfo
	 * @Description: 信息查询展示
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	@RequestMapping(IntegrateConstant.INTEGRATE_STUDENT_SELFINFO+ "/opt-query/queryStudentInfo")
	public String queryStudentInfo(ModelMap model, HttpServletRequest request,HttpServletResponse response) {

		log.info("Controller:StudentInfoController;方法:queryStudentInfo()");

		String currentStudentId = sessionUtil.getCurrentUserId();
		log.info("当前登录人id为"+currentStudentId);
		if (StringUtils.hasText(currentStudentId)) {

			//判断当前登录人是否为学生
			StudentInfoModel studentInfo = studentCommonService.queryStudentById(currentStudentId);
            if(studentInfo!=null){
            	StudentUpdateInfoModel studentUpdateInfo=studentManageService.queryStudentUpdateByNumStatus(currentStudentId,"DELETE");
            	if(studentUpdateInfo!=null && studentUpdateInfo.getStuId()!=null){
            		StudentGuardianUpdateModel studentGuardianUpdate1=(StudentGuardianUpdateModel) studentManageService.queryStudentGuardianUpdate(studentUpdateInfo.getId(),"1","DELETE");
	            	StudentGuardianUpdateModel studentGuardianUpdate2=(StudentGuardianUpdateModel) studentManageService.queryStudentGuardianUpdate(studentUpdateInfo.getId(),"2","DELETE");
	            	StudentGuardianUpdateModel studentGuardianUpdate3=(StudentGuardianUpdateModel) studentManageService.queryStudentGuardianUpdate(studentUpdateInfo.getId(),"3","DELETE");
	            	model.addAttribute("studentGuardianUpdate1", studentGuardianUpdate1);
	            	model.addAttribute("studentGuardianUpdate2", studentGuardianUpdate2);
	            	model.addAttribute("studentGuardianUpdate3", studentGuardianUpdate3);
	            	model.addAttribute("studentUpdateInfo", studentUpdateInfo);
            	}else{
            		StudentGuardianModel studentGuardian1=studentCommonService.queryStudentGuardianByStudentNo(studentInfo.getStuNumber(), "1");
        			StudentGuardianModel studentGuardian2=studentCommonService.queryStudentGuardianByStudentNo(studentInfo.getStuNumber(), "2");
        			StudentGuardianModel studentGuardian3=studentCommonService.queryStudentGuardianByStudentNo(studentInfo.getStuNumber(), "3");
        			model.addAttribute("studentGuardianUpdate1", studentGuardian1);
                	model.addAttribute("studentGuardianUpdate2", studentGuardian2);
                	model.addAttribute("studentGuardianUpdate3", studentGuardian3);
            	}
                model.addAttribute("studentInfo", studentInfo);
                model.addAttribute("student", "student");
            	//学生住宿信息
    			StudentRoomModel studentRoom=baseDataService.findRoomByStudentId(currentStudentId);
    			if(studentRoom!=null && studentRoom.getRoom()!=null){
    				List<StudentRoomModel> studentRoomList=baseDataService.findRoomByRoomId(studentRoom.getRoom().getId(),currentStudentId);
    				model.addAttribute("studentRoomList", studentRoomList);
    			}
    			//学生综合测评信息
    			int pageNo = request.getParameter("pageNo") != null ? Integer .valueOf(request.getParameter("pageNo")) : 1;
    			Page page =evaluationCommonService.queryEvaluationPage(pageNo, 10, currentStudentId);
    			//奖惩信息 奖 惩 助
    			List<StudentApplyInfo> studentApplyInfoList=rewardCommonService.getStuAwardList(studentInfo);
    			List<PunishInfo> PunishInfoList=rewardCommonService.getStuPunishList(studentInfo);
    			List<CollegeAwardInfo> collegeAwardList=rewardCommonService.getStuCollegeAwardList(studentInfo);
    			List<CountryBurseInfo> countryBurseInfoList=rewardCommonService.getStuBurseList(studentInfo);
    			//社团信息
    			List<AssociationMemberModel> associationMemberList=activityService.queryAssociationMemberByMemberId(studentInfo.getId());
    			//贫困生信息
    			List<DifficultStudentInfo> difficultList=commonSponsorService.queryDifficultStudentList(studentInfo.getStuNumber());
    			//班主任信息
    			if(studentInfo.getClassId()!=null && studentInfo.getClassId().getHeadermaster()!=null){
    				TeacherInfoModel teacher=istuJobTeamSetCommonService.getTeacherInfoByBTId(studentInfo.getClassId().getHeadermaster().getId());
        			model.addAttribute("teacher", teacher);
    			}
    			model.addAttribute("associationMemberList", associationMemberList);
    			model.addAttribute("difficultList", difficultList);
    			model.addAttribute("processStatusMap", ProjectSessionUtils.getApproveProcessStatus());
    			model.addAttribute("studentApplyInfoList", studentApplyInfoList);
    			model.addAttribute("PunishInfoList", PunishInfoList);
    			model.addAttribute("countryBurseInfoList", countryBurseInfoList);
    			model.addAttribute("collegeAwardList", collegeAwardList);
    			model.addAttribute("page", page);
    			model.addAttribute("studentRoom", studentRoom);
			}
		}
		//判断修改按钮是否显示
		boolean bol = commonConfigService.checkCurrentDateByCode(IntegrateConstant.UPDATE_TIME_CONFIG_CODE);
		model.addAttribute("bol", bol); 
		
		//学生健康测评网站
		 SysConfig webUrl = this.sysConfigService.getSysConfig(com.uws.common.util.Constants.STUDENT_EVALUATE_WEB_CODE);
		 model.addAttribute("webUrl",webUrl);
		
		return IntegrateConstant.INTEGRATE_STUDENT_FTL + "/studentInfoView";
	}
	
	/**
	 * 
	 * @Title: loadStuEvalList
	 * @Description: 学生信息中 综合素质测评 分页查询
	 * @param model
	 * @param request
	 * @param response
	 * @param studentId
	 * @return
	 * @throws
	 */
	@RequestMapping(IntegrateConstant.INTEGRATE_STUDENT_INFO+ "/nsm/loadStuEvalList")
	public String loadStuEvalList(ModelMap model, HttpServletRequest request,HttpServletResponse response,String studentId)
	{
		int pageNo = request.getParameter("pageNo")!=null?Integer.parseInt(request.getParameter("pageNo")):1;
		Page page = this.evaluationCommonService.queryEvaluationPage(pageNo,10, studentId);
		model.addAttribute("page", page);
		StudentInfoModel studentInfo = new StudentInfoModel();
		studentInfo.setId(studentId);
		model.addAttribute("studentInfo", studentInfo);
		return IntegrateConstant.INTEGRATE_STUDENT_FTL + "/studentInfo/studentInfoEvaluation";
	}
	
	/**
	 * @Title: editStudent
	 * @Description: 学生信息修改跳转页面
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	@RequestMapping(IntegrateConstant.INTEGRATE_STUDENT_SELFINFO + "/opt-edit/editStudentInfo")
	public String editStudent(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		
		log.info("Controller:StudentInfoController;方法:editStudent()");
		
		String currentStudentId = sessionUtil.getCurrentUserId();
		if (StringUtils.hasText(currentStudentId)) {
			// 下拉列表 性别
			List<Dic> genderDicList = dicUtil.getDicInfoList("GENDER");
			// 下拉列表 证件类型
			List<Dic> certificateTypeDic = dicUtil.getDicInfoList("CARDTYPE");
			// 下拉列表 户口类型
			List<Dic> addressTypeDic = dicUtil.getDicInfoList("ACCOUNT_TYPE");
			// 下拉列表 宗教信仰
			List<Dic> religionDic = dicUtil.getDicInfoList("CREED");
			// 下拉列表 血型
			List<Dic> bloodTypeDic = dicUtil.getDicInfoList("BLOOD_TYPE");
			// 下拉列表 血型
			List<Dic> nativeDic = dicUtil.getDicInfoList("NATIVE");

			//通过登录人Id（学号）查询学生的修改信息
			StudentUpdateInfoModel studentUpdate=studentManageService.queryStudentUpdateByNumStatus(currentStudentId,"DELETE");
			if(null!=studentUpdate){
				//StudentUpdateInfoModel studentInfo=studentManageService.queryStudentUpdateByNumStatus(currentStudentId,"DELETE");
				model.addAttribute("studentInfo", studentUpdate);
        		StudentGuardianUpdateModel studentGuardianUpdate1=(StudentGuardianUpdateModel) studentManageService.queryStudentGuardianUpdate(studentUpdate.getId(),"1","DELETE");
            	StudentGuardianUpdateModel studentGuardianUpdate2=(StudentGuardianUpdateModel) studentManageService.queryStudentGuardianUpdate(studentUpdate.getId(),"2","DELETE");
            	StudentGuardianUpdateModel studentGuardianUpdate3=(StudentGuardianUpdateModel) studentManageService.queryStudentGuardianUpdate(studentUpdate.getId(),"3","DELETE");
            	model.addAttribute("studentGuardianUpdate1", studentGuardianUpdate1);
            	model.addAttribute("studentGuardianUpdate2", studentGuardianUpdate2);
            	model.addAttribute("studentGuardianUpdate3", studentGuardianUpdate3);
			}else{
				StudentInfoModel studentInfo = studentCommonService.queryStudentById(currentStudentId);
				studentInfo.setId(null);
				StudentGuardianModel studentGuardian1=studentCommonService.queryStudentGuardianByStudentNo(studentInfo.getStuNumber(), "1");
				StudentGuardianModel studentGuardian2=studentCommonService.queryStudentGuardianByStudentNo(studentInfo.getStuNumber(), "2");
				StudentGuardianModel studentGuardian3=studentCommonService.queryStudentGuardianByStudentNo(studentInfo.getStuNumber(), "3");
				model.addAttribute("studentGuardianUpdate1", studentGuardian1);
            	model.addAttribute("studentGuardianUpdate2", studentGuardian2);
            	model.addAttribute("studentGuardianUpdate3", studentGuardian3);
				if(studentInfo.getStatus()==null){
					studentInfo.setStatus("");
				}
				model.addAttribute("studentInfo", studentInfo);
			}
			model.addAttribute("genderDicList", genderDicList);
			model.addAttribute("certificateTypeDic", certificateTypeDic);
			model.addAttribute("addressTypeDic", addressTypeDic);
			model.addAttribute("religionDic", religionDic);
			model.addAttribute("bloodTypeDic", bloodTypeDic);
			model.addAttribute("nativeDic", nativeDic);
		}
		return IntegrateConstant.INTEGRATE_STUDENT_FTL + "/studentInfoEdit";
	}
	
	/**
	 * 
	 * @Title: saveStudent
	 * @Description: 学生修改信息 保存
	 * @param model
	 * @param request
	 * @param studentModel
	 * @return
	 * @throws
	 */
	@RequestMapping(IntegrateConstant.INTEGRATE_STUDENT_SELFINFO + "/opt-save/saveStudentInfo")
	public String saveStudent(StudentUpdateInfoModel studentUpdateModel, Errors errors, ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		    String flags = request.getParameter("flags");
		    log.info("Controller:StudentInfoController;方法:学生修改信息保存saveStudent()");
		    StudentInfoModel student=studentCommonService.queryStudentByStudentNo((studentUpdateModel!=null&&studentUpdateModel.getStuId()!=null)?studentUpdateModel.getStuId().getStuNumber():null);
		    String currentStudentId = sessionUtil.getCurrentUserId();
		    StudentGuardianUpdateModel studentGuardianUpdate1=new StudentGuardianUpdateModel();
		    StudentGuardianUpdateModel studentGuardianUpdate2=new StudentGuardianUpdateModel();
		    StudentGuardianUpdateModel studentGuardianUpdate3=new StudentGuardianUpdateModel();
		    //studentGuardianUpdate1.setStudentInfo(student);
		    studentGuardianUpdate1.setGuardianName(request.getParameter("guardianName1"));
		    studentGuardianUpdate1.setGuardianPhone(request.getParameter("guardianPhone1"));
		    studentGuardianUpdate1.setGuardianEmail(request.getParameter("guardianEmail1"));
		    studentGuardianUpdate1.setGuardianPostCode(request.getParameter("guardianPostCode1"));
		    studentGuardianUpdate1.setGuardianWorkUnit(request.getParameter("guardianWorkUnit1"));
		    studentGuardianUpdate1.setGuardianAddress(request.getParameter("guardianAddress1"));
		    studentGuardianUpdate1.setSeqNum("1");
		    studentGuardianUpdate1.setStatus("SAVE");
		    //studentGuardianUpdate2.setStudentInfo(student);
		    studentGuardianUpdate2.setGuardianName(request.getParameter("guardianName2"));
		    studentGuardianUpdate2.setGuardianPhone(request.getParameter("guardianPhone2"));
		    studentGuardianUpdate2.setGuardianEmail(request.getParameter("guardianEmail2"));
		    studentGuardianUpdate2.setGuardianPostCode(request.getParameter("guardianPostCode2"));
		    studentGuardianUpdate2.setGuardianWorkUnit(request.getParameter("guardianWorkUnit2"));
		    studentGuardianUpdate2.setGuardianAddress(request.getParameter("guardianAddress2"));
		    studentGuardianUpdate2.setSeqNum("2");
		    studentGuardianUpdate2.setStatus("SAVE");
		    //studentGuardianUpdate3.setStudentInfo(student);
		    studentGuardianUpdate3.setGuardianName(request.getParameter("guardianName3"));
		    studentGuardianUpdate3.setGuardianPhone(request.getParameter("guardianPhone3"));
		    studentGuardianUpdate3.setGuardianEmail(request.getParameter("guardianEmail3"));
		    studentGuardianUpdate3.setGuardianPostCode(request.getParameter("guardianPostCode3"));
		    studentGuardianUpdate3.setGuardianWorkUnit(request.getParameter("guardianWorkUnit3"));
		    studentGuardianUpdate3.setGuardianAddress(request.getParameter("guardianAddress3"));
		    studentGuardianUpdate3.setSeqNum("3");
		    studentGuardianUpdate3.setStatus("SAVE");
			//学生已修改时
			log.debug("学生修改信息更新操作!");
			StudentUpdateInfoModel studentUpdateInfo = this.studentManageService.queryStudentUpdateById(studentUpdateModel.getId());
		//	StudentUpdateInfoModel studentUpdateInfo = this.studentManageService.queryStudentUpdateByNumStatus(studentUpdateModel.getStuId().getStuNumber(),"DELETE");
			if("1".equalsIgnoreCase(flags)){
				studentUpdateModel.setStatus("SUBMIT");
			}else{
				studentUpdateModel.setStatus("SAVE");
			}
			if(!StringUtils.hasText(studentUpdateModel.getId())){
				//未保存的信息提交
				User user=new User();
				user.setId(currentStudentId);
				studentUpdateModel.setCreator(user);
				if(studentUpdateModel.getStuId()!=null){
					studentUpdateModel.setStuId(studentCommonService.queryStudentByStudentNo((studentUpdateModel!=null&&studentUpdateModel.getStuId()!=null)?studentUpdateModel.getStuId().getStuNumber():null));
				}
				List<StudentGuardianUpdateModel> list =new ArrayList<StudentGuardianUpdateModel>();
				list.add(0, studentGuardianUpdate1);
				list.add(1, studentGuardianUpdate2);
				list.add(2, studentGuardianUpdate3);
				this.studentManageService.saveStudentUpdateModel(studentUpdateModel,list);
			}else{
				studentUpdateModel.setStuId(student);
				BeanUtils.copyProperties(studentUpdateModel,studentUpdateInfo,new String[]{"creator","createTime","nextApprover","approveStatus","processStatus","approveReason"});
				List<StudentGuardianUpdateModel> list =new ArrayList<StudentGuardianUpdateModel>();
			    list.add(0, studentGuardianUpdate1);
				list.add(1, studentGuardianUpdate2);
				list.add(2, studentGuardianUpdate3);
				this.studentManageService.updateStudentUpdateModel(studentUpdateInfo,list);
			}
		return "redirect:"+IntegrateConstant.INTEGRATE_STUDENT_SELFINFO+ "/opt-query/queryStudentInfo.do";		
	}

	/**
	 * 提交信息
	 * @param model
	 * @param request
	 * @param workApply
	 * @param workApplyFile
	 * @param schedule
	 * @param fileId
	 * @param flags
	 * @return
	 */
	@RequestMapping(value = {IntegrateConstant.INTEGRATE_STUDENT_SELFINFO+"/opt-save/submitStudentInfo" },produces = { "text/plain;charset=UTF-8"})
	@ResponseBody
	public String submitUpadateStudent(ModelMap model, HttpServletRequest request, StudentUpdateInfoModel studentUpdateModel, String flags){
		
		    String currentStudentId = sessionUtil.getCurrentUserId();
		    StudentGuardianUpdateModel studentGuardianUpdate1=new StudentGuardianUpdateModel();
		    StudentGuardianUpdateModel studentGuardianUpdate2=new StudentGuardianUpdateModel();
		    StudentGuardianUpdateModel studentGuardianUpdate3=new StudentGuardianUpdateModel();
		    //studentGuardianUpdate1.setStudentInfo(student);
		    studentGuardianUpdate1.setGuardianName(request.getParameter("guardianName1"));
		    studentGuardianUpdate1.setGuardianPhone(request.getParameter("guardianPhone1"));
		    studentGuardianUpdate1.setGuardianEmail(request.getParameter("guardianEmail1"));
		    studentGuardianUpdate1.setGuardianPostCode(request.getParameter("guardianPostCode1"));
		    studentGuardianUpdate1.setGuardianWorkUnit(request.getParameter("guardianWorkUnit1"));
		    studentGuardianUpdate1.setGuardianAddress(request.getParameter("guardianAddress1"));
		    studentGuardianUpdate1.setSeqNum("1");
		    studentGuardianUpdate1.setStatus("SAVE");
		    //studentGuardianUpdate2.setStudentInfo(student);
		    studentGuardianUpdate2.setGuardianName(request.getParameter("guardianName2"));
		    studentGuardianUpdate2.setGuardianPhone(request.getParameter("guardianPhone2"));
		    studentGuardianUpdate2.setGuardianEmail(request.getParameter("guardianEmail2"));
		    studentGuardianUpdate2.setGuardianPostCode(request.getParameter("guardianPostCode2"));
		    studentGuardianUpdate2.setGuardianWorkUnit(request.getParameter("guardianWorkUnit2"));
		    studentGuardianUpdate2.setGuardianAddress(request.getParameter("guardianAddress2"));
		    studentGuardianUpdate2.setSeqNum("2");
		    studentGuardianUpdate2.setStatus("SAVE");
		    //studentGuardianUpdate3.setStudentInfo(student);
		    studentGuardianUpdate3.setGuardianName(request.getParameter("guardianName3"));
		    studentGuardianUpdate3.setGuardianPhone(request.getParameter("guardianPhone3"));
		    studentGuardianUpdate3.setGuardianEmail(request.getParameter("guardianEmail3"));
		    studentGuardianUpdate3.setGuardianPostCode(request.getParameter("guardianPostCode3"));
		    studentGuardianUpdate3.setGuardianWorkUnit(request.getParameter("guardianWorkUnit3"));
		    studentGuardianUpdate3.setGuardianAddress(request.getParameter("guardianAddress3"));
		    studentGuardianUpdate3.setSeqNum("3");
		    studentGuardianUpdate3.setStatus("SAVE");
			//学生已修改时
			log.debug("学生修改信息更新操作!");
			StudentUpdateInfoModel studentUpdateInfo = this.studentManageService.queryStudentUpdateByNumStatus(studentUpdateModel.getStuId().getStuNumber(),"DELETE");
			studentUpdateModel.setStatus("SAVE");
			if(studentUpdateInfo==null){
				//未保存的信息提交
				User user=new User();
				user.setId(currentStudentId);
				studentUpdateModel.setCreator(user);
				if(studentUpdateModel.getStuId()!=null){
					studentUpdateModel.setStuId(studentCommonService.queryStudentByStudentNo(studentUpdateModel.getStuId().getStuNumber()));
				}
				List<StudentGuardianUpdateModel> list =new ArrayList<StudentGuardianUpdateModel>();
				list.add(0, studentGuardianUpdate1);
				list.add(1, studentGuardianUpdate2);
				list.add(2, studentGuardianUpdate3);
				this.studentManageService.saveStudentUpdateModel(studentUpdateModel,list);
			}else{
				studentUpdateModel.setStuId(studentCommonService.queryStudentByStudentNo(studentUpdateModel.getStuId().getStuNumber()));
				BeanUtils.copyProperties(studentUpdateModel,studentUpdateInfo,new String[]{"creator","createTime","nextApprover","approveStatus","processStatus","id","approveReason"});
				List<StudentGuardianUpdateModel> list =new ArrayList<StudentGuardianUpdateModel>();
				list.add(0, studentGuardianUpdate1);
				list.add(1, studentGuardianUpdate2);
				list.add(2, studentGuardianUpdate3);
				this.studentManageService.updateStudentUpdateModel(studentUpdateInfo,list);
			}
			//return studentUpdateModel.getId();
			return "redirect:"+IntegrateConstant.INTEGRATE_STUDENT_SELFINFO+ "/opt-query/queryStudentInfo.do";
	}
	/**------------审批流开始-----------*/
	/**
	 * 初始化当前流程
	 * @Title: saveCurProcess
	 * @Description: 初始化当前流程
	 * @param model
	 * @param request
	 * @param objectId			业务主键
	 * @param flags
	 * @param approveStatus		当前节点审批状态
	 * @param processStatusCode	流程当前状态
	 * @param nextApproverId	下一节点办理人
	 */
	@RequestMapping(value = {IntegrateConstant.INTEGRATE_STUDENT_SELFINFO+"/opt-add/saveCurProcess"},produces = { "text/plain;charset=UTF-8" })
	@ResponseBody
	public String saveCurProcess(ModelMap model,HttpServletRequest request,String objectId,String nextApproverId){
		ApproveResult result = new ApproveResult();
		if(ProjectConstants.IS_APPROVE_ENABLE){
			try {
				User initiator = new User(this.sessionUtil.getCurrentUserId());//封装发起人
				User nextApprover = new User(nextApproverId);//封装第一级审核人
				result = this.studentManageService.submitApprove(objectId, initiator, nextApprover);
				result = this.saveUpdateStudentApproveResult(objectId,result,nextApproverId);
				result.setResultFlag("success");
			} catch (Exception e) {
				result.setResultFlag("error");
			}
		}else{
			result.setResultFlag("deprecated");
	    }

		JSONObject json=JsonUtils.getJsonObject(result);
		return JsonUtils.jsonObject2Json(json);
	}
	
	/**
	 * 保存更新学生信息审批结果
	 * @Title: saveUpdateStudentApproveResult
	 * @Description: 保存学生信息审批结果
	 * @param objectId
	 * @param result
	 * @throws
	 */
	private ApproveResult saveUpdateStudentApproveResult(String objectId,ApproveResult result,String nextApproverId) {
		if(DataUtil.isNotNull(result)){
			//获取保存的学生信息
			StudentUpdateInfoModel studentUpdateInfo = this.studentManageService.queryStudentUpdateById(objectId);
			if(null == studentUpdateInfo)
			{
				studentUpdateInfo = new StudentUpdateInfoModel();
				studentUpdateInfo.setId(objectId);
				studentManageService.saveStudentUpdateModel(studentUpdateInfo,null);
			}
			//流程审批状态
			studentUpdateInfo.setApproveStatus(result.getApproveStatus());
			//流程实例状态
			studentUpdateInfo.setProcessStatus(result.getProcessStatusCode());
			if(DataUtil.isNotNull(nextApproverId)){
				//下一节点办理人
				User nextApprover = new User();
				nextApprover.setId(nextApproverId);
				studentUpdateInfo.setNextApprover(nextApprover);
			}else{
				studentUpdateInfo.setNextApprover(null);
			}
			//保存审批流回显的信息
			studentManageService.updateStudentUpdateModel(studentUpdateInfo,null);
		}
		
		return result;
	}
	/**------------审批流结束-----------*/
	
	/**
	 * 
	 * @Title: viewBaseStudent
	 * @Description: 学生信息查看 公用查询调用
	 * @param request
	 * @param id
	 * @return
	 * @throws
	 */
	@RequestMapping("/student/infoview/nsm/studentView")
	public String viewBaseStudent(ModelMap model, HttpServletRequest request, String id)
	{
		if (StringUtils.hasText(id))
		{
			StudentInfoModel studentInfo = studentCommonService.queryStudentById(id);
			model.addAttribute("studentInfo", studentInfo);
		}
		return "/integrate/student/common/viewStudent";
	}
	
}
