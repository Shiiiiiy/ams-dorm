package com.uws.integrate.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import com.uws.core.hibernate.dao.impl.BaseDaoImpl;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.core.util.HqlEscapeUtil;
import com.uws.domain.base.BaseClassModel;
import com.uws.domain.integrate.ClassMonitorSetModel;
import com.uws.domain.integrate.StudentApproveSetModel;
import com.uws.domain.integrate.StudentArmyInfoModel;
import com.uws.domain.integrate.StudentCollegeCount;
import com.uws.domain.integrate.StudentGuardianUpdateModel;
import com.uws.domain.integrate.StudentSchoolCount;
import com.uws.domain.integrate.StudentUpdateInfoModel;
import com.uws.domain.orientation.StudentInfoModel;
import com.uws.integrate.dao.IStudentManageDao;
import com.uws.sys.service.DicUtil;
import com.uws.sys.service.impl.DicFactory;
import com.uws.user.model.UserRole;
import com.uws.util.CheckUtils;
import com.uws.util.ProjectConstants;


/**
 * 
 * @ClassName: StudentManageDaoImpl
 * @Description: 综合信息服务 学生管理 DAO 实现
 * @author 联合永道
 * @date 2015-7-27 上午9:46:56
 * 
 */
@Repository("com.uws.integrate.dao.impl.StudentManageDaoImpl")
public class StudentManageDaoImpl extends BaseDaoImpl implements IStudentManageDao {
	//数据字典
	private DicUtil dicUtil = DicFactory.getDicUtil();
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
	@Override
	public Page pagedQueryStudentInfo(int pageNo, int pageSize, StudentInfoModel studentInfo, String userId ,String teacherOrgId,boolean isStudent,List classesList,UserRole useRole) 
	{
		List<Object> values = new ArrayList<Object>();
		StringBuffer hql = new StringBuffer("select s from StudentInfoModel s where 1=1 ");
		if (null != studentInfo) {
			// 学院
			if (studentInfo.getCollege() != null && !StringUtils.isEmpty(studentInfo.getCollege().getId())) {
				hql.append(" and  s.college.id = ? ");
				values.add(studentInfo.getCollege().getId());
			}
			// 专业
			if (null != studentInfo.getMajor() && !StringUtils.isEmpty(studentInfo.getMajor().getId())) {
				hql.append(" and  s.major.id = ? ");
				values.add(studentInfo.getMajor().getId());
			}
			// 班级
			if (studentInfo.getClassId() != null && !StringUtils.isEmpty(studentInfo.getClassId().getId())) {
				hql.append(" and  s.classId.id = ? ");
				values.add(studentInfo.getClassId().getId());
			}
			// 学号
			if (!StringUtils.isEmpty(studentInfo.getStuNumber())) {
				hql.append(" and s.stuNumber like ? ");
				if (HqlEscapeUtil.IsNeedEscape(studentInfo.getStuNumber())) {
					values.add("%" + HqlEscapeUtil.escape(studentInfo.getStuNumber()) + "%");
					hql.append(HqlEscapeUtil.HQL_ESCAPE);
				} else
					values.add("%" + studentInfo.getStuNumber() + "%");

			}
			// 姓名
			if (!StringUtils.isEmpty(studentInfo.getName())) {
				hql.append(" and s.name like ? ");
				if (HqlEscapeUtil.IsNeedEscape(studentInfo.getName())) {
					values.add("%" + HqlEscapeUtil.escape(studentInfo.getName()) + "%");
					hql.append(HqlEscapeUtil.HQL_ESCAPE);
				} else
					values.add("%" + studentInfo.getName() + "%");

			}
			// 性别
			if (studentInfo.getGenderDic() != null && !StringUtils.isEmpty(studentInfo.getGenderDic().getId())) {
				hql.append(" and s.genderDic = ? ");
				values.add(studentInfo.getGenderDic());
			}
			
			// 年级
			if (studentInfo.getGrade() != null && !StringUtils.isEmpty(studentInfo.getGrade())) {
				hql.append(" and s.classId.grade = ? ");
				values.add(studentInfo.getGrade());
			}
			
			// 学籍
			if (studentInfo.getEdusStatus() != null && !StringUtils.isEmpty(studentInfo.getEdusStatus())) {
				hql.append(" and s.edusStatus = ? ");
				values.add(studentInfo.getEdusStatus());
			}
			// 民族
			if (studentInfo.getNational() != null && !StringUtils.isEmpty(studentInfo.getNational())) {
				hql.append(" and s.national = ? ");
				values.add(studentInfo.getNational());
			}
			// 政治面貌 2016-5-20
			if (studentInfo.getPoliticalDic() != null && !StringUtils.isEmpty(studentInfo.getPoliticalDic().getId())) {
				hql.append(" and s.politicalDic = ? ");
				values.add(studentInfo.getPoliticalDic());
			}
		}
		//登录为学生情况
		if(isStudent)
		{
			hql.append(" and ( s.classId.monitor.id = ?  or s.classId.id in (select t.code from ClassMonitorSetModel t where t.student.id = ?)  ) ");
			values.add(userId);
			values.add(userId);
		}else if(CheckUtils.isCurrentOrgEqCollege(teacherOrgId) && null!=classesList && classesList.size()==0)
		{
			hql.append(" and s.college.id = ?");
			values.add(teacherOrgId);
		}else if(CheckUtils.isCurrentOrgEqCollege(teacherOrgId) && null!=classesList && classesList.size()>0 &&
				null !=useRole && null !=useRole.getRole() && !StringUtils.isEmpty(useRole.getRole().getId()) && "HKY_COLLEGE_DIRECTOR".equals(useRole.getRole().getCode()))
		{
			hql.append(" and s.college.id = ?");
			values.add(teacherOrgId);
		}else if(CheckUtils.isCurrentOrgEqCollege(teacherOrgId) && null!=classesList && classesList.size()>0)
		{
			hql.append(" and (s.classId.headermaster.id = ? or s.classId.code  in (select t.klass.id from StuJobTeamSetModel t where t.teacher.id = ? and t.teacherType = ? ) ) ");
			values.add(userId);
			values.add(userId);
			values.add(dicUtil.getDicInfo("TEACHER_TYPE", "TEACHER_COUNSELLOR"));
		}
		else
		{
			
		}
		hql.append(" order by  s.classId.grade desc , s.classId , stuNumber ,  s.major , s.college  ");
		
		if (values.size() == 0)
			return this.pagedQuery(hql.toString(), pageNo, pageSize);
		else
			return this.pagedQuery(hql.toString(), pageNo, pageSize, values.toArray());
	}

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
	@Override
	public Page pagedQueryBaseClass(Integer pageSize, Integer pageNum, ClassMonitorSetModel classMonitor,String teacher) {
 
		List<Object> values = new ArrayList<Object>();
		
		StringBuffer hql = new StringBuffer( "from ClassMonitorSetModel where 1=1 ");

		if (null != classMonitor) {
			// 学院单位
			if (null != classMonitor.getMajor() && null != classMonitor.getMajor().getCollage() && !StringUtils.isEmpty(classMonitor.getMajor().getCollage().getId())) {
				hql.append(" and major.collage = ? ");
				values.add(classMonitor.getMajor().getCollage());
			}
			// 专业
			if (null != classMonitor.getMajor() && !StringUtils.isEmpty(classMonitor.getMajor().getId())) {
				hql.append(" and major = ? ");
				values.add(classMonitor.getMajor());
			}
			// 班级名称
			if (!StringUtils.isEmpty(classMonitor.getId())) {
				hql.append(" and id = ? ");
				values.add(classMonitor.getId());
			}
			// 班号
			if (!StringUtils.isEmpty(classMonitor.getCode())) {
				hql.append(" and code like ? ");
				if (HqlEscapeUtil.IsNeedEscape(classMonitor.getCode())) {
					values.add("%" + HqlEscapeUtil.escape(classMonitor.getCode()) + "%");
					hql.append(HqlEscapeUtil.HQL_ESCAPE);
				} else
					values.add("%" + classMonitor.getCode() + "%");
			}
			// 班号
			if (!StringUtils.isEmpty(classMonitor.getGrade())) {
				hql.append(" and grade = ? ");
				values.add(classMonitor.getGrade());
			}
		}
        if(teacher!=null){
        		hql.append(" and id in ( select t.klass.id from StuJobTeamSetModel t where t.teacher.id = ? and t.teacherType = ? )");
        		values.add(teacher);
				values.add(dicUtil.getDicInfo("TEACHER_TYPE", "TEACHER_COUNSELLOR"));
        }
        
        //排序
        hql.append(" order by grade desc,  major , code ");
        
		if (values.size() == 0)
			return this.pagedQuery(hql.toString(), pageNum, pageSize);
		else
			return this.pagedQuery(hql.toString(), pageNum, pageSize, values.toArray());
	}

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
	@Override
	public StudentApproveSetModel queryApproveByClassId(String classId) {
		
		return (StudentApproveSetModel) this.queryUnique("from StudentApproveSetModel s where s.classId.id=?", classId);
	}

