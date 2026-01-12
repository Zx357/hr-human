package com.kadmin.dto;

import lombok.Data;
import java.util.List;

/**
 * 组织统计数据 DTO
 */
@Data
public class OrgStatisticsDTO {
    
    /** 员工总数 */
    private Integer totalCount;
    
    /** 学历分布 */
    private List<DistributionItem> educationDistribution;
    
    /** 性别分布 */
    private List<DistributionItem> genderDistribution;
    
    /** 年龄分布 */
    private List<AgeDistributionItem> ageDistribution;
    
    /** 在职状态分布 */
    private List<DistributionItem> statusDistribution;
    
    /** 员工类型分布 */
    private List<DistributionItem> employeeTypeDistribution;
    
    /**
     * 分布项
     */
    @Data
    public static class DistributionItem {
        private String name;
        private Integer value;
        
        public DistributionItem() {}
        
        public DistributionItem(String name, Integer value) {
            this.name = name;
            this.value = value;
        }
    }
    
    /**
     * 年龄分布项
     */
    @Data
    public static class AgeDistributionItem {
        private String range;
        private Integer count;
        
        public AgeDistributionItem() {}
        
        public AgeDistributionItem(String range, Integer count) {
            this.range = range;
            this.count = count;
        }
    }
}
