package com.dlink.controller;

import com.dlink.common.result.ProTableResult;
import com.dlink.common.result.Result;
import com.dlink.gateway.result.TestResult;
import com.dlink.model.ClusterConfiguration;
import com.dlink.service.ClusterConfigurationService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * ClusterConfigController
 *
 * @author wenmo
 * @since 2021/11/6 21:16
 */
@Slf4j
@RestController
@RequestMapping("/api/clusterConfiguration")
public class ClusterConfigurationController {
    @Autowired
    private ClusterConfigurationService clusterConfigurationService;

    /**
     * 新增或者更新
     */
    @PutMapping
    public Result saveOrUpdate(@RequestBody ClusterConfiguration clusterConfiguration) {
        TestResult testResult = clusterConfigurationService.testGateway(clusterConfiguration);
        clusterConfiguration.setAvailable(testResult.isAvailable());
        if(clusterConfigurationService.saveOrUpdate(clusterConfiguration)){
            return Result.succeed("新增成功");
        }else {
            return Result.failed("新增失败");
        }
    }

    /**
     * 动态查询列表
     */
    @PostMapping
    public ProTableResult<ClusterConfiguration> listClusterConfigs(@RequestBody JsonNode para) {
        return clusterConfigurationService.selectForProTable(para);
    }

    /**
     * 批量删除
     */
    @DeleteMapping
    public Result deleteMul(@RequestBody JsonNode para) {
        if (para.size()>0){
            List<Integer> error = new ArrayList<>();
            for (final JsonNode item : para){
                Integer id = item.asInt();
                if(!clusterConfigurationService.removeById(id)){
                    error.add(id);
                }
            }
            if(error.size()==0) {
                return Result.succeed("删除成功");
            }else {
                return Result.succeed("删除部分成功，但"+error.toString()+"删除失败，共"+error.size()+"次失败。");
            }
        }else{
            return Result.failed("请选择要删除的记录");
        }
    }

    /**
     * 获取指定ID的信息
     */
    @PostMapping("/getOneById")
    public Result getOneById(@RequestBody ClusterConfiguration clusterConfiguration) {
        clusterConfiguration = clusterConfigurationService.getById(clusterConfiguration.getId());
        return Result.succeed(clusterConfiguration,"获取成功");
    }

    /**
     * 获取可用的集群列表
     */
    @GetMapping("/listEnabledAll")
    public Result listEnabledAll() {
        List<ClusterConfiguration >clusters = clusterConfigurationService.listEnabledAll();
        return Result.succeed(clusters,"获取成功");
    }

    /**
     * 测试
     */
    @PostMapping("/testConnect")
    public Result testConnect(@RequestBody ClusterConfiguration clusterConfiguration) {
        TestResult testResult = clusterConfigurationService.testGateway(clusterConfiguration);
        if(testResult.isAvailable()){
            return Result.succeed("测试链接成功");
        }else {
            return Result.failed(testResult.getError());
        }
    }
}
