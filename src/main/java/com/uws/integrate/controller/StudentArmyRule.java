package com.uws.integrate.controller;

import java.util.Map;

import com.uws.common.service.IStudentCommonService;
import com.uws.core.excel.ExcelException;
import com.uws.core.excel.rule.IRule;
import com.uws.core.excel.vo.ExcelColumn;
import com.uws.core.excel.vo.ExcelData;
import com.uws.core.util.SpringBeanLocator;

public class StudentArmyRule implements IRule {
	@Override
	public void format(ExcelData arg0, ExcelColumn arg1, Map arg2) {
	}

	@Override
	public void validate(ExcelData arg0, ExcelColumn column, Map arg2) throws ExcelException {
		IStudentCommonService studentCommonService = (IStudentCommonService)SpringBeanLocator.getBean("com.uws.common.service.impl.StudentCommonServiceImpl");
		
		if ("身份证号".equals(column.getName())) {
			String code = arg0.getValue().toString();
			if (studentCommonService.queryStudentByCode(code)==null){
				String isText = arg0.getId().replaceAll("\\$", "");
				throw new ExcelException(isText + "单元格值("
						+ arg0.getValue().toString()+ 
						")与在系统中没有找到匹配身份证号的学生信息，请修正后重新上传；<br/>");
			}
		}
		
	}

	@Override
	public void operation(ExcelData data, ExcelColumn column, Map arg2,
			Map<String, ExcelData> eds, int site) {
	/*	if ("学生类别".equals(column.getName())) {
			List<Dic> dicGendere = this.dicUtil.getDicInfoList("STU_CATEGORY");
			String genderValue = getString(site, eds, "H");
			for (Dic dic : dicGendere)
				if (genderValue.equals(dic.getName())) {
					data.setValue(dic);
					break;
				}
		}*/
	}
	private String getString(int site, Map<String, ExcelData> eds, String key) {
		String s = "";
		String keyName = "$" + key + "$" + site;
		if ((eds.get(keyName) != null)
				&& (((ExcelData) eds.get(keyName)).getValue() != null)) {
			s = s + (String) ((ExcelData) eds.get(keyName)).getValue();
		}
		return s.trim();
	}

}
