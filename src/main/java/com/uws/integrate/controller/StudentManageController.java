package com.uws.integrate.controller;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONObject;

import org.apache.commons.lang.ArrayUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.uws.apw.model.ApproveResult;
import com.uws.apw.model.FlowInstancePo;
import com.uws.apw.service.IFlowInstanceService;
import com.uws.apw.util.JsonUtils;
import com.uws.common.service.IActivityService;
import com.uws.common.service.IBaseDataService;
import com.uws.common.service.ICommonConfigService;
import com.uws.common.service.ICommonRoleService;
import com.uws.common.service.ICommonSponsorService;
import com.uws.common.service.IEvaluationCommonService;
import com.uws.common.service.IRewardCommonService;
import com.uws.common.service.IStuJobTeamSetCommonService;
import com.uws.common.service.IStudentCommonService;
import com.uws.comp.service.ICompService;
import com.uws.core.base.BaseController;
import com.uws.core.excel.ExcelException;
import com.uws.core.excel.ImportUtil;
import com.uws.core.excel.service.IExcelService;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.core.session.SessionFactory;
import com.uws.core.session.SessionUtil;
import com.uws.core.util.DataUtil;
import com.uws.core.util.DateUtil;
import com.uws.core.util.StringUtils;
import com.uws.domain.association.AssociationMemberModel;
import com.uws.domain.base.BaseAcademyModel;
import com.uws.domain.base.BaseClassModel;
import com.uws.domain.base.BaseMajorModel;
import com.uws.domain.base.StudentRoomModel;
import com.uws.domain.config.TimeConfigModel;
import com.uws.domain.integrate.ClassMonitorSetModel;
import com.uws.domain.integrate.StudentApproveSetModel;
import com.uws.domain.integrate.StudentArmyInfoModel;
import com.uws.domain.integrate.StudentCollegeCount;
import com.uws.domain.integrate.StudentGuardianUpdateModel;
import com.uws.domain.integrate.StudentSchoolCount;
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
import com.uws.sys.service.DicUtil;
import com.uws.sys.service.FileUtil;
import com.uws.sys.service.impl.DicFactory;
import com.uws.sys.service.impl.FileFactory;
import com.uws.sys.util.MultipartFileValidator;
import com.uws.user.model.User;
import com.uws.user.model.UserRole;
import com.uws.util.CheckUtils;
import com.uws.util.ProjectConstants;
import com.uws.util.ProjectSessionUtils;

/**
 * 
 * @ClassName: StudentManageController
 * @Description: 学生信息管理控制 Controller
 * @author 联合永道
 * @date 2015-7-27 上午9:41:21
 * 
 */
@Controller
public class StudentManageController extends BaseController {

