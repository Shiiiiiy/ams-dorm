package com.uws.integrate.dao;

import java.util.List;

import com.uws.core.hibernate.dao.IBaseDao;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.domain.base.BaseClassModel;
import com.uws.domain.integrate.ClassMonitorSetModel;
import com.uws.domain.integrate.StudentApproveSetModel;
import com.uws.domain.integrate.StudentArmyInfoModel;
import com.uws.domain.integrate.StudentCollegeCount;
import com.uws.domain.integrate.StudentGuardianUpdateModel;
import com.uws.domain.integrate.StudentSchoolCount;
import com.uws.domain.integrate.StudentUpdateInfoModel;
import com.uws.domain.orientation.StudentInfoModel;
import com.uws.user.model.UserRole;

/**
 * 
* @ClassName: IStudentManageDao 
* @Description: 综合信息服务 学生管理 DAO 接口
* @author 联合永道
* @date 2015-7-27 上午9:45:45 
*
 */
public interface IStudentManageDao extends IBaseDao{
	
	/**
	 * 
	 * @Title: pagedQueryStudentInfo
	 * @Description: 学生查询列表
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	public Page pagedQueryStudentInfo(int pageNo, int pageSize, StudentInfoModel studentInfo,String userId,String teacherOrgId,boolean isStudent,List classesList,UserRole useRole);
	/**
	 * 
	 * @Title: pagedQueryBaseClass
	 * @Description: 班级查询列表
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	public Page pagedQueryBaseClass(Integer pageSize, Integer pageNum, ClassMonitorSetModel baseClass,String teacher);
	/**
	 * 
	 * @Title: queryApproveByClassId
	 * @Description: 通过班级id查询审核设置信息
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	public StudentApproveSetModel queryApproveByClassId(String classId);
	/**
	 * 
	 * @Title: queryStudentUpdateByNum
	 * @Description: 通过学号查询学生的修改信息
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	public StudentUpdateInfoModel queryStudentUpdateByNum(String stuNumber);
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
	public Page pagedQueryUpdateStudentInfo(int pageNo, int pageSize,StudentUpdateInfoModel studentUpdateInfo, String userId,String[] objectIds,boolean isTeacher);
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
	public Page pagedQueryStudentArmy(int pageNo, int pageSize, StudentArmyInfoModel studentArmyInfo, String userId);
	/** 
	* @Title: countStudentArmyNum 
	* @Description: TODO 获取所有的数据数
	* @param  @return    
	* @return long    
	* @throws 
	*/
	public long countStudentArmyNum();
	/**
	 * 
	 * @Title: getStudentArmyByNum
	 * @Description: 通过身份证号查询学生参军信息
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	public StudentArmyInfoModel getStudentArmyByCertificateCode(String certificateCode); 
	/**
	 * 
	 * @Title: updateStudentStatus
	 * @Description: 修改状态
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	public void updateStudentStatus(String id,String status);
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
	public StudentUpdateInfoModel queryStudentUpdateByNumStatus(String stuNumber,String status);
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
	public List<BaseClassModel> queryClassByTeacher(String id);
	/**
	 * @Title: deleteStudentGuardianUpdate
	 * @Description: 根据学生id删除监护人信息
	 */
	public void deleteStudentGuardianUpdate(String studentId, String status);
	/**
	 * @Title: queryStudentGuardianUpdate
	 * @Description: 根据学生id查询监护人信息
	 */
	public StudentGuardianUpdateModel queryStudentGuardianUpdate(String studentUpdateId,String seqNum,String status);
	/**
	 * @Title: deleteStudentGuardianUpdate
	 * @Description: 根据学生id删除监护人信息
	 */
	public void updateStudentGuardianUpdateStatus(String studentId, String status);
	
	/**
	 * 
	 * @Title: getStudentSchoolCount
	 * @Description: 全校学生统计
	 * @return
	 * @throws
	 */
	public List<StudentSchoolCount> getStudentSchoolCount();
	/**
	 * 
	 * @Title: getStudentCollegeCount
	 * @Description: 分学院学生统计
	 * @return
	 * @throws
	 */
	public List<StudentCollegeCount> getStudentCollegeCount(String grade);
	
	/**
	 * 修改政治面貌
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	public void updateStudentPolitical(String id,String political);
}
