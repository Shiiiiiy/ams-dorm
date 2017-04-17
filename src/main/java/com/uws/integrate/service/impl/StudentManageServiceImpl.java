package com.uws.integrate.service.impl;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uws.apw.model.ApproveResult;
import com.uws.apw.service.IFlowInstanceService;
import com.uws.common.dao.ICommonRoleDao;
import com.uws.common.service.IStudentCommonService;
import com.uws.core.base.BaseServiceImpl;
import com.uws.core.excel.ExcelException;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.core.util.IdUtil;
import com.uws.core.util.SpringBeanLocator;
import com.uws.core.util.StringUtils;
import com.uws.domain.base.BaseClassModel;
import com.uws.domain.integrate.ClassMonitorSetModel;
import com.uws.domain.integrate.StudentApproveSetModel;
import com.uws.domain.integrate.StudentArmyInfoModel;
import com.uws.domain.integrate.StudentCollegeCount;
import com.uws.domain.integrate.StudentGuardianUpdateModel;
import com.uws.domain.integrate.StudentSchoolCount;
import com.uws.domain.integrate.StudentUpdateInfoModel;
import com.uws.domain.orientation.StudentGuardianModel;
import com.uws.domain.orientation.StudentInfoModel;
import com.uws.integrate.dao.IStudentManageDao;
import com.uws.integrate.service.IStudentManageService;
import com.uws.user.model.User;
import com.uws.user.model.UserRole;
import com.uws.util.ProjectConstants;

/**
 * 
* @ClassName: StudentManagerServiceImpl 
* @Description: 综合信息服务 学生管理 Service  实现
* @author 联合永道
* @date 2015-7-27 上午9:44:36 
*
 */
@Service("com.uws.integrate.service.impl.StudentManageServiceImpl")
public class StudentManageServiceImpl extends BaseServiceImpl implements IStudentManageService{
   
	@Autowired
	private IFlowInstanceService flowInstanceService;
	@Autowired
	private IStudentManageDao studentManageDao;
	@Autowired
	private IStudentCommonService studentCommonService;
	@Autowired
	private ICommonRoleDao commonRoleDao;

