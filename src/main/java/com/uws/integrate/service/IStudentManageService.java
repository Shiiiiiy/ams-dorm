package com.uws.integrate.service;

import java.io.IOException;
import java.util.List;

import org.apache.poi.poifs.filesystem.OfficeXmlFileException;

import com.uws.apw.model.ApproveResult;
import com.uws.core.base.IBaseService;
import com.uws.core.excel.ExcelException;
import com.uws.core.hibernate.dao.support.Page;
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
import com.uws.user.model.User;
import com.uws.user.model.UserRole;

/**
 * 
	* @ClassName: IStudentManageService 
	* @Description: 综合信息服务 学生管理 Service接口
	* @author 联合永道
	* @date 2015-7-27 上午9:42:48 
	*
 */
public interface IStudentManageService extends IBaseService{
	
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
	public Page pagedQueryStudentInfo(int pageNo,int pageSize,StudentInfoModel studentInfo,String userId,String teacherOrgId,boolean isStudent,List classesList,UserRole useRole);
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
	public StudentInfoModel queryStudentInfo(String id);
	/**
	 * 
	 * @Title: pagedQueryBaseClass
	 * @Description:通过学院，专业，班级的下列表查询班级信息列表
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	public Page pagedQueryBaseClass(Integer pageSize, Integer pageNum, ClassMonitorSetModel baseClass,String teacher);
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
	public void editMonitor(StudentApproveSetModel studentApproveSet,String orginStudentId);
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
	public void addMonitor(StudentApproveSetModel studentApproveSet);
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
	 * @Title: saveStudentUpdateModel
	 * @Description: 保存学生修改的信息
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	public void saveStudentUpdateModel(StudentUpdateInfoModel studentUpdateModel,List<StudentGuardianUpdateModel> list);
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
	public void updateStudentInfoModel(StudentInfoModel studentInfoModel, List<StudentGuardianModel> list);
	/**
	 * 
	 * @Title: updateStudentUpdateModel
	 * @Description: 修改已保存的保存学生修改的信息
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	public void updateStudentUpdateModel(StudentUpdateInfoModel studentUpdateModel, List<StudentGuardianUpdateModel> list);
	/**
	 * 
	 * @Title: queryStudentUpdateByNum
	 * @Description: 根据学号查询学生修改的信息
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	public StudentUpdateInfoModel queryStudentUpdateByNum(String stuNumber);
	/**
	 * 
	 * @Title: queryStudentUpdateById
	 * @Description: 根据id查询学生修改的信息
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	public StudentUpdateInfoModel queryStudentUpdateById(String id);
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
	public Page pagedQueryUpdateStudentInfo(int pageNo, int pageSize, StudentUpdateInfoModel studentUpdateInfo, String userId,String[] objectIds,boolean isTeacher);
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
	public Page pagedQueryStudentArmy(int pageNo,int pageSize,StudentArmyInfoModel studentArmyInfo,String userId);

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
	public StudentArmyInfoModel queryStudentArmy(String id);
	/**
	 * @Title: getStudentArmyByCertificateCode
	 * @Description: 根据学生学号查询学生参军的信息
	 * @param studentNum
	 * @return
	 */
	public StudentArmyInfoModel getStudentArmyByCertificateCode(String certificateCode);
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
	public ApproveResult submitApprove(String objectId,User initiator,User nextApprover);
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
	public void updateStudentStatus(String objectId,String status);
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
	public void updateStudent(StudentUpdateInfoModel studentUpdateModel); 
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
	 * 
	 * @Title: deleteStudentAramy
	 * @Description: 参军信息删除
	 * @param ids
	 * @throws
	 */
	public void deleteStudentAramy(String[] ids);
	/**
	 * 描述信息: 参军信息新增
	 * @param armyStudentId
	 */
    public void addStudentAramy(StudentArmyInfoModel studentArmy);
    /**
	 * 描述信息: 参军信息修改
	 * @param updateStudentAramy
	 */
    public void updateStudentAramy(StudentArmyInfoModel studentArmy);
	/**
	 * @throws Exception  
	* @Title: importData 
	* @Description: TODO 导入Excel方法
	* @param  @param list
	* @param  @param filePath
	* @param  @param compareId
	* @param  @throws OfficeXmlFileException
	* @param  @throws IOException
	* @param  @throws IllegalAccessException
	* @param  @throws ExcelException
	* @param  @throws InstantiationException
	* @param  @throws ClassNotFoundException    
	* @return void    
	* @throws 
	*/
	public String importStudentArmy(List<StudentArmyInfoModel> list, String filePath) throws OfficeXmlFileException, IOException, IllegalAccessException, ExcelException, InstantiationException, ClassNotFoundException, Exception;
	/**
	 * @Title: queryStudentGuardianUpdate
	 * @Description: 根据学生id查询监护人信息
	 */
	public StudentGuardianUpdateModel queryStudentGuardianUpdate(String studentUpdateId,String seqNum,String status);
	
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
