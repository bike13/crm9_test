package com.kakarote.crm9.erp.crm.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Aop;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.jfinal.kit.Kv;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;
import com.kakarote.crm9.common.annotation.*;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.entity.AdminFieldSort;
import com.kakarote.crm9.erp.admin.entity.AdminRecord;
import com.kakarote.crm9.erp.admin.service.AdminFieldService;
import com.kakarote.crm9.erp.admin.service.AdminSceneService;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.common.CrmExcelUtil;
import com.kakarote.crm9.erp.crm.entity.CrmBusiness;
import com.kakarote.crm9.erp.crm.entity.CrmContract;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import com.kakarote.crm9.erp.crm.entity.CrmCustomerSetting;
import com.kakarote.crm9.erp.crm.service.CrmBusinessService;
import com.kakarote.crm9.erp.crm.service.CrmContactsService;
import com.kakarote.crm9.erp.crm.service.CrmContractService;
import com.kakarote.crm9.erp.crm.service.CrmCustomerService;
import com.kakarote.crm9.utils.AuthUtil;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CrmCustomerController extends Controller {

    @Inject
    private CrmCustomerService crmCustomerService;

    @Inject
    private CrmContactsService crmContactsService;//?????????

    @Inject
    private CrmBusinessService crmBusinessService;//??????

    @Inject
    private CrmContractService crmContractService;//??????

    @Inject
    private AdminFieldService adminFieldService;

    @Inject
    private AdminSceneService adminSceneService;

    /**
     * @author wyq
     * ???????????????
     */
    @Permissions({"crm:customer:index"})
    public void queryPageList(BasePageRequest basePageRequest) {
        JSONObject jsonObject = basePageRequest.getJsonObject().fluentPut("type", 2);
        basePageRequest.setJsonObject(jsonObject);
        renderJson(adminSceneService.filterConditionAndGetPageList(basePageRequest));
    }

    /**
     * @author wyq
     * ?????????????????????
     */
    @Permissions({"crm:pool:index"})
    public void queryPoolPageList(BasePageRequest basePageRequest) {
        JSONObject jsonObject = basePageRequest.getJsonObject().fluentPut("type", CrmEnum.CRM_CUSTOMER_POOL.getType());
        basePageRequest.setJsonObject(jsonObject);
        renderJson(adminSceneService.filterConditionAndGetPageList(basePageRequest));
    }

    /**
     * @author wyq
     * ????????????????????????
     */
    public void queryList(BasePageRequest<CrmCustomer> basePageRequest) {
        renderJson(R.ok().put("data", crmCustomerService.getCustomerPageList(basePageRequest)));
    }

    /**
     * @author wyq
     * ?????????????????????
     */
    @Permissions({"crm:customer:save", "crm:customer:update"})
    public void addOrUpdate() {
        JSONObject jsonObject = JSON.parseObject(getRawData());
        renderJson(crmCustomerService.addOrUpdate(jsonObject, "noImport"));
    }

    /**
     * @author wyq
     * ????????????id??????
     */
    @Permissions("crm:customer:read")
    @NotNullValidate(value = "customerId", message = "??????id????????????")
    public void queryById(@Para("customerId") Integer customerId) {
        boolean auth = AuthUtil.isPoolAuth(customerId);
        if (auth) {
            renderJson(R.ok().put("data",new Record().set("dataAuth",0)));
            return;
        }
        renderJson(R.ok().put("data", crmCustomerService.queryById(customerId)));
    }

    /**
     * @author wyq
     * ????????????????????????
     */
    @NotNullValidate(value = "name", message = "????????????????????????")
    public void queryByName(@Para("name") String name) {
        renderJson(R.ok().put("data", crmCustomerService.queryByName(name)));
    }

    /**
     * @author wyq
     * ????????????id???????????????
     */
    public void queryContacts(BasePageRequest<CrmCustomer> basePageRequest) {
        boolean auth = AuthUtil.isPoolAuth(basePageRequest.getData().getCustomerId());
        if (auth) {
            renderJson(R.noAuth());
            return;
        }
        renderJson(crmCustomerService.queryContacts(basePageRequest));
    }

    /**
     * @author wyq
     * ??????id????????????
     */
    @Permissions("crm:customer:delete")
    @NotNullValidate(value = "customerIds", message = "??????id????????????")
    public void deleteByIds(@Para("customerIds") String customerIds) {
        renderJson(crmCustomerService.deleteByIds(customerIds));
    }

    /**
     * @author wyq
     * ????????????id????????????
     */
    public void queryBusiness(BasePageRequest<CrmCustomer> basePageRequest) {
        boolean auth = AuthUtil.isPoolAuth(basePageRequest.getData().getCustomerId());
        if (auth) {
            renderJson(R.noAuth());
            return;
        }
        renderJson(crmCustomerService.queryBusiness(basePageRequest));
    }

    /**
     * @author wyq
     * ????????????id????????????
     */
    public void queryContract(BasePageRequest<CrmCustomer> basePageRequest) {
        boolean auth = AuthUtil.isPoolAuth(basePageRequest.getData().getCustomerId());
        if (auth) {
            renderJson(R.noAuth());
            return;
        }
        renderJson(crmCustomerService.queryContract(basePageRequest));
    }

    /**
     * @author wyq
     * ????????????id??????????????????
     */
    public void queryReceivablesPlan(BasePageRequest<CrmCustomer> basePageRequest) {
        boolean auth = AuthUtil.isPoolAuth(basePageRequest.getData().getCustomerId());
        if (auth) {
            renderJson(R.noAuth());
            return;
        }
        renderJson(crmCustomerService.queryReceivablesPlan(basePageRequest));
    }

    /**
     * @author zxy
     * ????????????id????????????
     */
    public void queryReceivables(BasePageRequest<CrmCustomer> basePageRequest) {
        boolean auth = AuthUtil.isPoolAuth(basePageRequest.getData().getCustomerId());
        if (auth) {
            renderJson(R.noAuth());
            return;
        }
        renderJson(crmCustomerService.queryReceivables(basePageRequest));
    }

    /**
     * @author wyq
     * ????????????
     */
    @Permissions("crm:customer:lock")
    @NotNullValidate(value = "ids", message = "??????id????????????")
    @NotNullValidate(value = "isLock", message = "????????????????????????")
    public void lock(@Para("") CrmCustomer crmCustomer) {
        renderJson(crmCustomerService.lock(crmCustomer));
    }

    /**
     * @author wyq
     * ??????????????????
     */
    public void setDealStatus(@Para("ids")String ids,@Para("dealStatus")Integer dealStatus){
        renderJson(crmCustomerService.setDealStatus(ids,dealStatus));
    }

    /**
     * ????????????
     *
     * @author wyq
     */
    @Permissions("crm:customer:transfer")
    @NotNullValidate(value = "customerIds", message = "??????id????????????")
    @NotNullValidate(value = "newOwnerUserId", message = "????????????????????????")
    @NotNullValidate(value = "transferType", message = "????????????????????????")
    public void transfer(@Para("") CrmCustomer crmCustomer) {
        String[] customerIdsArr = crmCustomer.getCustomerIds().split(",");
        Db.tx(()->{
            for (String customerId : customerIdsArr) {
                if(Db.queryInt("select count(*) from `72crm_crm_customer` where find_in_set(?,ro_user_id) and customer_id = ?",BaseUtil.getUserId(),customerId)>0){
                    renderJson(R.error("????????????"));
                    return false;
                }
                crmCustomer.setCustomerId(Integer.valueOf(customerId));
                renderJson(crmCustomerService.updateOwnerUserId(crmCustomer));
                String changeType = crmCustomer.getChangeType();
                if (StrUtil.isNotEmpty(changeType)) {
                    String[] changeTypeArr = changeType.split(",");
                    for (String type : changeTypeArr) {
                        //????????????????????????
                        if ("1".equals(type)) {
                            renderJson(crmContactsService.updateOwnerUserId(crmCustomer.getCustomerId(), crmCustomer.getNewOwnerUserId()));
                        }
                        //?????????????????????
                        if ("2".equals(type)) {
                            renderJson(crmBusinessService.updateOwnerUserId(crmCustomer));
                        }
                        //?????????????????????
                        if ("3".equals(type)) {
                            renderJson(crmContractService.updateOwnerUserId(crmCustomer));
                        }
                    }
                }
            }
            return true;
        });

    }

    /**
     * @author wyq
     * ??????????????????
     */
    @NotNullValidate(value = "customerId", message = "??????id????????????")
    public void getMembers(@Para("customerId") Integer customerId) {
        boolean auth = AuthUtil.isPoolAuth(customerId);
        if (auth) {
            renderJson(R.noAuth());
            return;
        }
        renderJson(R.ok().put("data", crmCustomerService.getMembers(customerId)));
    }

    /**
     * @author wyq
     * ??????????????????
     */
    @Permissions("crm:customer:teamsave")
    @NotNullValidate(value = "ids", message = "??????id????????????")
    @NotNullValidate(value = "memberIds", message = "??????id????????????")
    @NotNullValidate(value = "power", message = "????????????????????????")
    @Before(Tx.class)
    public void addMembers(@Para("") CrmCustomer crmCustomer) {
        String changeType = crmCustomer.getChangeType();
        if (StrUtil.isNotEmpty(changeType)) {
            String[] changeTypeArr = changeType.split(",");
            for (String type : changeTypeArr) {
                //????????????
                if ("2".equals(type)) {
                    CrmBusiness crmBusiness = new CrmBusiness();
                    crmBusiness.setIds(crmCustomerService.getBusinessIdsByCustomerIds(crmCustomer.getIds()));
                    crmBusiness.setMemberIds(crmCustomer.getMemberIds());
                    crmBusiness.setPower(crmCustomer.getPower());
                    crmBusiness.setTransferType(crmCustomer.getTransferType());
                    crmBusinessService.addMember(crmBusiness);
                }
                //????????????
                if ("3".equals(type)) {
                    CrmContract crmContract = new CrmContract();
                    crmContract.setIds(crmCustomerService.getContractIdsByCustomerIds(crmCustomer.getIds()));
                    crmContract.setMemberIds(crmCustomer.getMemberIds());
                    crmContract.setPower(crmCustomer.getPower());
                    crmContract.setTransferType(crmCustomer.getTransferType());
                    crmCustomerService.addMember(crmCustomer);
                }
            }
            crmCustomerService.addMember(crmCustomer);
        }
        renderJson(crmCustomerService.addMember(crmCustomer));
    }

    /**
     * @author wyq
     * ??????????????????
     */
    @Permissions("crm:customer:teamsave")
    @NotNullValidate(value = "ids", message = "??????id????????????")
    @NotNullValidate(value = "memberIds", message = "??????id????????????")
    @NotNullValidate(value = "power", message = "????????????????????????")
    public void updateMembers(@Para("") CrmCustomer crmCustomer) {
        renderJson(crmCustomerService.addMember(crmCustomer));
    }

    /**
     * @author wyq
     * ??????????????????
     */
    @Permissions("crm:customer:teamsave")
    @NotNullValidate(value = "ids", message = "??????id????????????")
    @NotNullValidate(value = "memberIds", message = "??????id????????????")
    public void deleteMembers(@Para("") CrmCustomer crmCustomer) {
        renderJson(crmCustomerService.deleteMembers(crmCustomer));
    }

    /**
     * @author zxy
     * ????????????????????????
     */
    @Permissions("manage:crm:setting")
    @NotNullValidate(value = "followupDay", message = "????????????????????????")
    @NotNullValidate(value = "dealDay", message = "????????????????????????")
    @NotNullValidate(value = "type", message = "????????????????????????")
    public void updateRulesSetting() {
        //????????????
        Integer followupDay = getParaToInt("followupDay");
        //????????????
        Integer dealDay = getParaToInt("dealDay");
        //????????????
        Integer type = getParaToInt("type");
        Integer remindConfig = getParaToInt("putInPoolRemindConfig");
        Integer remindDay = getParaToInt("putInPoolRemindDays");
        renderJson(crmCustomerService.updateRulesSetting(dealDay, followupDay, type, remindDay,remindConfig));
    }

    /**
     * @author zxy
     * ??????????????????????????????
     */
    public void getRulesSetting() {
        renderJson(crmCustomerService.getRulesSetting());
    }

    /**
     * @author wyq
     * ??????????????????
     */
    @NotNullValidate(value = "typesId", message = "??????id????????????")
    @NotNullValidate(value = "content", message = "??????????????????")
    @NotNullValidate(value = "category", message = "????????????????????????")
    public void addRecord(@Para("") AdminRecord adminRecord) {
        boolean auth = AuthUtil.isCrmAuth(CrmEnum.CRM_CUSTOMER, adminRecord.getTypesId());
        if (auth) {
            renderJson(R.noAuth());
            return;
        }
        renderJson(crmCustomerService.addRecord(adminRecord));
    }

    /**
     * @author wyq
     * ??????????????????
     */
    public void getRecord(BasePageRequest<CrmCustomer> basePageRequest) {
        boolean auth = AuthUtil.isPoolAuth(basePageRequest.getData().getCustomerId());
        if (auth) {
            renderJson(R.noAuth());
            return;
        }
        renderJson(R.ok().put("data", crmCustomerService.getRecord(basePageRequest)));
    }

    /**
     * @author wyq
     * ??????????????????
     */
    @Permissions("crm:customer:excelexport")
    public void batchExportExcel(BasePageRequest basePageRequest){
        JSONObject jsonObject=basePageRequest.getJsonObject();
        String ids=jsonObject.getString("ids");
        JSONObject data =new JSONObject();
        data.fluentPut("customerExport",new JSONObject().fluentPut("name","customer_id").fluentPut("condition","in").fluentPut("value", ids));
        jsonObject.fluentPut("data",data).fluentPut("search","").fluentPut("type",2);
        basePageRequest.setJsonObject(jsonObject);
        JSONObject resultData = (JSONObject)adminSceneService.getCrmPageList(basePageRequest).get("data");
        List<Record> recordList = resultData.getJSONArray("list").toJavaList(Record.class);
        export(recordList);
        renderNull();
    }

    /**
     * @author wyq
     * ????????????
     */
    @Permissions("crm:customer:excelexport")
    public void allExportExcel(BasePageRequest basePageRequest){
        JSONObject jsonObject = basePageRequest.getJsonObject();
        jsonObject.fluentPut("excel", "yes").fluentPut("type", 2);
        AdminSceneService adminSceneService = new AdminSceneService();
        JSONObject data = (JSONObject)adminSceneService.filterConditionAndGetPageList(basePageRequest).get("data");
        List<Record> recordList = data.getJSONArray("list").toJavaList(Record.class);
        export(recordList);
        renderNull();
    }

    /**
     * @author wyq
     * ??????????????????
     */
    @Permissions("crm:pool:excelexport")
    public void poolBatchExportExcel(BasePageRequest basePageRequest){
        JSONObject jsonObject=basePageRequest.getJsonObject();
        String ids=jsonObject.getString("ids");
        JSONObject data =new JSONObject();
        data.fluentPut("customerExport",new JSONObject().fluentPut("name","customer_id").fluentPut("condition","in").fluentPut("value", ids));
        jsonObject.fluentPut("data",data).fluentPut("search","").fluentPut("type",9);
        basePageRequest.setJsonObject(jsonObject);
        JSONObject resultData = (JSONObject)adminSceneService.getCrmPageList(basePageRequest).get("data");
        List<Record> recordList = resultData.getJSONArray("list").toJavaList(Record.class);
        export(recordList);
        renderNull();
    }

    /**
     * @author wyq
     * ??????????????????
     */
    @Permissions("crm:pool:excelexport")
    public void poolAllExportExcel(BasePageRequest basePageRequest){
        JSONObject jsonObject = basePageRequest.getJsonObject();
        jsonObject.fluentPut("excel", "yes").fluentPut("type", 8);
        AdminSceneService adminSceneService = new AdminSceneService();
        JSONObject data = (JSONObject)adminSceneService.filterConditionAndGetPageList(basePageRequest).get("data");
        List<Record> recordList = data.getJSONArray("list").toJavaList(Record.class);
        export(recordList);
        renderNull();
    }

    private void export(List<Record> recordList){
        ExcelWriter writer = ExcelUtil.getWriter();
        try {
            AdminFieldSort adminFieldSort = new AdminFieldSort();
            adminFieldSort.setLabel(CrmEnum.CRM_CUSTOMER.getType());
            List<Record> headList = Aop.get(AdminFieldService.class).queryListHead(adminFieldSort);
            headList.forEach(head -> writer.addHeaderAlias(StrUtil.toUnderlineCase(head.getStr("fieldName")),head.getStr("name")));
            writer.merge(headList.size()-1, "????????????");
            HttpServletResponse response = getResponse();
            List<Map<String, Object>> list = new ArrayList<>();
            if (recordList.size() == 0){
                Record record = new Record();
                headList.forEach(head -> record.set(StrUtil.toUnderlineCase(head.getStr("fieldName")), ""));
                list.add(record.getColumns());
            }
            recordList.forEach(record -> {
                record.set("deal_status",record.getInt("deal_status").equals(1)?"?????????":"?????????");
                list.add(record.getColumns());
            });
            writer.setOnlyAlias(true);
            writer.write(list, true);
            writer.setRowHeight(0, 20);
            writer.setRowHeight(1, 20);
            for (int i = 0; i < headList.size(); i++) {
                writer.setColumnWidth(i, 20);
            }
            Cell cell = writer.getCell(0, 0);
            CellStyle cellStyle = cell.getCellStyle();
            cellStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font font = writer.createFont();
            font.setBold(true);
            font.setFontHeightInPoints((short) 16);
            cellStyle.setFont(font);
            cell.setCellStyle(cellStyle);
            //?????????????????????
            //response???HttpServletResponse??????
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            //test.xls??????????????????????????????????????????????????????????????????????????????
            response.setHeader("Content-Disposition", "attachment;filename=customer.xls");
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
        } catch (Exception e) {
            Log.getLog(getClass()).error("?????????????????????",e);
        } finally {
            // ??????writer???????????????
            writer.close();
        }
    }

//    private void export(List<Record> recordList){
//        try (ExcelWriter writer = ExcelUtil.getWriter()) {
//            AdminFieldService adminFieldService = new AdminFieldService();
//            List<Record> fieldList = adminFieldService.customFieldList(CrmEnum.CRM_CUSTOMER.getType());
//            List<Record> customerFields = adminFieldService.list(CrmEnum.CRM_CUSTOMER.getType());
//            Kv kv = new Kv();
//            customerFields.forEach(customerField -> kv.set(customerField.getStr("field_name"), customerField.getStr("name")));
//            writer.addHeaderAlias("customer_name", kv.getStr("customer_name"));
//            writer.addHeaderAlias("telephone", kv.getStr("telephone"));
//            writer.addHeaderAlias("mobile", kv.getStr("mobile"));
//            writer.addHeaderAlias("website", kv.getStr("website"));
//            writer.addHeaderAlias("next_time", kv.getStr("next_time"));
//            writer.addHeaderAlias("deal_status", "????????????");
//            writer.addHeaderAlias("create_user_name", "?????????");
//            writer.addHeaderAlias("owner_user_name", "?????????");
//            writer.addHeaderAlias("address", "?????????");
//            writer.addHeaderAlias("location", "????????????");
//            writer.addHeaderAlias("detail_address", "????????????");
//            writer.addHeaderAlias("lng", "??????????????????");
//            writer.addHeaderAlias("lat", "??????????????????");
//            writer.addHeaderAlias("create_time", "????????????");
//            writer.addHeaderAlias("update_time", "????????????");
//            writer.addHeaderAlias("remark", kv.getStr("remark"));
//            writer.addHeaderAlias("last_content", "??????????????????");
//            for (Record field : fieldList) {
//                writer.addHeaderAlias(field.getStr("name"), field.getStr("name"));
//            }
//            writer.merge(fieldList.size() + 16, "????????????");
//            HttpServletResponse response = getResponse();
//            List<Map<String, Object>> list = new ArrayList<>();
//            if (recordList.size() == 0) {
//                Record record = new Record().set("customer_name", "").set("telephone", "").set("mobile", "").set("website", "").set("next_time", "").set("deal_status", "").set("create_user_name", "").set("owner_user_name", "").set("address", "").set("location", "").set("detail_address", "").set("lng", "").set("lat", "").set("create_time", "").set("update_time", "").set("remark", "");
//                for (Record field : fieldList) {
//                    record.set(field.getStr("name"), "");
//                }
//                list.add(record.getColumns());
//            }
//            for (Record record : recordList) {
//                list.add(record.remove("batch_id", "create_user_id", "customer_id", "is_lock", "owner_user_id", "ro_user_id", "rw_user_id", "followup", "field_batch_id", "business_count", "pool_day").getColumns());
//            }
//            writer.write(list, true);
//            writer.setRowHeight(0, 20);
//            writer.setRowHeight(1, 20);
//            for (int i = 0; i < fieldList.size() + 17; i++) {
//                writer.setColumnWidth(i, 20);
//            }
//            Cell cell = writer.getCell(0, 0);
//            CellStyle cellStyle = cell.getCellStyle();
//            cellStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
//            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//            Font font = writer.createFont();
//            font.setBold(true);
//            font.setFontHeightInPoints((short) 16);
//            cellStyle.setFont(font);
//            cell.setCellStyle(cellStyle);
//            //?????????????????????
//            //response???HttpServletResponse??????
//            response.setContentType("application/vnd.ms-excel;charset=utf-8");
//            response.setCharacterEncoding("UTF-8");
//            //test.xls??????????????????????????????????????????????????????????????????????????????
//            response.setHeader("Content-Disposition", "attachment;filename=customer.xls");
//            ServletOutputStream out = response.getOutputStream();
//            writer.flush(out);
//        } catch (Exception e) {
//            Log.getLog(getClass()).error("?????????????????????",e);
//        }
//    }

    /**
     * ??????????????????
     *
     * @author zxy
     */
    @Permissions("crm:customer:putinpool")
    public void updateCustomerByIds() {
        String ids = get("ids");
        renderJson(crmCustomerService.updateCustomerByIds(ids));
    }

    /**
     * ??????????????????
     *
     * @author zxy
     */
    @Permissions("crm:pool:distribute")
    public void distributeByIds() {
        String ids = get("ids");
        Long userId = getLong("userId");
        renderJson(crmCustomerService.getCustomersByIds(ids, userId));
    }

    /**
     * ??????????????????
     *
     * @author zxy
     */
    @Permissions("crm:pool:receive")
    public void receiveByIds() {
        String ids = get("ids");
        Long userId = getLong("userId");
        renderJson(crmCustomerService.getCustomersByIds(ids, userId));
    }

    /**
     * @author wyq
     * ??????????????????
     */
    public void downloadExcel() {
        List<Record> recordList = adminFieldService.queryAddField(CrmEnum.CRM_CUSTOMER);
        recordList.removeIf(record -> "file".equals(record.getStr("formType")) || "checkbox".equals(record.getStr("formType")) || "user".equals(record.getStr("formType")) || "structure".equals(record.getStr("formType")));
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("???????????????");
        sheet.setDefaultRowHeight((short)400);
        CellStyle textStyle = wb.createCellStyle();
        DataFormat format = wb.createDataFormat();
        textStyle.setDataFormat(format.getFormat("@"));
        for (int i = 0; i < recordList.size() + 2; i++) {
            sheet.setDefaultColumnStyle(i,textStyle);
            sheet.setColumnWidth(i,20*256);
        }
        HSSFRow titleRow = sheet.createRow(0);
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        cellStyle.setFont(font);
        titleRow.createCell(0).setCellValue("??????????????????(*)????????????");
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        titleRow.getCell(0).setCellStyle(cellStyle);
        CellRangeAddress region = new CellRangeAddress(0, 0, 0, recordList.size() + 1);
        sheet.addMergedRegion(region);
        try {
            HSSFRow row = sheet.createRow(1);
            for (int i = 0; i < recordList.size(); i++) {
                Record record = recordList.get(i);
                //????????????????????????
                if ("map_address".equals(record.getStr("field_name"))) {
                    HSSFCell cell1 = row.createCell(i);
                    cell1.setCellValue("???");
                    HSSFCell cell2 = row.createCell(i + 1);
                    cell2.setCellValue("???");
                    HSSFCell cell3 = row.createCell(i + 2);
                    cell3.setCellValue("???");
                    HSSFSheet hideSheet = wb.createSheet("address");
                    wb.setSheetHidden(wb.getSheetIndex(hideSheet), true);
                    int rowId = 0;
                    // ?????????????????????????????????
                    Row provinceRow = hideSheet.createRow(rowId++);
                    provinceRow.createCell(0).setCellValue("?????????");
                    List<String> provinceList = Db.query("select city_name from 72crm_crm_area where parent_id = ?", 100000);
                    for (int x = 0; x < provinceList.size(); x++) {
                        Cell provinceCell = provinceRow.createCell(x + 1);
                        provinceCell.setCellValue(provinceList.get(x));
                    }
                    // ??????????????????????????????????????????????????????????????????????????????????????????
                    Map<String, List<String>> areaMap = CrmExcelUtil.getAreaMap();
                    for (String key : areaMap.keySet()) {
                        List<String> son = areaMap.get(key);
                        Row subRow = hideSheet.createRow(rowId++);
                        subRow.createCell(0).setCellValue(key);
                        for (int x = 0; x < son.size(); x++) {
                            Cell cell = subRow.createCell(x + 1);
                            cell.setCellValue(son.get(x));
                        }
                        // ?????????????????????
                        String range = CrmExcelUtil.getRange(1, rowId, son.size());
                        Name name = wb.createName();
                        // key????????????
                        name.setNameName(key);
                        String formula = "address!" + range;
                        name.setRefersToFormula(formula);
                    }
                    // ???????????????
                    CellRangeAddressList provRangeAddressList = new CellRangeAddressList(2, Integer.MAX_VALUE, i, i);
                    String[] arr = provinceList.toArray(new String[]{});
                    DVConstraint provConstraint = DVConstraint.createExplicitListConstraint(arr);
                    HSSFDataValidation provinceDataValidation = new HSSFDataValidation(provRangeAddressList, provConstraint);
                    provinceDataValidation.createErrorBox("error", "????????????????????????");
                    sheet.addValidationData(provinceDataValidation);
                    //??? ????????????
                    for (int x = 2; x < 10000; x++) {
                        CrmExcelUtil.setDataValidation(CrmExcelUtil.getCorrespondingLabel(i + 1), sheet, x, i + 1);
                        CrmExcelUtil.setDataValidation(CrmExcelUtil.getCorrespondingLabel(i + 2), sheet, x, i + 2);
                    }
                } else {
                    HSSFCell cell = row.createCell(i);
                    if (record.getInt("is_null") == 1) {
                        cell.setCellValue(record.getStr("name") + "(*)");
                    } else {
                        cell.setCellValue(record.getStr("name"));
                    }
                    String[] setting = record.get("setting");
                    if (setting != null && setting.length != 0) {
                        CellRangeAddressList regions = new CellRangeAddressList(2, Integer.MAX_VALUE, i, i);
                        DVConstraint constraint = DVConstraint.createExplicitListConstraint(setting);
                        HSSFDataValidation dataValidation = new HSSFDataValidation(regions, constraint);
                        sheet.addValidationData(dataValidation);
                    }
                }
            }
            HttpServletResponse response = getResponse();
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            //test.xls??????????????????????????????????????????????????????????????????????????????
            response.setHeader("Content-Disposition", "attachment;filename=customer_import.xls");
            wb.write(response.getOutputStream());

        } catch (Exception e) {
            Log.getLog(getClass()).error("error", e);
        } finally {
            try {
                wb.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
        renderNull();
    }

    /**
     * @author wyq
     * ????????????
     */
    @Permissions("crm:customer:excelimport")
    public void uploadExcel() {
        String prefix= BaseUtil.getDate();
        UploadFile file=getFile("file",prefix);
        Integer repeatHandling=getParaToInt("repeatHandling");
        Long ownerUserId=getParaToLong("ownerUserId");
        CrmExcelUtil excelUtil=new CrmExcelUtil();
        Long messageId = excelUtil.addWork(CrmEnum.CRM_CUSTOMER,file.getFile().getAbsolutePath(),ownerUserId,repeatHandling);
        renderJson(R.ok().put("data",messageId));
    }

    @NotNullValidate(value = "customerNum", message = "?????????????????????",type = HttpEnum.JSON)
    public void customerSetting(@RequestBody CrmCustomerSetting customerSetting){
        renderJson(crmCustomerService.customerSetting(customerSetting));
    }
    @NotNullValidate("type")
    public void queryCustomerSetting(BasePageRequest<CrmCustomerSetting> pageRequest){
        renderJson(R.ok().put("data",crmCustomerService.queryCustomerSetting(pageRequest)));
    }

    @NotNullValidate("settingId")
    public void editCustomerSetting(@Para("settingId") Integer settingId){
        renderJson(crmCustomerService.queryEditCustomerSetting(settingId));
    }

    @NotNullValidate("settingId")
    public void deleteCustomerSetting(@Para("settingId") Integer settingId){
        renderJson(crmCustomerService.deleteCustomerSetting(settingId));
    }
}