    /**
	 * 
	 * @Title: pagedQueryStudentInfo
	 * @Description: 查询学生列表
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	@Override
	public Page pagedQueryStudentInfo(int pageNo, int pageSize, StudentInfoModel studentInfo, String userId,String teacherOrgId,boolean isStudent,List classesList,UserRole useRole) {

		return studentManageDao.pagedQueryStudentInfo(pageNo, pageSize, studentInfo, userId, teacherOrgId,isStudent,classesList,useRole);
	}
	
	/**
	 * 
	 * @Title: queryStudentInfo
	 * @Description: 学生id查询学生信息
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	@Override
	public StudentInfoModel queryStudentInfo(String id) {

		return (StudentInfoModel) studentManageDao.get(StudentInfoModel.class, id);
	}
	
	/**
	 * 
	 * @Title: pagedQueryBaseClass
	 * @Description:通过学院，专业，班级的下列表查询班级信息列表+审核班长
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	@Override
	public Page pagedQueryBaseClass(Integer pageSize, Integer pageNum, ClassMonitorSetModel classMonitor,String teacher) {

		return studentManageDao.pagedQueryBaseClass(pageSize, pageNum, classMonitor,teacher);
	}
	
	/**
	 * 
	 * @Title: editMonitor
	 * @Description: 修改审核班长
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	@Override
	public void editMonitor(StudentApproveSetModel studentApproveSet,String orginStudentId) {
		 //StudentApproveSetModel studentApprove=this.queryApproveByClassId(studentApproveSet.getClassId().getId());
		 commonRoleDao.updateUserRole(orginStudentId, studentApproveSet.getStudentId().getId(), ProjectConstants.AUDIT_CLASS_MONITOR_ROLE_NAME);
		 studentManageDao.update(studentApproveSet);
	}
	
	/**
	 * 
	 * @Title: addMonitor
	 * @Description: 添加审核班长
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	@Override
	public void addMonitor(StudentApproveSetModel studentApproveSet) {
		 commonRoleDao.saveUserRole(studentApproveSet.getStudentId().getId(), ProjectConstants.AUDIT_CLASS_MONITOR_ROLE_NAME);
		 studentManageDao.save(studentApproveSet);
	}
	
	/**
	 * 
	 * @Title: queryApproveByClassId
	 * @Description: 通过班级id查询审核设置信息（审核班长，班级Id，学生Id）
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	@Override
	public StudentApproveSetModel queryApproveByClassId(String classId) {

		return studentManageDao.queryApproveByClassId(classId);
	}
	/**
	 * 
	 * @Title: saveStudentUpdateModel
	 * @Description: 创建学生修改的信息
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	@Override
	public void saveStudentUpdateModel(StudentUpdateInfoModel studentUpdateModel, List<StudentGuardianUpdateModel> list) {
		
		if(!StringUtils.hasText(studentUpdateModel.getId()))
			studentUpdateModel.setId(IdUtil.getUUIDHEXStr());
		studentManageDao.save(studentUpdateModel);
		//删除已保存的监护人信息      重新添加新的监护人信息
		if(list!=null && list.size()>0){
			studentManageDao.deleteStudentGuardianUpdate(studentUpdateModel.getId(),"delete");
			for(int i=0;i<list.size();i++){
				list.get(i).setStudentUpdateInfo(studentUpdateModel);
				studentManageDao.save(list.get(i));
			}
		}
	}
	/**
	 * 
	 * @Title: updateStudentInfoModel
	 * @Description: 老师修改学生的信息保存方法
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	@Override
	public void updateStudentInfoModel(StudentInfoModel studentInfoModel, List<StudentGuardianModel> list){
		
		//保存学生信息 修改监护人信息（3条）
		studentManageDao.save(studentInfoModel);
		StudentGuardianModel studentGuardian1 =studentCommonService.queryStudentGuardianByStudentNo(studentInfoModel.getStuNumber(), "1");
        StudentGuardianModel studentGuardian2 =studentCommonService.queryStudentGuardianByStudentNo(studentInfoModel.getStuNumber(), "2");
        StudentGuardianModel studentGuardian3 =studentCommonService.queryStudentGuardianByStudentNo(studentInfoModel.getStuNumber(), "3");
       
        if(null!=studentGuardian1)
        {
        	BeanUtils.copyProperties(list.get(0),studentGuardian1,new String[]{"studentInfo","createTime","id","seqNum","updater","creator","status"});	
        	studentManageDao.update(studentGuardian1);
        }else
        {
        	studentGuardian1 = list.get(0);
        	studentGuardian1.setStatus("1");
        	studentGuardian1.setStudentInfo(studentInfoModel);
        	studentManageDao.save(studentGuardian1);
        }
        if(null!=studentGuardian2)
        {
        	BeanUtils.copyProperties(list.get(1),studentGuardian2,new String[]{"studentInfo","createTime","id","seqNum","updater","creator","status"});	
        	studentManageDao.update(studentGuardian2);
        }else
        {
        	studentGuardian2 = list.get(1);
        	studentGuardian2.setStatus("1");
        	studentGuardian2.setStudentInfo(studentInfoModel);
        	studentManageDao.save(studentGuardian2);
        }
        if(null!=studentGuardian3)
        {
        	BeanUtils.copyProperties(list.get(2),studentGuardian3,new String[]{"studentInfo","createTime","id","seqNum","updater","creator","status"});	
        	studentManageDao.update(studentGuardian3);
        }else
        {
        	studentGuardian3 = list.get(2);
        	studentGuardian3.setStatus("1");
        	studentGuardian3.setStudentInfo(studentInfoModel);
        	studentManageDao.save(studentGuardian3);
        }
	}
	/**
	 * 
	 * @Title: queryStudentUpdateByNum
	 * @Description: 通过学号查询学生在更新的表中的数据
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	@Override
	public StudentUpdateInfoModel queryStudentUpdateByNum(String stuNumber) {
 
		return (StudentUpdateInfoModel) studentManageDao.queryStudentUpdateByNum(stuNumber);
	}
	/**
	 * 
	 * @Title: queryStudentUpdateById
	 * @Description: 通过学号查询学生在更新的表中的数据
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	@Override
	public StudentUpdateInfoModel queryStudentUpdateById(String id) {
 
		return (StudentUpdateInfoModel) studentManageDao.get(StudentUpdateInfoModel.class, id);
	}
	/**
	 * 
	 * @Title: updateStudentUpdateModel
	 * @Description:修改学生的更新信息表
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	@Override
	public void updateStudentUpdateModel(StudentUpdateInfoModel studentUpdateModel,List<StudentGuardianUpdateModel> list) {
		studentManageDao.update(studentUpdateModel);
		//删除已保存的监护人信息      重新添加新的监护人信息
		if(list!=null && list.size()>0){
			studentManageDao.deleteStudentGuardianUpdate(studentUpdateModel.getId(),"delete");
			for(int i=0;i<list.size();i++){
				list.get(i).setStudentUpdateInfo(studentUpdateModel);
				studentManageDao.save(list.get(i));
			}
		}
	}
	/**
	 * 
	 * @Title: updateStudentUpdate
	 * @Description:修改学生的更新信息表
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	@Override
	public void updateStudent(StudentUpdateInfoModel studentUpdateInfo) {
		//最后一级审核通过后	将更信息保存到学生信息表中	监护人信息保存到监护人表中（3条数据）
		if(studentUpdateInfo!=null){
			StudentGuardianUpdateModel StudentGuardianUpdateModel1 =studentManageDao.queryStudentGuardianUpdate(studentUpdateInfo.getId(), "1", "DELETE");
			if(StudentGuardianUpdateModel1!=null && studentUpdateInfo.getStuId()!=null && studentUpdateInfo.getProcessStatus().equals("PASS")){
		        studentUpdateInfo.setStatus("DELETE");
		        StudentGuardianUpdateModel StudentGuardianUpdateModel2 =studentManageDao.queryStudentGuardianUpdate(studentUpdateInfo.getId(), "2", "DELETE");
		        StudentGuardianUpdateModel StudentGuardianUpdateModel3 =studentManageDao.queryStudentGuardianUpdate(studentUpdateInfo.getId(), "3", "DELETE");
		        StudentGuardianModel studentGuardian1 =studentCommonService.queryStudentGuardianByStudentNo(studentUpdateInfo.getStuId().getStuNumber(), "1");
		        StudentGuardianModel studentGuardian2 =studentCommonService.queryStudentGuardianByStudentNo(studentUpdateInfo.getStuId().getStuNumber(), "2");
		        StudentGuardianModel studentGuardian3 =studentCommonService.queryStudentGuardianByStudentNo(studentUpdateInfo.getStuId().getStuNumber(), "3");
		        if(null!=studentGuardian1)
		        {
		        	BeanUtils.copyProperties(StudentGuardianUpdateModel1,studentGuardian1,new String[]{"studentInfo","createTime","id","seqNum","updater","creator","updateTime","status"});	
		        	studentManageDao.update(studentGuardian1);
		        }else
		        {
		        	studentGuardian1 = new StudentGuardianModel();
		        	BeanUtils.copyProperties(StudentGuardianUpdateModel1,studentGuardian1);
		        	studentGuardian1.setStatus("1");
		        	studentGuardian1.setStudentInfo(studentUpdateInfo.getStuId());
		        	studentManageDao.save(studentGuardian1);
		        }
		        if(null!=studentGuardian2)
		        {
		        	BeanUtils.copyProperties(StudentGuardianUpdateModel2,studentGuardian2,new String[]{"studentInfo","createTime","id","seqNum","updater","creator","updateTime","status"});	
		        	studentManageDao.update(studentGuardian2);
		        }else
		        {
		        	studentGuardian2 = new StudentGuardianModel();
		        	BeanUtils.copyProperties(StudentGuardianUpdateModel2,studentGuardian2);	
		        	studentGuardian2.setStatus("1");
		        	studentGuardian2.setStudentInfo(studentUpdateInfo.getStuId());
		        	studentManageDao.save(studentGuardian2);
		        }
		        if(null!=studentGuardian3)
		        {
		        	BeanUtils.copyProperties(StudentGuardianUpdateModel3,studentGuardian3,new String[]{"studentInfo","createTime","id","seqNum","updater","creator","updateTime","status"});	
		        	 studentManageDao.update(studentGuardian3);
		        } else
		        {
		        	studentGuardian3 = new StudentGuardianModel();
		        	BeanUtils.copyProperties(StudentGuardianUpdateModel3,studentGuardian3);	
		        	studentGuardian3.setStudentInfo(studentUpdateInfo.getStuId());
		        	studentGuardian3.setStatus("1");
		        	studentManageDao.save(studentGuardian3);
		        }
		        
		        studentManageDao.updateStudentGuardianUpdateStatus(studentUpdateInfo.getId(),"DELETE");
				StudentInfoModel studentInfo= this.studentCommonService.queryStudentByStudentNo(studentUpdateInfo.getStuId().getStuNumber());
		        BeanUtils.copyProperties(studentUpdateInfo,studentInfo,new String[]{"createTime","id","stuNumber","creator","certificateTypeDic","certificateCode","status"});	
		        studentManageDao.update(studentInfo);
			}
		}
		studentManageDao.update(studentUpdateInfo);
	}
	
//	/**
//	 * 
//	 * @Title: covertInfo
//	 * @Description: 
//	 * @param studentGuardianModel
//	 * @param studentGuardianUpdateModel
//	 * @return
//	 * @throws
//	 */
//	private StudentGuardianModel covertInfo(StudentGuardianModel studentGuardianModel ,StudentGuardianUpdateModel studentGuardianUpdateModel)
//	{
//		return studentGuardianModel;
//	}
	