	// 日志
	private Logger log = new LoggerFactory(StudentManageController.class);
	// 学生管理service
	@Autowired
	private IStudentManageService studentManageService;
	// 基础数据service
	@Autowired
	private IBaseDataService baseDataService;
	// 时间设置service
	@Autowired
	private ICommonConfigService commonConfigService;
	@Autowired
	private ICommonRoleService commonRoleService;
	@Autowired
	private IStudentCommonService studentCommonService;
	//综合测评
	@Autowired
	private IEvaluationCommonService evaluationCommonService;
	//奖惩助信息
	@Autowired
	private IRewardCommonService rewardCommonService;
	//社团信息
	@Autowired
	private IActivityService activityService;
	//贫困生信息
	@Autowired
	private ICommonSponsorService commonSponsorService;
	//组件封装service
	@Autowired
	private ICompService compService;
	//审批 Service
	@Autowired
	private IFlowInstanceService flowInstanceService;
	@Autowired
	private IExcelService excelService;
	@Autowired
	private IStuJobTeamSetCommonService stuJobTeamSetCommonService; 
    //数据字典
	private DicUtil dicUtil = DicFactory.getDicUtil();
	// sessionUtil工具类
	private SessionUtil sessionUtil = SessionFactory.getSession(IntegrateConstant.INTEGRATE_STUDENT_INFO);
	// fileUtil工具类
	private FileUtil fileUtil=FileFactory.getFileUtil();
	//教工信息工具类
	@Autowired
	private IStuJobTeamSetCommonService istuJobTeamSetCommonService;
	@InitBinder
    protected void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }
	

	/**
	 * 
	 * @Title: listStudent
	 * @Description: 学生查询列表
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */

	@RequestMapping(IntegrateConstant.INTEGRATE_STUDENT_INFO + "/opt-query/listStudent")
	public String listStudent(ModelMap model, HttpServletRequest request, HttpServletResponse response, StudentInfoModel studentInfo) {
		log.info("Controller:StudentManageController;方法:学生查询列表listStudent()");
        String teacherOrgId= ProjectSessionUtils.getCurrentTeacherOrgId(request);
        boolean isStudent = ProjectSessionUtils.checkIsStudent(request);
        String userId = sessionUtil.getCurrentUserId();
        UserRole useRole = commonRoleService.getRoleByCode(userId,"HKY_COLLEGE_DIRECTOR");
		List<BaseClassModel> classesList = this.studentManageService.queryClassByTeacher(sessionUtil.getCurrentUserId());		
		if(null !=useRole && null !=useRole.getRole() && StringUtils.hasText(useRole.getRole().getId()) && "HKY_COLLEGE_DIRECTOR".equals(useRole.getRole().getCode()))
		{
			BaseAcademyModel college = this.baseDataService.findAcademyById(teacherOrgId);
	        studentInfo.setCollege(college);
	        model.addAttribute("orgStatus", "ture");
	        classesList = null;
		}
		
		if(null != classesList && classesList.size()>0)
		{   
			model.addAttribute("classesList", classesList);
		}
		
		if(CheckUtils.isCurrentOrgEqCollege(teacherOrgId) && (!(ProjectConstants.STUDNET_OFFICE_ORG_ID).equals(teacherOrgId) || !(ProjectConstants.JOBOFFICEID).equals(teacherOrgId)))
        {
	        BaseAcademyModel college = this.baseDataService.findAcademyById(teacherOrgId);
	        studentInfo.setCollege(college);
	        model.addAttribute("orgStatus", "ture");
        }
	    
		int pageNo = request.getParameter("pageNo") != null ? Integer .valueOf(request.getParameter("pageNo")) : 1;
		Page page = studentManageService.pagedQueryStudentInfo(pageNo, Page.DEFAULT_PAGE_SIZE, studentInfo, sessionUtil.getCurrentUserId() ,teacherOrgId,isStudent,classesList,useRole);
		// 下拉列表 学院
		List<BaseAcademyModel> collegeList = baseDataService.listBaseAcademy();
		// 下拉列表 专业
		List<BaseMajorModel> majorList =null;
		if (null != studentInfo && null != studentInfo.getCollege()&& null != studentInfo.getCollege().getId()&& studentInfo.getCollege().getId().length() > 0) {
			majorList = compService.queryMajorByCollage(studentInfo.getCollege().getId());
			log.debug("若已经选择学院，则查询学院下的专业信息.");
		}
		// 下拉列表 班级
		List<BaseClassModel> classList =null;
		if (null != studentInfo && null != studentInfo.getClassId() && null != studentInfo.getMajor() && null != studentInfo.getMajor().getId() && studentInfo.getMajor().getId().length() > 0) {
			classList = compService.queryClassByMajor(studentInfo.getMajor().getId());
			log.debug("若已经选择专业，则查询专业下的班级信息.");
		}
		
		model.addAttribute("collegeList", collegeList);
		model.addAttribute("genderDicList", dicUtil.getDicInfoList("GENDER"));
		model.addAttribute("nationDicList", dicUtil.getDicInfoList("NATION"));//民族
		model.addAttribute("politicalDicList", dicUtil.getDicInfoList("SCH_POLITICAL_STATUS"));//政治面貌
		model.addAttribute("page", page);
		model.addAttribute("majorList", majorList);
		model.addAttribute("classList", classList);
		model.addAttribute("statusMap", ProjectConstants.getStatusMap());
		model.addAttribute("gradeList", baseDataService.listGradeList());
		model.addAttribute("isStudent", isStudent);
		model.addAttribute("studentInfo", studentInfo);
		
		return IntegrateConstant.INTEGRATE_STUDENT_FTL + "/studentList";
	}

	/**
	 * 学生信息查询
	 * 
	 * @param model
	 * @param request
	 * @param response
	 * @return StudentInfoModel
	 */
	@RequestMapping(IntegrateConstant.INTEGRATE_STUDENT_INFO + "/opt-query/queryStudent")
	public String queryStudent(ModelMap model, HttpServletRequest request,HttpServletResponse response) {
		
		log.info("Controller:StudentManageController;方法:学生信息查询queryStudent()");

		String id = (String) request.getParameter("id");		
		if (StringUtils.hasText(id)) {
			StudentInfoModel studentInfo = studentCommonService.queryStudentById(id);
			StudentGuardianModel studentGuardian1=studentCommonService.queryStudentGuardianByStudentNo(studentInfo.getStuNumber(), "1");
			StudentGuardianModel studentGuardian2=studentCommonService.queryStudentGuardianByStudentNo(studentInfo.getStuNumber(), "2");
			StudentGuardianModel studentGuardian3=studentCommonService.queryStudentGuardianByStudentNo(studentInfo.getStuNumber(), "3");
			model.addAttribute("studentGuardianUpdate1", studentGuardian1);
        	model.addAttribute("studentGuardianUpdate2", studentGuardian2);
        	model.addAttribute("studentGuardianUpdate3", studentGuardian3);
			//学生住宿信息
			StudentRoomModel studentRoom=baseDataService.findRoomByStudentId(id);
			if(studentRoom!=null && studentRoom.getRoom()!=null){
				List<StudentRoomModel> studentRoomList=baseDataService.findRoomByRoomId(studentRoom.getRoom().getId(),id);
				model.addAttribute("studentRoomList", studentRoomList);
			}
			//奖惩信息 奖 惩 助
			List<StudentApplyInfo> studentApplyInfoList=rewardCommonService.getStuAwardList(studentInfo);
			List<PunishInfo> PunishInfoList=rewardCommonService.getStuPunishList(studentInfo);
			List<CollegeAwardInfo> collegeAwardList=rewardCommonService.getStuCollegeAwardList(studentInfo);
			List<CountryBurseInfo> countryBurseInfoList=rewardCommonService.getStuBurseList(studentInfo);
			//社团信息
			List<AssociationMemberModel> associationMemberList=activityService.queryAssociationMemberByMemberId(studentInfo.getId());
			//贫困生信息
			List<DifficultStudentInfo> difficultList=commonSponsorService.queryDifficultStudentList(studentInfo.getStuNumber());
			//综合测评信息
			int pageNo = request.getParameter("pageNo")!=null?Integer.parseInt(request.getParameter("pageNo")):1;
			Page page = this.evaluationCommonService.queryEvaluationPage(pageNo,10, id);
			if(studentInfo.getClassId()!=null && studentInfo.getClassId().getHeadermaster()!=null){
				TeacherInfoModel teacher=istuJobTeamSetCommonService.getTeacherInfoByBTId(studentInfo.getClassId().getHeadermaster().getId());
    			model.addAttribute("teacher", teacher);
			}
			model.addAttribute("processStatusMap", ProjectSessionUtils.getApproveProcessStatus());
			model.addAttribute("associationMemberList", associationMemberList);
			model.addAttribute("difficultList", difficultList);
			model.addAttribute("studentApplyInfoList", studentApplyInfoList);
			model.addAttribute("PunishInfoList", PunishInfoList);
			model.addAttribute("countryBurseInfoList", countryBurseInfoList);
			model.addAttribute("collegeAwardList", collegeAwardList);
			model.addAttribute("studentInfo", studentInfo);
			model.addAttribute("studentRoom", studentRoom);
			model.addAttribute("page",page);
		}
		return IntegrateConstant.INTEGRATE_STUDENT_FTL + "/studentDetailView";
	}

	/**
	 * @Title: editStudent
	 * @Description: 老师修改学生信息跳转页面
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	@RequestMapping(IntegrateConstant.INTEGRATE_STUDENT_INFO + "/opt-edit/editStudentInfo")
	public String editStudent(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		
		log.info("Controller:StudentManageController;方法:editStudent()");
		
		String id = (String) request.getParameter("id");		
		if (StringUtils.hasText(id)) {
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
			StudentInfoModel studentInfo = studentCommonService.queryStudentById(id);
			StudentGuardianModel studentGuardian1=studentCommonService.queryStudentGuardianByStudentNo(studentInfo.getStuNumber(), "1");
			StudentGuardianModel studentGuardian2=studentCommonService.queryStudentGuardianByStudentNo(studentInfo.getStuNumber(), "2");
			StudentGuardianModel studentGuardian3=studentCommonService.queryStudentGuardianByStudentNo(studentInfo.getStuNumber(), "3");
			model.addAttribute("studentGuardian1", studentGuardian1);
        	model.addAttribute("studentGuardian2", studentGuardian2);
        	model.addAttribute("studentGuardian3", studentGuardian3);
			model.addAttribute("studentInfo", studentInfo);
			model.addAttribute("genderDicList", genderDicList);
			model.addAttribute("certificateTypeDic", certificateTypeDic);
			model.addAttribute("addressTypeDic", addressTypeDic);
			model.addAttribute("religionDic", religionDic);
			model.addAttribute("bloodTypeDic", bloodTypeDic);
			model.addAttribute("nativeDic", nativeDic);
		}
		return IntegrateConstant.INTEGRATE_STUDENT_FTL + "/studentInfoByTeachEdit";
	}
	/**
	 * 
	 * @Title: submitStudent
	 * @Description: 老师修改学生信息跳转页面保存
	 * @param model
	 * @param request
	 * @param studentModel
	 * @return
	 * @throws
	 */
	@RequestMapping(IntegrateConstant.INTEGRATE_STUDENT_INFO + "/opt-save/submitStudentInfo")
	public String submitStudent(StudentInfoModel studentInfo, HttpServletRequest request, Errors errors , ModelMap model) {
		
	    log.info("Controller:StudentManageController;方法:老师修改学生信息保存submitStudent()");
		log.debug("老师对学生信息进行更新操作!");
		String currentStudentId = sessionUtil.getCurrentUserId();
		User user=new User();
		user.setId(currentStudentId);
		studentInfo.setUpdater(user);
		StudentInfoModel student = studentCommonService.queryStudentById(studentInfo.getId());
		if(student!=null){
			StudentGuardianModel studentGuardianUpdate1=new StudentGuardianModel();
			StudentGuardianModel studentGuardianUpdate2=new StudentGuardianModel();
			StudentGuardianModel studentGuardianUpdate3=new StudentGuardianModel();
		    
		    studentGuardianUpdate1.setGuardianName(request.getParameter("guardianName1"));
		    studentGuardianUpdate1.setGuardianPhone(request.getParameter("guardianPhone1"));
		    studentGuardianUpdate1.setGuardianEmail(request.getParameter("guardianEmail1"));
		    studentGuardianUpdate1.setGuardianPostCode(request.getParameter("guardianPostCode1"));
		    studentGuardianUpdate1.setGuardianWorkUnit(request.getParameter("guardianWorkUnit1"));
		    studentGuardianUpdate1.setGuardianAddress(request.getParameter("guardianAddress1"));
		    studentGuardianUpdate1.setSeqNum("1");
		    studentGuardianUpdate2.setGuardianName(request.getParameter("guardianName2"));
		    studentGuardianUpdate2.setGuardianPhone(request.getParameter("guardianPhone2"));
		    studentGuardianUpdate2.setGuardianEmail(request.getParameter("guardianEmail2"));
		    studentGuardianUpdate2.setGuardianPostCode(request.getParameter("guardianPostCode2"));
		    studentGuardianUpdate2.setGuardianWorkUnit(request.getParameter("guardianWorkUnit2"));
		    studentGuardianUpdate2.setGuardianAddress(request.getParameter("guardianAddress2"));
		    studentGuardianUpdate2.setSeqNum("2");
		    studentGuardianUpdate3.setGuardianName(request.getParameter("guardianName3"));
		    studentGuardianUpdate3.setGuardianPhone(request.getParameter("guardianPhone3"));
		    studentGuardianUpdate3.setGuardianEmail(request.getParameter("guardianEmail3"));
		    studentGuardianUpdate3.setGuardianPostCode(request.getParameter("guardianPostCode3"));
		    studentGuardianUpdate3.setGuardianWorkUnit(request.getParameter("guardianWorkUnit3"));
		    studentGuardianUpdate3.setGuardianAddress(request.getParameter("guardianAddress3"));
		    studentGuardianUpdate3.setSeqNum("3");
		   
		    List<StudentGuardianModel> list =new ArrayList<StudentGuardianModel>();
			list.add(0, studentGuardianUpdate1);
			list.add(1, studentGuardianUpdate2);
			list.add(2, studentGuardianUpdate3);
		    BeanUtils.copyProperties(studentInfo,student,new String[]{"stuNumber","studentType","passWord","name","certificateTypeDic","certificateCode","graduation","enterScore","college","subjectDic","major","classId","dorm","politicalDic","sourceLand","urlStr","marriageDic","overChineseDic","healthStateDic","partyApp","partyStudy","comments","reportDate","reportSiteDic","costState","greenWay","greenReason","cancelReason","enterYearDic","edusStatus","status","collectState","createTime","creator","enterDate"});
		    
		    this.studentManageService.updateStudentInfoModel(student,list);
		}
	    return "redirect:"+IntegrateConstant.INTEGRATE_STUDENT_INFO + "/opt-query/listStudent.do";		
    }
	
	/**
	 * 班级列表查询页面（在页面里修改审核班长）
	 * @param model
	 * @param request
	 * @param baseClass
	 * @return BaseClassModel
	 */
	@RequestMapping(IntegrateConstant.INTEGRATE_STUDENT_SET + "/opt-query/classMonitorSetList")
	public String queryBaseClass(ModelMap model, HttpServletRequest request,ClassMonitorSetModel classMonitor) {
		
		log.info("Controller:StudentManageController;方法:班级列表查询页面（在页面里修改审核班长）queryBaseClass()");
		Page page =new Page();
		Integer pageNo = request.getParameter("pageNo") != null ? Integer.valueOf(request.getParameter("pageNo")) : 1;
		//教学辅导员或班主任
		String currentStudentId = sessionUtil.getCurrentUserId();
		List<BaseClassModel> classList=null;
    	List<BaseClassModel> classList1=stuJobTeamSetCommonService.queryBaseClassModelByTCId(currentStudentId);
    	if(classList1.size()>0){
        	page = studentManageService.pagedQueryBaseClass(Page.DEFAULT_PAGE_SIZE, pageNo, classMonitor,currentStudentId);
    	}else{
			model.addAttribute("teacher", "false");
    		page = studentManageService.pagedQueryBaseClass(Page.DEFAULT_PAGE_SIZE, pageNo, classMonitor,null);
        }
		// 下拉列表 学院
		List<BaseAcademyModel> collageList = baseDataService.listBaseAcademy();
		List<BaseMajorModel> majorList = null;
		if (null != classMonitor) {
			if (null != classMonitor.getMajor()&& null != classMonitor.getMajor().getCollage()&& classMonitor.getMajor().getCollage().getId().length() > 0) {
				majorList = compService.queryMajorByCollage(classMonitor.getMajor().getCollage().getId());
				log.debug("若已经选择学院，则查询学院下的专业信息.");
			}
			// 下拉列表 班级
			if (null != classMonitor.getId() && null != classMonitor.getMajor()&& null != classMonitor.getMajor().getId()&& classMonitor.getMajor().getId().length() > 0) {
				classList = compService.queryClassByMajor(classMonitor.getMajor().getId());
				log.debug("若已经选择专业，则查询专业下的班级信息.");
			}
		}
		//时间设置实体类
		TimeConfigModel timeConfigModel=commonConfigService.findByCondition(IntegrateConstant.UPDATE_TIME_CONFIG_CODE);
		model.addAttribute("page", page);
		model.addAttribute("collageList", collageList);
		model.addAttribute("majorList", majorList);
		model.addAttribute("classList", classList);
		model.addAttribute("classMonitor", classMonitor);
		model.addAttribute("timeConfigModel", timeConfigModel);
		model.addAttribute("gradeList", baseDataService.listGradeList());
		
		return IntegrateConstant.INTEGRATE_STUDENT_FTL + "/classMonitorSetList";
	}

	/**
	 * 修改审核班长
	 * 
	 * @param baseClass
	 * @param model
	 * @param fileId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value={IntegrateConstant.INTEGRATE_STUDENT_SET + "/opt-edit/editMonitor"} , produces={"text/plain;charset=UTF-8"})
	public String updateMonitor( String orginStudentId ,String studentId, String classId) {

		log.info("Controller:StudentManageController;方法:修改审核班长updateMonitor()");

		StudentApproveSetModel studentApprove = studentManageService.queryApproveByClassId(classId);
		
		StudentInfoModel student = new StudentInfoModel();
		student.setId(studentId);
		if (null != studentApprove) {
			studentApprove.setStudentId(student);
			studentApprove.setStatus(dicUtil.getStatusNormal());
			studentManageService.editMonitor(studentApprove,orginStudentId);
		} else {
			BaseClassModel baseClass = new BaseClassModel();
			baseClass.setId(classId);
			studentApprove = new StudentApproveSetModel();
			studentApprove.setStatus(dicUtil.getStatusNormal());
			studentApprove.setClassId(baseClass);
			studentApprove.setStudentId(student);
			studentManageService.addMonitor(studentApprove);
		}
		return "success";
	}
	
	/**
	 * 
	 * @Title: listApproveStudent
	 * @Description: 需要审核的学生信息列表
	 * @param model
	 * @param request
	 * @param studentModel
	 * @return
	 * @throws
	 */
	@RequestMapping(IntegrateConstant.INTEGRATE_STUDENT_MANAGE + "/opt-query/listApproveStudent")
	public String listApproveStudent(ModelMap model, HttpServletRequest request, HttpServletResponse response, StudentUpdateInfoModel studentUpdateInfo, String userId) {

		String currentStudentId = sessionUtil.getCurrentUserId();
		List<BaseClassModel> classList=null;
		boolean bol = ProjectSessionUtils.checkIsTeacher(request);
        if(bol){
        	log.debug("教师审核操作...");
        	classList=stuJobTeamSetCommonService.queryBaseClassModelByTCId(currentStudentId);
        	if(classList.size()>0){
            	model.addAttribute("classList", classList);
        	}
        }
		
        int pageNo = request.getParameter("pageNo") != null ? Integer .valueOf(request.getParameter("pageNo")) : 1;
		String[] objectIds = flowInstanceService.getObjectIdByProcessKey(IntegrateConstant.STUDENT_APPROVE_FLOW_KEY,currentStudentId);
		
		Page page = studentManageService.pagedQueryUpdateStudentInfo(pageNo, Page.DEFAULT_PAGE_SIZE, studentUpdateInfo, currentStudentId,objectIds,bol);
		model.addAttribute("page", page);
		model.addAttribute("isApprove",this.isApprove(studentUpdateInfo.getId(), currentStudentId));
		// request传递查询条件
		model.addAttribute("processStatusMap", ProjectSessionUtils.getApproveProcessStatus());
		model.addAttribute("studentUpdateInfo", studentUpdateInfo);
		model.addAttribute("currentStudentId", currentStudentId);
		model.addAttribute("isTeacher", bol);
		
		return IntegrateConstant.INTEGRATE_STUDENT_FTL + "/approveStudentList";
	}
	
	/**
	 * 学生审核信息查询
	 * 
	 * @param model
	 * @param request
	 * @param response
	 * @return StudentInfoModel
	 */
	@RequestMapping(IntegrateConstant.INTEGRATE_STUDENT_MANAGE + "/opt-query/editApproveStudent")
	public String editApproveStudent(ModelMap model, HttpServletRequest request,HttpServletResponse response) {
		
		log.info("Controller:StudentManageController;方法:学生审核信息查询editApproveStudent()");

		String id = (String) request.getParameter("id");
		if (StringUtils.hasText(id)) {
			StudentUpdateInfoModel studentInfo = studentManageService.queryStudentUpdateById(id);
			StudentGuardianUpdateModel studentGuardianUpdate1=(StudentGuardianUpdateModel) studentManageService.queryStudentGuardianUpdate(studentInfo.getId(),"1",null);
        	StudentGuardianUpdateModel studentGuardianUpdate2=(StudentGuardianUpdateModel) studentManageService.queryStudentGuardianUpdate(studentInfo.getId(),"2",null);
        	StudentGuardianUpdateModel studentGuardianUpdate3=(StudentGuardianUpdateModel) studentManageService.queryStudentGuardianUpdate(studentInfo.getId(),"3",null);
        	model.addAttribute("studentGuardianUpdate1", studentGuardianUpdate1);
        	model.addAttribute("studentGuardianUpdate2", studentGuardianUpdate2);
        	model.addAttribute("studentGuardianUpdate3", studentGuardianUpdate3);
			model.addAttribute("studentInfo", studentInfo);
		}
		return IntegrateConstant.INTEGRATE_STUDENT_FTL + "/approveStudentViewEdit";
	}
	
	/**
	 * 学生审核信息查询
	 * 
	 * @param model
	 * @param request
	 * @param response
	 * @return StudentInfoModel
	 */
	@RequestMapping(IntegrateConstant.INTEGRATE_STUDENT_MANAGE + "/opt-query/queryApproveStudent")
	public String queryApproveStudent(ModelMap model, HttpServletRequest request,HttpServletResponse response) {
		
		log.info("Controller:StudentManageController;方法:学生审核信息查询queryApproveStudent()");

		String id = (String) request.getParameter("id");
		if (StringUtils.hasText(id)) {
			StudentUpdateInfoModel studentInfo = studentManageService.queryStudentUpdateById(id);
			StudentGuardianUpdateModel studentGuardianUpdate1=(StudentGuardianUpdateModel) studentManageService.queryStudentGuardianUpdate(studentInfo.getId(),"1",null);
        	StudentGuardianUpdateModel studentGuardianUpdate2=(StudentGuardianUpdateModel) studentManageService.queryStudentGuardianUpdate(studentInfo.getId(),"2",null);
        	StudentGuardianUpdateModel studentGuardianUpdate3=(StudentGuardianUpdateModel) studentManageService.queryStudentGuardianUpdate(studentInfo.getId(),"3",null);
        	model.addAttribute("studentGuardianUpdate1", studentGuardianUpdate1);
        	model.addAttribute("studentGuardianUpdate2", studentGuardianUpdate2);
        	model.addAttribute("studentGuardianUpdate3", studentGuardianUpdate3);
			model.addAttribute("studentInfo", studentInfo);
		}
		return IntegrateConstant.INTEGRATE_STUDENT_FTL + "/approveStudentView";
	}
	
	/**
	 * 
	 * @Title: submitApproveStudent
	 * @Description: 提交审核信息
	 * @param model
	 * @param request
	 * @param StudentUpdateInfoModel
	 * @return
	 * @throws
	 */
	@RequestMapping(IntegrateConstant.INTEGRATE_STUDENT_MANAGE + "/opt-save/submitApproveStudent")
	public String submitApproveStudent(StudentUpdateInfoModel studentUpdateModel,HttpServletRequest request,ModelMap model) {
		
		log.info("Controller:StudentInfoController;方法:提交审核信息submitApproveStudent()");

		String currentStudentId = sessionUtil.getCurrentUserId();
		log.info("当前登录人id为"+currentStudentId);
			String id = request.getParameter("id");
			StudentUpdateInfoModel studentUpdate=studentManageService.queryStudentUpdateById(id);
	        BeanUtils.copyProperties(studentUpdateModel,studentUpdate,new String[]{"createTime","id","stuNumber","creator","status","nextApprover","approveStatus","processStatus"});
			
			this.studentManageService.updateStudentUpdateModel(studentUpdate,null);
            return "redirect:"+IntegrateConstant.INTEGRATE_STUDENT_MANAGE+ "/opt-query/queryApproveStudent.do";
	}
	
	/**
	 * 
	 * @Title: 
	 * @Description: 批量审核信息保存。
	 * @param model
	 * @param request
	 * @return
	 * @throws
	 */
	@RequestMapping({IntegrateConstant.INTEGRATE_STUDENT_MANAGE +"/opt-save/submitApproveStudents"})
	public String submitApproveStudents(ModelMap model,HttpServletRequest request,String ids,String status){
		if(ids != null && !"".equals(ids)){
			String[] id = ids.split(",");
			FlowInstancePo instancePo = new FlowInstancePo();
			StudentUpdateInfoModel studentUpdateInfo = new StudentUpdateInfoModel();
			for(String i:id){
				studentUpdateInfo = this.studentManageService.queryStudentUpdateById(i);
			}
			if(StringUtils.hasText(status) && status.equals("pass"))
			{
				//默认给出审批通过意见
			   	instancePo.setSuggest("审批通过");
				instancePo.setApproveResultDic(this.dicUtil.getDicInfo("ROD_APPROVE_STATUS", "PASS"));//审批通过
				studentUpdateInfo.setApproveReason("审核通过");
			}
			else
			{
				//默认给出审批不通过意见
			   	instancePo.setSuggest("审批不通过");
			   	instancePo.setApproveResultDic(this.dicUtil.getDicInfo("ROD_APPROVE_STATUS", "NOT_PASS"));//不通过
			   	studentUpdateInfo.setApproveReason("审核不通过");
			}
			this.studentManageService.updateStudentUpdateModel(studentUpdateInfo,null);		
		}
		return "redirect:/student/manage/opt-query/listApproveStudent.do";
	}
	
	
	/**
	 * 
	 * @Title: listStudentArmy
	 * @Description: 参军学生列表
	 * @param model
	 * @param request
	 * @param studentArmyInfoModel
	 * @return
	 * @throws
	 */
	@RequestMapping(IntegrateConstant.INTEGRATE_STUDENT_ARMY + "/opt-query/listStudentArmy")
	public String listStudentArmy(ModelMap model, HttpServletRequest request, HttpServletResponse response, StudentArmyInfoModel studentArmyInfo, String userId) {

		log.info("Controller:StudentManageController;方法:查询参军学生列表listStudentArmy()");
		int pageNo = request.getParameter("pageNo") != null ? Integer .valueOf(request.getParameter("pageNo")) : 1;
		Page page = studentManageService.pagedQueryStudentArmy(pageNo, Page.DEFAULT_PAGE_SIZE, studentArmyInfo, userId);
		// 下拉列表 性别
		List<Dic> genderDicList = dicUtil.getDicInfoList("GENDER");
		// 下拉列表 学院
		List<BaseAcademyModel> collegeList = baseDataService.listBaseAcademy();
		// 下拉列表 专业
		List<BaseMajorModel> majorList = null;
		if (null != studentArmyInfo && null !=studentArmyInfo.getStudent() && null != studentArmyInfo.getStudent().getMajor()&& null != studentArmyInfo.getStudent().getCollege()&& null != studentArmyInfo.getStudent().getCollege().getId()&& studentArmyInfo.getStudent().getCollege().getId().length() > 0) {
			majorList = compService.queryMajorByCollage(studentArmyInfo.getStudent().getCollege().getId());
			log.debug("若已经选择学院，则查询学院下的专业信息.");
		}
		// 下拉列表 班级
		List<BaseClassModel> classList = null;
		if (null != studentArmyInfo && null != studentArmyInfo.getStudent() && null != studentArmyInfo.getStudent().getClassId() && null != studentArmyInfo.getStudent().getMajor() && null != studentArmyInfo.getStudent().getMajor().getId() && studentArmyInfo.getStudent().getMajor().getId().length() > 0) {
			classList = compService.queryClassByMajor(studentArmyInfo.getStudent().getMajor().getId());
			log.debug("若已经选择专业，则查询专业下的班级信息.");
		}
		model.addAttribute("genderDicList", genderDicList);
		model.addAttribute("page", page);
		model.addAttribute("collegeList", collegeList);
		model.addAttribute("majorList", majorList);
		model.addAttribute("classList", classList);
		// request传递查询条件
		model.addAttribute("studentArmyInfo", studentArmyInfo);

		return IntegrateConstant.INTEGRATE_STUDENT_FTL + "/studentArmyList";
	}
	
	/**
	 * 学生参军信息查询
	 * 
	 * @param model
	 * @param request
	 * @param response
	 * @return StudentArmyInfoModel
	 */
	@RequestMapping(IntegrateConstant.INTEGRATE_STUDENT_ARMY + "/opt-query/queryStudentArmy")
	public String queryStudentArmy(ModelMap model, HttpServletRequest request,HttpServletResponse response) {
		
		log.info("Controller:StudentManageController;方法:学生审核信息查询queryStudentArmy()");

		String id = (String) request.getParameter("id");
		if (StringUtils.hasText(id)) {
			StudentArmyInfoModel studentArmyInfo = studentManageService.queryStudentArmy(id);
			model.addAttribute("studentArmyInfo", studentArmyInfo);
		}
		return IntegrateConstant.INTEGRATE_STUDENT_FTL + "/studentArmyview";
	}
	/**
	 * 学生参军信息查询
	 * 
	 * @param model
	 * @param request
	 * @param response
	 * @return StudentArmyInfoModel
	 */
	@ResponseBody
	@RequestMapping(value={IntegrateConstant.INTEGRATE_STUDENT_ARMY+"/opt-query/queryStudentArmyInfoById.do"}, produces={"text/plain;charset=UTF-8"})
	public String queryStudentArmyInfoById(ModelMap model, HttpServletRequest request,HttpServletResponse response,String certificateCode) {
		
		log.info("Controller:StudentManageController;方法:根据学生id查询参军信息queryStudentArmyInfoById()");
		if (StringUtils.hasText(certificateCode)){
			StudentArmyInfoModel studentArmyInfo = studentManageService.getStudentArmyByCertificateCode(certificateCode);
			if(studentArmyInfo==null)
				return "success";
			return studentArmyInfo.getStudent().getName() + "学生的参军信息已经存在！";
		}
		return "参军信息查询报错，请联系管理员处理！";
	}
	/**
	 * @Title: editStudentArmy
	 * @Description: 参军信息添加跳转
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	@RequestMapping(IntegrateConstant.INTEGRATE_STUDENT_ARMY + "/opt-edit/editStudentArmy")
	public String editStudentArmy(ModelMap model, HttpServletRequest request,HttpServletResponse response,StudentArmyInfoModel studentArmy){
		String id = (String) request.getParameter("id");
		if (StringUtils.hasText(id)) {
			StudentArmyInfoModel studentArmyInfo=studentManageService.queryStudentArmy(id);
			if(studentArmyInfo!=null){
	        	model.addAttribute("studentArmyInfo",studentArmyInfo);
			}
		}
        return IntegrateConstant.INTEGRATE_STUDENT_FTL+"/studentArmyEdit";
	}
	
	/**
	 * @Title: addStudentArmy
	 * @Description: 参军信息添加
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	@RequestMapping(IntegrateConstant.INTEGRATE_STUDENT_ARMY + "/opt-add/addStudentArmy")
	public String addStudentArmy(ModelMap model, HttpServletRequest request,HttpServletResponse response,StudentArmyInfoModel studentArmy){
		
		studentArmy.setTuitionFees(DataUtil.isNotNull(request.getParameter("tuitionFeesString"))?Float.parseFloat(request.getParameter("tuitionFeesString")):0);
		studentArmy.setPaidFees(DataUtil.isNotNull(request.getParameter("paidFeesString"))?Float.parseFloat(request.getParameter("tuitionFeesString")):0);
		studentArmy.setLoanAmount(DataUtil.isNotNull(request.getParameter("loanAmountString"))?Float.parseFloat(request.getParameter("tuitionFeesString")):0);
		studentArmy.setCompensationAmount(DataUtil.isNotNull(request.getParameter("compensationAmountString"))?Float.parseFloat(request.getParameter("tuitionFeesString")):0);
		studentArmy.setLoanCount(DataUtil.isNotNull(request.getParameter("loanCountString"))?Float.parseFloat(request.getParameter("tuitionFeesString")):0);
		studentArmy.setLoanPrincipal(DataUtil.isNotNull(request.getParameter("loanPrincipalString"))?Float.parseFloat(request.getParameter("tuitionFeesString")):0);
		studentArmy.setSupportFees(DataUtil.isNotNull(request.getParameter("supportFeesString"))?Float.parseFloat(request.getParameter("tuitionFeesString")):0);
		if (studentArmy!=null && StringUtils.hasText(studentArmy.getId())) {
			studentManageService.updateStudentAramy(studentArmy);
		}else{
			studentManageService.addStudentAramy(studentArmy);
		}
        return "redirect:"+IntegrateConstant.INTEGRATE_STUDENT_ARMY + "/opt-query/listStudentArmy.do";
	}
	/**
	 * @Title: deleteStudentArmy
	 * @Description: 参军信息删除   物理删除
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	@ResponseBody
	@RequestMapping(IntegrateConstant.INTEGRATE_STUDENT_ARMY + "/opt-del/deleteStudentArmy")
	public String deleteStudentArmy(ModelMap model, HttpServletRequest request,HttpServletResponse response) 
	{
		String[] ids =  request.getParameterValues("armyStudentId");
		if (!ArrayUtils.isEmpty(ids)) 
			studentManageService.deleteStudentAramy(ids);
		return "success";
	}
	/**
	 * @Title: importStudentArmy
	 * @Description: 导入参军学生信息
	 * @param model
	 * @param request
	 * @param studentArmyInfoModel
	 * @return
	 * @throws
	 */
	@SuppressWarnings({"deprecation", "finally" })
	@RequestMapping(IntegrateConstant.INTEGRATE_STUDENT_ARMY+"/opt-modify/importStudentArmy")
	public String importStudentArmy(ModelMap model, @RequestParam("file")  MultipartFile file, String maxSize,String allowedExt,HttpServletRequest request, HttpSession session){
		
		log.info("Controller:StudentManageController;方法:导入参军学生列表importStudentArmy()");
		List<Object> errorText = new ArrayList<Object>();
		String errorTemp = "";
		try {
		//构建文件验证对象
    	MultipartFileValidator validator = new MultipartFileValidator();
    	if(DataUtil.isNotNull(allowedExt)){
    		validator.setAllowedExtStr(allowedExt.toLowerCase());
    	}
    	//设置文件大小
    	if(DataUtil.isNotNull(maxSize)){
    		validator.setMaxSize(Long.valueOf(maxSize));//20M
    	}else{
    		validator.setMaxSize(1024*1024*20);//20M
    	}
		//调用验证框架自动验证数据
        String returnValue=validator.validate(file);                
        if(!returnValue.equals("")){
			errorTemp = returnValue;       	
			errorText.add(errorTemp);
        	model.addAttribute("errorText",errorText.size()==0);
			model.addAttribute("importFlag", Boolean.valueOf(true));
        	return IntegrateConstant.INTEGRATE_STUDENT_FTL+"/studentArmyImport";
        }
        String tempFileId=fileUtil.saveSingleFile(true, file); 
        File tempFile=fileUtil.getTempRealFile(tempFileId);
		String filePath = tempFile.getAbsolutePath();
        session.setAttribute("filePath", filePath);
        	ImportUtil iu = new ImportUtil();
			// 将Excel数据映射成对象List
			@SuppressWarnings("unchecked")
            List<StudentArmyInfoModel> list = iu.getDataList(tempFile.getAbsolutePath(), "importStudentArmy", null, StudentArmyInfoModel.class);
			studentManageService.importStudentArmy(list, filePath);
		} catch (OfficeXmlFileException e) {
			log.error(e.getMessage());
			errorTemp = "OfficeXmlFileException" + e.getMessage();
			errorText.add(errorTemp);
		} catch (ExcelException e) { 
			log.error(e.getMessage());
			errorTemp = e.getMessage();
			errorText.add(errorTemp);
		} catch (InstantiationException e) {
			log.error(e.getMessage());
			errorTemp = "InstantiationException" + e.getMessage();
			errorText.add(errorTemp);
		} catch (IOException e) {
			log.error(e.getMessage());
			errorTemp = "IOException" + e.getMessage();
			errorText.add(errorTemp);
		} catch (IllegalAccessException e) {
			log.error(e.getMessage());
			errorTemp = "IllegalAccessException" + e.getMessage();
			errorText.add(errorTemp);
		} catch (ClassNotFoundException e) {
			log.error(e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage());
			errorText.add("模板不正确或者模板内数据异常，请检查后再导入。");
		} finally {
			model.addAttribute("importFlag", Boolean.valueOf(true));
			model.addAttribute("errorText", errorText.size()==0? null : errorText);
	        return IntegrateConstant.INTEGRATE_STUDENT_FTL+"/studentArmyImport";
		}
	}
	
	/** ----------审批流开始---------*/
	/**
	 * @Title: saveApproveAction
	 * @Description: 保存当前审批操作
	 * @param model
	 * @param request
	 * @param objectId
	 * @param nextApproverId
	 * @return
	 * @throws
	 */
	@RequestMapping(value = {IntegrateConstant.INTEGRATE_STUDENT_MANAGE+"/opt-add/saveApproveAction" },produces = { "text/plain;charset=UTF-8" })
	@ResponseBody
	public String saveApproveAction(ModelMap model,HttpServletRequest request,String objectId,String nextApproverId,String approveStatus,String processStatusCode){
		
		ApproveResult result = new ApproveResult();
		if(ProjectConstants.IS_APPROVE_ENABLE){
			try {
				  result.setApproveStatus(approveStatus);
				  result.setProcessStatusCode(processStatusCode);
				  this.saveUpdateStudentApproveResult(objectId,result,nextApproverId);
				  result.setResultFlag("success");
				  
			} catch (Exception e) {
				result.setResultFlag("error");
				e.printStackTrace();
			}
		}else{
			result.setResultFlag("deprecated");
	    }
		JSONObject json=JsonUtils.getJsonObject(result);
		return JsonUtils.jsonObject2Json(json);
	}
    /**
	 * @Title: saveUpdateStudentApproveResult
	 * @Description: 保存更新学生信息审批结果
	 * @param objectId
	 * @param result
	 * @throws
	 */
	private ApproveResult saveUpdateStudentApproveResult(String objectId,ApproveResult result,String nextApproverId) {
		if(DataUtil.isNotNull(result)){
			//获取保存的学生信息
			StudentUpdateInfoModel studentUpdateInfo = this.studentManageService.queryStudentUpdateById(objectId);
			if(DataUtil.isNotNull(result.getProcessStatusCode()) && result.getProcessStatusCode().equals("REJECT")) {
				studentUpdateInfo.setStatus("SAVE");
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
				this.studentManageService.updateStudent(studentUpdateInfo);
		}
		return result;
	}
	/**
	 * 判断是否审核
	 * @Title: isApprove
	 * @Description: 判断是否已审核
	 * @param objectId
	 * @param userId
	 * @return
	 * @throws
	 */
	public boolean isApprove(String objectId, String userId){
		boolean bol = false;
		if(StringUtils.hasText(userId))
		{
			FlowInstancePo fipo = this.flowInstanceService.getFlowInstancePo(objectId,sessionUtil.getCurrentUserId());
			if(null!=fipo && !"".equals(fipo.getId()))
				if(fipo.getApproveToken().equals("AVAILABLE")){
					return bol = true;
				}else{
					return bol = false;
				}
		}
		return bol;
	}
	/**
	 * 
	 * @Title: saveApproveReason
	 * @Description: 保存审核的信息（保存在自己表中 审核理由）
	 * @param model
	 * @param request
	 * @return
	 * @throws
	 */
	@RequestMapping({IntegrateConstant.INTEGRATE_STUDENT_MANAGE+"/opt-save/saveApproveReason"})
	public String saveApproveReason(ModelMap model,HttpServletRequest request){
		String id = request.getParameter("id");
		String approveReason = request.getParameter("approveReason");
		
		StudentUpdateInfoModel studentUpdateInfo =this.studentManageService.queryStudentUpdateById(id);
		
		studentUpdateInfo.setApproveReason(approveReason);
		if( "PASS".equals(studentUpdateInfo.getProcessStatus())||"REJECT".equals(studentUpdateInfo.getProcessStatus())){
			studentUpdateInfo.setNextApprover(null);
		}
		this.studentManageService.updateStudent(studentUpdateInfo);
		return "redirect:"+IntegrateConstant.INTEGRATE_STUDENT_MANAGE+"/opt-query/listApproveStudent.do";						
	}
	/** ----------审批流结束---------*/
	
	/**
	 * 导出学生信息列表
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping({IntegrateConstant.INTEGRATE_STUDENT_INFO+"/nsm/exportStudentInfoView"})
	  public String exportStudentInfoList(ModelMap model, HttpServletRequest request){
	    int exportSize = Integer.valueOf(request.getParameter("exportSize")).intValue();
	    int pageTotalCount = Integer.valueOf(request.getParameter("pageTotalCount")).intValue();
	    int maxNumber = 0;
	    if (pageTotalCount < exportSize)
	      maxNumber = 1;
	    else if (pageTotalCount % exportSize == 0)
	      maxNumber = pageTotalCount / exportSize;
	    else {
	      maxNumber = pageTotalCount / exportSize + 1;
	    }
	    model.addAttribute("exportSize", Integer.valueOf(exportSize));
	    model.addAttribute("maxNumber", Integer.valueOf(maxNumber));
	    if (maxNumber < 500)
	      model.addAttribute("isMore", "false");
	    else {
	      model.addAttribute("isMore", "true");
	    }
	    return IntegrateConstant.INTEGRATE_STUDENT_FTL+"/exportStudentInfoView";
	  }
	/**
	 * 导出数据
	 * @param model
	 * @param request
	 * @param workApplyPo
	 * @param response
	 */
	  @SuppressWarnings({ "rawtypes", "deprecation" })
      @RequestMapping({IntegrateConstant.INTEGRATE_STUDENT_INFO+"/opt-export/exportStudentInfo"})
	  public void exportStudentInfo(ModelMap model, HttpServletRequest request, StudentInfoModel studentInfo, HttpServletResponse response)
	  {
	    String exportSize = request.getParameter("studentInfoQuery_exportSize");
	    String exportPage = request.getParameter("studentInfoQuery_exportPage");

	    String teacherOrgId= ProjectSessionUtils.getCurrentTeacherOrgId(request);
        boolean isStudent = ProjectSessionUtils.checkIsStudent(request);
        String userId = sessionUtil.getCurrentUserId();
        UserRole useRole = commonRoleService.getRoleByCode(userId,"HKY_COLLEGE_DIRECTOR");
        List<BaseClassModel> classesList = this.studentManageService.queryClassByTeacher(sessionUtil.getCurrentUserId());		
	    Page page = this.studentManageService.pagedQueryStudentInfo(Integer.parseInt(exportPage), Integer.parseInt(exportSize), studentInfo,sessionUtil.getCurrentUserId() ,teacherOrgId,isStudent,classesList,useRole);

	    List<Map> listMap = new ArrayList<Map>();
	    List<StudentInfoModel> studentInfoList= (List)page.getResult();
	    for(int i=0;i<studentInfoList.size();i++){
	      StudentInfoModel s=studentInfoList.get(i);
	      Map<String,Object> newmap = new HashMap<String,Object>();
	      /*
	       * 院系，专业，班级，学号，姓名，性别，学籍状态，名族
	       * 
	       */
	      newmap.put("sortId", i+1);
	      newmap.put("collegeName",s.getCollege()!=null?s.getCollege().getName():"");
	      newmap.put("majorName", s.getMajor()!=null?s.getMajor().getMajorName():"");
	      newmap.put("className", s.getClassId()!=null?s.getClassId().getClassName():"");
	      newmap.put("stuNumber", s.getStuNumber());
	      newmap.put("name", s.getName());
	      newmap.put("certificateCode", s.getCertificateCode());
	      newmap.put("genderName", s.getGenderDic()!=null?s.getGenderDic().getName():"");
	      newmap.put("national", s.getNational());
	      newmap.put("edusStatus",s.getEdusStatus()!=null?ProjectConstants.getStatusMap().get(s.getEdusStatus()):"");
	      listMap.add(newmap);
	    }
	    try
	    {
	      HSSFWorkbook wb = this.excelService.exportData("export_studentInfo.xls", "exportStudentInfo",listMap);
	      //添加表头列表 
		  HSSFSheet sheet = wb.getSheetAt(0);
		  HSSFRow headRow = null;
		  headRow = sheet.createRow(0);	
	      //单元格样式
	      HSSFCellStyle styleBold = (HSSFCellStyle) wb.createCellStyle();
		  styleBold.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框
		  styleBold.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
		  styleBold.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
		  styleBold.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框
		  styleBold.setAlignment(HSSFCellStyle.ALIGN_CENTER);//水平居中
		  HSSFFont f = wb.createFont(); f.setFontHeightInPoints((short) 24);
		  //字号
		  f.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
		  //加粗 
		  styleBold.setFont(f); 
		  /* if(studentInfo.getCollege()!=null && StringUtils.hasText(studentInfo.getCollege().getName())){
				title=studentInfo.getCollege().getName()+"学院"+"学生信息情况统计表";
		  }*/
		  //表格表头
		  String title="学生学籍信息统计表";
		  HSSFCell headCell = headRow.createCell(0);
			headCell.setCellValue(title);// 跨单元格显示的数据
			headCell.setCellType(HSSFCell.CELL_TYPE_STRING);
			sheet.addMergedRegion(new CellRangeAddress(0,0,0,7));
			//设置合并单元格的高度
			sheet.getRow(0).setHeightInPoints(35);
			headCell.setCellStyle(styleBold);
	      String filename = "学生信息"+exportPage+"页.xls";
	      response.setContentType("application/x-excel");
	      response.setHeader("Content-disposition", "attachment;filename=" + new String(filename.getBytes("GBK"), "iso-8859-1"));
	      response.setCharacterEncoding("UTF-8");
	      OutputStream ouputStream = response.getOutputStream();
	      wb.write(ouputStream);
	      ouputStream.flush();
	      ouputStream.close();
	    }
	    catch (ExcelException e)
	    {
	      e.printStackTrace();
	    }
	    catch (InstantiationException e) {
	      e.printStackTrace();
	    }
	    catch (IOException e) {
	      e.printStackTrace();
	    }
	    catch (IllegalAccessException e) {
	      e.printStackTrace();
	    }
	    catch (SQLException e) {
	      e.printStackTrace();
	    }
	    catch (ClassNotFoundException e) {
	      e.printStackTrace();
	    }
	    catch (URISyntaxException e) {
	      e.printStackTrace();
	    }
	  }
	  
	  /**
	   * 
	   * @Title: printStudent
	   * @Description: 打印在校证明
	   * @param request
	   * @param id
	   * @return
	   * @throws
	   */
	  @RequestMapping("/student/print/opt-print/printStudent")
	  public String printStudent(ModelMap model,HttpServletRequest request,String id)
	  {
		  if (StringUtils.hasText(id)) 
		  {
				StudentInfoModel studentInfo = studentCommonService.queryStudentById(id);
				model.addAttribute("student", studentInfo);
				model.addAttribute("currentDate", DateUtil.getCurYear()+" 年  "+DateUtil.getCurMonth()+" 月  "+DateUtil.getCurDay()+" 日  ");
		  }
		  return IntegrateConstant.INTEGRATE_STUDENT_FTL+"/printStudent";
	  }
	  
	  /**
	   * 
	   * @Title: batchApproveStudent
	   * @Description: 批量审核结果处理
	   * @param model
	   * @param request
	   * @param resultsArray
	   * @return
	   * @throws
	   */
	  @ResponseBody
	  @RequestMapping("/student/manage/opt-approve/batchApproveStudent")
	  public String batchApproveStudent(ModelMap model,HttpServletRequest request,String resultsArray)
	  {
		  log.debug("批量审核返回结果值：" + resultsArray );
		  List<ApproveResult> list  = this.flowInstanceService.getFormatedResult(resultsArray,ProjectConstants.IS_APPROVE_ENABLE);
		  if(DataUtil.isNotNull(list) && list.size()>0){
			  String userId = "";
			  for(ApproveResult result : list)
			  {
				  userId = null == result.getNextApproverList() || result.getNextApproverList().size() ==0 ? "" : result.getNextApproverList().get(0).getUserId();
				  saveUpdateStudentApproveResult(result.getObjectId(), result,userId);
			  }
		  }
		  return "success";
	  }
	 
	  /**
	   * 
	   * @Title: studentStatistics
	   * @Description: 学生信息统计
	   * @param model
	   * @param request
	   * @return
	   * @throws
	   */
	  @RequestMapping("/student/statistics/opt-query/studentStatistics")
	  public String studentStatistics(ModelMap model,HttpServletRequest request)
	  {
		  List<StudentSchoolCount> schoolCountList = studentManageService.getStudentSchoolCount();
		  List<StudentCollegeCount> collegeCountList = studentManageService.getStudentCollegeCount("");
		  
		  model.addAttribute("schoolCountList", schoolCountList);
		  model.addAttribute("collegeCountList", collegeCountList);
		  model.addAttribute("gradeList", baseDataService.listGradeList());
		  model.addAttribute("collegeList", baseDataService.listBaseAcademy());
		  
		  return "/integrate/statistics/studentStatistics";
	  }
	  
	  
	  
	  
	  
	  
	  
	  
	  /**
	   * 
	   * @Title: studentCollegeCountByGrade
	   * @Description: TODO(这里用一句话描述这个方法的作用)
	   * @param model
	   * @param request
	   * @param grade
	   * @return
	   * @throws
	   */
	  @RequestMapping("student/manage/nsm/studentCollegeCountByGrade")
	  public String studentCollegeCountByGrade(ModelMap model,HttpServletRequest request,String grade)
	  {
		  List<StudentCollegeCount> collegeCountList = studentManageService.getStudentCollegeCount(grade);
		  model.addAttribute("collegeCountList", collegeCountList);
		  return  "/integrate/statistics/studentCollegeCountBody";
	  }
	  
	  /**
		 * 
		 * @Title: listStudent 2016-5-20
		 * @Description: 学生政治面貌列表
		 * @param model
		 * @param request
		 * @param response
		 * @return
		 * @throws
		 */

		@RequestMapping("/student/political/opt-query/studentPoliticalList")
		public String studentPolitical(ModelMap model, HttpServletRequest request, HttpServletResponse response, StudentInfoModel studentInfo) {
			log.info("学生政治面貌列表查询");
	        String teacherOrgId= ProjectSessionUtils.getCurrentTeacherOrgId(request);
	        boolean isStudent = ProjectSessionUtils.checkIsStudent(request);
	        String userId = sessionUtil.getCurrentUserId();
	        UserRole useRole = commonRoleService.getRoleByCode(userId,"HKY_COLLEGE_DIRECTOR");
			List<BaseClassModel> classesList = this.studentManageService.queryClassByTeacher(sessionUtil.getCurrentUserId());		
			if(null !=useRole && null !=useRole.getRole() && StringUtils.hasText(useRole.getRole().getId()) && "HKY_COLLEGE_DIRECTOR".equals(useRole.getRole().getCode()))
			{
				BaseAcademyModel college = this.baseDataService.findAcademyById(teacherOrgId);
		        studentInfo.setCollege(college);
		        model.addAttribute("orgStatus", "ture");
		        classesList = null;
			}
			
			if(null != classesList && classesList.size()>0)
			{   
				model.addAttribute("classesList", classesList);
			}
			
			if(CheckUtils.isCurrentOrgEqCollege(teacherOrgId) && (!(ProjectConstants.STUDNET_OFFICE_ORG_ID).equals(teacherOrgId) || !(ProjectConstants.JOBOFFICEID).equals(teacherOrgId)))
	        {
		        BaseAcademyModel college = this.baseDataService.findAcademyById(teacherOrgId);
		        studentInfo.setCollege(college);
		        model.addAttribute("orgStatus", "ture");
	        }
		    
			int pageNo = request.getParameter("pageNo") != null ? Integer .valueOf(request.getParameter("pageNo")) : 1;
			Page page = studentManageService.pagedQueryStudentInfo(pageNo, Page.DEFAULT_PAGE_SIZE, studentInfo, sessionUtil.getCurrentUserId() ,teacherOrgId,isStudent,classesList,useRole);
			// 下拉列表 学院
			List<BaseAcademyModel> collegeList = baseDataService.listBaseAcademy();
			// 下拉列表 专业
			List<BaseMajorModel> majorList =null;
			if (null != studentInfo && null != studentInfo.getCollege()&& null != studentInfo.getCollege().getId()&& studentInfo.getCollege().getId().length() > 0) {
				majorList = compService.queryMajorByCollage(studentInfo.getCollege().getId());
				log.debug("若已经选择学院，则查询学院下的专业信息.");
			}
			// 下拉列表 班级
			List<BaseClassModel> classList =null;
			if (null != studentInfo && null != studentInfo.getClassId() && null != studentInfo.getMajor() && null != studentInfo.getMajor().getId() && studentInfo.getMajor().getId().length() > 0) {
				classList = compService.queryClassByMajor(studentInfo.getMajor().getId());
				log.debug("若已经选择专业，则查询专业下的班级信息.");
			}
			
			model.addAttribute("collegeList", collegeList);
			model.addAttribute("genderDicList", dicUtil.getDicInfoList("GENDER"));
			model.addAttribute("politicalDicList", dicUtil.getDicInfoList("SCH_POLITICAL_STATUS"));
			model.addAttribute("page", page);
			model.addAttribute("majorList", majorList);
			model.addAttribute("classList", classList);
			model.addAttribute("gradeList", baseDataService.listGradeList());
			model.addAttribute("isStudent", isStudent);
			model.addAttribute("studentInfo", studentInfo);
			
			return IntegrateConstant.INTEGRATE_STUDENT_FTL + "/politicalList";
		}
		
		/**
		 * 更新政治面貌
		 * @param baseClass
		 * @param model
		 * @param fileId
		 * @returnleave
		 */
		@ResponseBody
		@RequestMapping(value={"/student/political/opt-update/saveStudentPolitical"} , produces={"text/plain;charset=UTF-8"})
		public String saveStudentPolitical(String id, String political){
			this.studentManageService.updateStudentPolitical(id, political);
			log.info("更新政治面貌");
			return "success";
		}
		
	  
}