	/**
	 * 
	 * @Title: queryStudentUpdateByNum
	 * @Description: 通过学号查询学生已修改的信息
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	@Override
	public StudentUpdateInfoModel queryStudentUpdateByNum(String stuNumber) {

		return (StudentUpdateInfoModel) this.queryUnique("from StudentUpdateInfoModel s where s.stuId.stuNumber =?", stuNumber);
	}
	/**
	 * 
	 * @Title: pagedQueryUpdateStudentInfo
	 * @Description: 查询需要审核的学生信息列表
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	public Page pagedQueryUpdateStudentInfo(int pageNo, int pageSize,StudentUpdateInfoModel studentUpdateInfo, String userId,String[] objectIds,boolean isTeacher) {

		Map<String,Object> values = new HashMap<String,Object>();
		StringBuffer hql = new StringBuffer("select s from StudentUpdateInfoModel s where 1=1 and (s.nextApprover.id = :userId or s.id in (:objectIds)) ");
		values.put("userId",userId);
		values.put("objectIds",objectIds);
		if (null != studentUpdateInfo && null!=studentUpdateInfo.getStuId()) {
			// 班级
			if (studentUpdateInfo.getStuId().getClassId() != null && !StringUtils.isEmpty(studentUpdateInfo.getStuId().getClassId().getId())) {
				hql.append(" and  s.stuId.classId.id = :classId ");
				values.put("classId",studentUpdateInfo.getStuId().getClassId().getId());
			}
			// 学号
			if (!StringUtils.isEmpty(studentUpdateInfo.getStuId().getStuNumber())) {
				hql.append(" and s.stuId.stuNumber like :stuNum ");
				if (HqlEscapeUtil.IsNeedEscape(studentUpdateInfo.getStuId().getStuNumber())) {
					values.put("stuNum","%"+HqlEscapeUtil.escape(studentUpdateInfo.getStuId().getStuNumber())+"%");
					hql.append(HqlEscapeUtil.HQL_ESCAPE);
				} else
					values.put("stuNum","%"+HqlEscapeUtil.escape(studentUpdateInfo.getStuId().getStuNumber())+"%");
			}
			// 姓名
			if (!StringUtils.isEmpty(studentUpdateInfo.getStuId().getName())) {
				hql.append(" and s.stuId.name like :name ");
				if (HqlEscapeUtil.IsNeedEscape(studentUpdateInfo.getStuId().getName())) {
					values.put("name","%" + HqlEscapeUtil.escape(studentUpdateInfo.getStuId().getName()) + "%");
					hql.append(HqlEscapeUtil.HQL_ESCAPE);
				} else
					values.put("name","%" + HqlEscapeUtil.escape(studentUpdateInfo.getStuId().getName()) + "%");
			}
			
			// 审核状态
			if (!StringUtils.isEmpty(studentUpdateInfo.getProcessStatus())) {
				if(ProjectConstants.CURRENT_APPROVE_USER_PROCESS_CODE.equals(studentUpdateInfo.getProcessStatus()))
				{
					hql.append(" and s.nextApprover.id = :approveUserId ");
					values.put("approveUserId",userId);
				}
				else
				{
					hql.append(" and s.processStatus = :processStatus and ( s.nextApprover.id != :approveUserId or s.nextApprover is null ) ");
					values.put("processStatus",studentUpdateInfo.getProcessStatus());
					values.put("approveUserId",userId);
				}
			}
		}
		
		//排序
		if(isTeacher)
			hql.append(" order by s.processStatus");
		else
			hql.append(" order by s.processStatus desc");
		
		if (values.size() == 0)
			return this.pagedQuery(hql.toString(), pageNo, pageSize);
		else
			return this.pagedQuery(hql.toString(), values, pageSize, pageNo);
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

		List<Object> values = new ArrayList<Object>();
		//获取已参军的数据
		StringBuffer hql = new StringBuffer("from StudentArmyInfoModel where 1=1 ");
		if (null != studentArmyInfo) {
			if (null != studentArmyInfo.getStudent()) {
				// 学院
				if (studentArmyInfo.getStudent().getCollege() != null && !StringUtils.isEmpty(studentArmyInfo.getStudent().getCollege().getId())) {
					hql.append(" and  student.college.id = ? ");
					values.add(studentArmyInfo.getStudent().getCollege().getId());
				}
				// 专业
				if (null != studentArmyInfo.getStudent().getMajor() && !StringUtils.isEmpty(studentArmyInfo.getStudent().getMajor().getId())) {
					hql.append(" and  student.major.id = ? ");
					values.add(studentArmyInfo.getStudent().getMajor().getId());
				}
				// 班级
				if (studentArmyInfo.getStudent().getClassId() != null && !StringUtils.isEmpty(studentArmyInfo.getStudent().getClassId().getId())) {
					hql.append(" and  student.classId.id = ? ");
					values.add(studentArmyInfo.getStudent().getClassId().getId());
				}
				// 学号
				if (!StringUtils.isEmpty(studentArmyInfo.getStudent().getStuNumber())) {
					hql.append(" and student.stuNumber like ? ");
					if (HqlEscapeUtil.IsNeedEscape(studentArmyInfo.getStudent().getStuNumber())) {
						values.add("%" + HqlEscapeUtil.escape(studentArmyInfo.getStudent().getStuNumber()) + "%");
						hql.append(HqlEscapeUtil.HQL_ESCAPE);
					} else
						values.add("%" + studentArmyInfo.getStudent().getStuNumber() + "%");
				}
				// 姓名
				if (!StringUtils.isEmpty(studentArmyInfo.getStudent().getName())) {
					hql.append(" and student.name like ? ");
					if (HqlEscapeUtil.IsNeedEscape(studentArmyInfo.getStudent().getName())) {
						values.add("%" + HqlEscapeUtil.escape(studentArmyInfo.getStudent().getName()) + "%");
						hql.append(HqlEscapeUtil.HQL_ESCAPE);
					} else
						values.add("%" + studentArmyInfo.getStudent().getName() + "%");
				}
				// 性别
				if (studentArmyInfo.getStudent().getGenderDic() != null && !StringUtils.isEmpty(studentArmyInfo.getStudent().getGenderDic().getId())) {
					hql.append(" and student.genderDic = ? ");
					values.add(studentArmyInfo.getStudent().getGenderDic());
				}
				
				// 参军时间
				if ( studentArmyInfo.getBeginDate()!= null) {
					hql.append(" and armyDate >= ? ");
					values.add(studentArmyInfo.getBeginDate());
				}
				if (studentArmyInfo.getEndDate() != null) {
					hql.append(" and armyDate <= ? ");
					values.add(studentArmyInfo.getEndDate());
				}
			}

		}
		hql.append(" order by armyDate desc");
		if (values.size() == 0)
			return this.pagedQuery(hql.toString(), pageNo, pageSize);
		else
			return this.pagedQuery(hql.toString(), pageNo, pageSize, values.toArray());
	}

	/**
	 * 
	 * @Title: getStudentArmyByCertificateCode
	 * @Description: 通过身份证号查询学生参军信息
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws
	 */
	@Override
	public StudentArmyInfoModel getStudentArmyByCertificateCode(String certificateCode) {
		
		return (StudentArmyInfoModel) this.queryUnique("from StudentArmyInfoModel s where s.student.certificateCode =?", certificateCode);	
		
	}
	
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
	@Override
	public void updateStudentStatus(String id,String status) {
		if(!StringUtils.isEmpty(id)){
		List<Object> list =new ArrayList<Object>();
		String hql="update StudentUpdateInfoModel s set s.status=? where s.id=?";
		list.add(status);
		list.add(id);
		this.executeHql(hql, list.toArray());	
		}
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

		return (StudentUpdateInfoModel) this.queryUnique("from StudentUpdateInfoModel s where s.stuId.id =? and s.status!=? ", stuNumber,status);
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
	@SuppressWarnings("unchecked")
	public List<BaseClassModel> queryClassByTeacher(String id) {
		//班主任编码HEADMASTER教学辅导员TEACHER_COUNSELLOR
		//return this.query("from BaseClassModel c where c.id in (select s.klass.id from StuJobTeamSetModel s where s.teacher.id= ? and (s.teacherType=? or s.teacherType=? ))", id,dicUtil.getDicInfo("TEACHER_TYPE", "HEADMASTER"),dicUtil.getDicInfo("TEACHER_TYPE", "TEACHER_COUNSELLOR"));
		return this.query("from BaseClassModel c where c.id in (select s.klass.id from StuJobTeamSetModel s where s.teacher.id= ? and (s.teacherType=? or s.teacherType=? ) group by s.klass.id)", id,dicUtil.getDicInfo("TEACHER_TYPE", "HEADMASTER"),dicUtil.getDicInfo("TEACHER_TYPE", "TEACHER_COUNSELLOR"));
	}

	@Override
	public long countStudentArmyNum() {
		String hql = "select count(s.id) from StudentArmyInfoModel s where 1 = 1";
		return this.queryCount(hql, new Object[]{});
	}

	/**
	 * @Title: deleteStudentGuardianUpdate
	 * @Description: 根据更新学生信息id删除监护人信息
	 */
	@Override
	public void deleteStudentGuardianUpdate(String studentId, String status) {
		this.executeHql(" delete from StudentGuardianUpdateModel s where s.studentUpdateInfo.id=? and s.status !=?", studentId, status);
	
	}
	
	/**
	 * @Title: queryStudentGuardianUpdate
	 * @Description: 根据更新学生信息id查询监护人信息
	 */
	@Override
	public StudentGuardianUpdateModel queryStudentGuardianUpdate(String studentUpdateId,String seqNum,String status) {
		List<Object> values = new ArrayList<Object>();
		StringBuffer hql = new StringBuffer( " from StudentGuardianUpdateModel s where s.studentUpdateInfo.id =? and s.seqNum=? ");
		values.add(studentUpdateId);
		values.add(seqNum);
		if (!StringUtils.isEmpty(status)) {
			hql.append(" and s.status !=?");
			values.add(status);
		}
		return (StudentGuardianUpdateModel) this.queryUnique(hql.toString() , values.toArray());
	}
	
	/**
	 * @Title: updateStudentGuardianUpdateStatus
	 * @Description: 根据更新学生信息id修改监护人信息状态
	 */
	@Override
	public void updateStudentGuardianUpdateStatus(String studentId, String status) {
		this.executeHql("update StudentGuardianUpdateModel s set s.status =? where s.studentUpdateInfo.id=? ", status, studentId);
	}

	/**
	 * @return
	 * 2016-5-16 下午5:11:47
	 */
	@Override
	@SuppressWarnings("unchecked")
    public List<StudentSchoolCount> getStudentSchoolCount()
    {
	    return this.query("from StudentSchoolCount order by grade desc", new Object[]{});
    }

	/**
	 * @return
	 * 2016-5-16 下午5:11:43
	 */
	@Override
	@SuppressWarnings("unchecked")
    public List<StudentCollegeCount> getStudentCollegeCount(String grade)
    {
		if(!StringUtils.isEmpty(grade))
			return this.query("from StudentCollegeCount where grade = ? order by grade desc , college", new Object[]{grade});
		else
			return this.query("from StudentCollegeCount order by grade desc , college", new Object[]{});
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
	public void updateStudentPolitical(String id,String political) {
		if(!StringUtils.isEmpty(id)){
		List<Object> list =new ArrayList<Object>();
		String hql="update StudentInfoModel s set s.politicalDic.id = ? where s.id = ? ";
		list.add(political);
		list.add(id);
		this.executeHql(hql, list.toArray());	
		}
	}
}