	/**
	 * 
	 * @Title: pagedQueryUpdateStudentInfo
	 * @Description: 查询当前登录人下需要审核的学生列表
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	@Override
	public Page pagedQueryUpdateStudentInfo(int pageNo, int pageSize, StudentUpdateInfoModel studentUpdateInfo, String userId,String[] objectIds,boolean isTeacher){
		
		return studentManageDao.pagedQueryUpdateStudentInfo(pageNo, pageSize, studentUpdateInfo, userId,objectIds,isTeacher);
	}
	/**
	 * 
	 * @Title: pagedQueryStudentArmy
	 * @Description: 查询参军学生列表
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	@Override
	public Page pagedQueryStudentArmy(int pageNo, int pageSize, StudentArmyInfoModel studentArmyInfo, String userId) {
		
		return studentManageDao.pagedQueryStudentArmy(pageNo, pageSize, studentArmyInfo, userId);
	}

	@Override
	public String importStudentArmy(List<StudentArmyInfoModel> list, String filePath) throws OfficeXmlFileException, IOException, IllegalAccessException, ExcelException, InstantiationException, ClassNotFoundException, Exception {
		// 错误信息
		String message = "";
		for(StudentArmyInfoModel studentArmy : list) {
			// 把导入的数据保存到数据库中
			String certificateCode = "";
			certificateCode = studentArmy.getCertificateCode();
			if (StringUtils.hasText(certificateCode)){
				IStudentCommonService studentCommonService = (IStudentCommonService)SpringBeanLocator.getBean("com.uws.common.service.impl.StudentCommonServiceImpl");
				//通过身份证号查询学生信息是否存在
				StudentInfoModel studentInfo=studentCommonService.queryStudentByCode(certificateCode);
				if(studentInfo!=null){
					//通过学号查询参军学生信息表中是否存在该学号存在则update
					StudentArmyInfoModel studentArmyInfo=(StudentArmyInfoModel) studentManageDao.getStudentArmyByCertificateCode(studentInfo.getCertificateCode());
					if(studentArmyInfo!=null){
						BeanUtils.copyProperties(studentArmy,studentArmyInfo,new String[]{"id","student"});
					    studentManageDao.update(studentArmyInfo);
					}else{
						studentArmy.setStudent(studentInfo);
						studentManageDao.save(studentArmy);
					}
				}else{
					message ="身份证号为"+certificateCode+"的学生在系统中不存在，请确认后再上传！";	
				}
			}
		}
		return message;		
	}
	/**
	 * 
	 * @Title: queryStudentArmy
	 * @Description: 根据id查询学生参军的信息
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	@Override
	public StudentArmyInfoModel queryStudentArmy(String id){
		return (StudentArmyInfoModel) studentManageDao.get(StudentArmyInfoModel.class, id);
	}
	
	/**
	 * @Title: getStudentArmyByCertificateCode
	 * @Description: 根据学生身份证号查询学生参军的信息
	 * @param studentNum
	 * @return
	 */
	@Override
	public StudentArmyInfoModel getStudentArmyByCertificateCode(String certificateCode){
		return (StudentArmyInfoModel) studentManageDao.getStudentArmyByCertificateCode(certificateCode);
	}
	/**
	 * 
	 * @Title: submitApprove
	 * @Description: 审批 修改状态 添加下一节点人
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	@Override
	public ApproveResult submitApprove(String objectId,User initiator,User nextApprover){
		
        ApproveResult result = new ApproveResult();
		//发起审核流程
		result = flowInstanceService.initProcessInstance(objectId,"STUDENT_INFO_UPDATE_APPROVE", 
				 initiator,nextApprover,ProjectConstants.IS_APPROVE_ENABLE);
		studentManageDao.updateStudentStatus(objectId,"SUBMIT");
		return result;
		
	}
	/**
	 * 
	 * @Title: updateStudentStatus
	 * @Description: 审批 修改状态 添加下一节点人
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	@Override
	public void updateStudentStatus(String objectId,String status){
		studentManageDao.updateStudentStatus(objectId,"SUBMIT");
		
	}
	/**
	 * 
	 * @Title: queryStudentUpdateByNumStatus
	 * @Description: 通过学号查询学生在更新的表中的数据(非删除状态)
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	@Override
	public StudentUpdateInfoModel queryStudentUpdateByNumStatus(String stuNumber,String status) {
 
		return (StudentUpdateInfoModel) studentManageDao.queryStudentUpdateByNumStatus(stuNumber, status);
	}
	/**
	 * 
	 * @Title: queryClassByTeacher
	 * @Description: 通过老师查询辅导员对应关系表中的班级数据
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	@Override
	public List<BaseClassModel> queryClassByTeacher(String id) {
		
		return this.studentManageDao.queryClassByTeacher(id);
		
	}

	/**
	 * 描述信息: 参军信息删除
	 * @param ids
	 * @see com.uws.integrate.service.IStudentManageService#deleteStudentAramy(java.lang.String[])
	 */
	@Override
    public void deleteStudentAramy(String[] ids)
    {
	    if(!ArrayUtils.isEmpty(ids))
	    {
	    	for(String id : ids)
	    		studentManageDao.deleteById(StudentArmyInfoModel.class, id);
	    }
    }
	
	/**
	 * 描述信息: 参军信息新增
	 * @param armyStudentId
	 */
	@Override
    public void addStudentAramy(StudentArmyInfoModel studentArmy){
	    if(studentArmy!=null){
			studentManageDao.save(studentArmy);
	    }
    }
	
	/**
	 * 描述信息: 参军信息修改
	 * @param updateStudentAramy
	 */
	@Override
    public void updateStudentAramy(StudentArmyInfoModel studentArmy){
	    if(studentArmy!=null){
			studentManageDao.update(studentArmy);
	    }
    }
	/**
	 * @Title: queryStudentGuardianUpdate
	 * @Description: 根据学生id查询监护人信息
	 */
	@Override
	public StudentGuardianUpdateModel queryStudentGuardianUpdate(String studentUpdateId,String seqNum,String status) {
		
		return studentManageDao.queryStudentGuardianUpdate(studentUpdateId,seqNum,status);
	}

	@Override
    public List<StudentSchoolCount> getStudentSchoolCount()
    {
		return studentManageDao.getStudentSchoolCount();
    }

	@Override
    public List<StudentCollegeCount> getStudentCollegeCount(String grade)
    {
	    return studentManageDao.getStudentCollegeCount(grade);
    }
	
	/**
	 * 修改政治面貌
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	@Override
	public void updateStudentPolitical(String id,String political){
		studentManageDao.updateStudentPolitical(id, political);
		
	}
}
